package com.ee.Client;

import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import com.ee.Common.BlockType;
import com.ee.Common.CliArgs;

import org.joml.*;

import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWErrorCallback.createPrint;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main implements AutoCloseable, Runnable {
    private static final String windowTitle = "Hello, World!";
    private static final int windowWidth = 1280;
    private static final int windowHeight = 720;
    private static final float mouseLookSensitivity = 0.0025f;
    private static ImGuiOverlay imGuiOverlay = new ImGuiOverlay();
    private static Shader mainShader;
    private static Shader selectionShader;
    private static Texture texture;
    private static Camera camera;
    private static ClientWorld world;
    private static PosOnlyMesh cubeMesh;
    private static Player player;
    private static NetworkManager networkManager;
    private static long windowHandle;
    private boolean firstMouseEvent = true;
    private double lastCursorX;
    private double lastCursorY;
    private final CliArgs.ClientOptions options;

    public Main() {
        this(new String[0]);
    }

    public Main(String... args) {
        this.options = CliArgs.parseClient(args);
    }

    public static void main(String... args) {
        try (Main main = new Main(args)) {
            main.run();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Client usage: --server-ip=<host> --server-port=<port> --render-distance=<chunks>");
        }
    }

    public void run() {
        init();
        loop();
    }

    public void init() {
        createPrint(System.err).set();
        System.out.println("Starting LWJGL " + Version.getVersion());
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        windowHandle = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        glfwSetKeyCallback(windowHandle, (windowHandle, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(windowHandle, true);
            }
        });
        glfwSetCursorPosCallback(windowHandle, (windowHandle, xpos, ypos) -> {
            if (firstMouseEvent) {
                lastCursorX = xpos;
                lastCursorY = ypos;
                firstMouseEvent = false;
                return;
            }

            float xOffset = (float) (xpos - lastCursorX);
            float yOffset = (float) (lastCursorY - ypos);
            lastCursorX = xpos;
            lastCursorY = ypos;

            if (player != null) {
                player.rotate(xOffset * mouseLookSensitivity, yOffset * mouseLookSensitivity);
            }
        });
        glfwSetFramebufferSizeCallback(windowHandle, (windowHandle, width, height) -> {
            glViewport(0, 0, width, height);
            if (player != null && height > 0) {
                camera.setAspectRatio((float) width / height);
            }
        });
        glfwSetScrollCallback(windowHandle, (windowHandle, xoffset, yoffset) -> {
            if (player != null) {
                if (yoffset > 0) {
                    player.nextBlockType();
                } else if (yoffset < 0) {
                    player.previousBlockType();
                }
            }
        });
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(windowHandle, pWidth, pHeight);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    windowHandle,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pHeight.get(0)) / 2);
        }
        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(1);
        glfwShowWindow(windowHandle);
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        createCapabilities();
        glEnable(GL_DEPTH_TEST);
        System.out.println("OpenGL: " + glGetString(GL_VERSION));
        glClearColor(0.529f, 0.808f, 0.922f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        imGuiOverlay.init(windowHandle);

        try {
            String vertSource = Files.readString(Path.of("assets/shaders/main/main.vert"));
            String fragSource = Files.readString(Path.of("assets/shaders/main/main.frag"));
            mainShader = new Shader(vertSource, fragSource);
        } catch (Exception e) {
            System.err.println("Failed to compile the main shader: " + e.toString());
            e.printStackTrace(System.err);
        }

        try {
            String vertSource = Files.readString(Path.of("assets/shaders/selection/selection.vert"));
            String fragSource = Files.readString(Path.of("assets/shaders/selection/selection.frag"));
            selectionShader = new Shader(vertSource, fragSource);
        } catch (Exception e) {
            System.err.println("Failed to compile the selection shader: " + e.toString());
            e.printStackTrace(System.err);
        }

        try {
            texture = new Texture(Path.of("assets/textures/atlas.png"));
        } catch (Exception e) {
            System.err.println("Failed to load the texture: " + e.toString());
            e.printStackTrace(System.err);
        }

        camera = new Camera(new Vector3f(0), new Vector3f(0), (float) 90.0f, (float) windowWidth / windowHeight);
        player = new Player(new Vector3f(0.0f, 67.0f, 0.0f), new Vector3f(0.0f, 0.0f, -1.0f));
        System.out.println("Fly camera controls: WASD move, Space up, Left Shift down, mouse look.");

        world = new ClientWorld(options.renderDistance());
        networkManager = new NetworkManager(world, options.serverHost(), options.serverPort());
        cubeMesh = Cube.cubeMesh();
    }

    private double lastFrameTime;

    public void loop() {
        lastFrameTime = glfwGetTime();
        while (!glfwWindowShouldClose(windowHandle)) {
            double currentFrameTime = glfwGetTime();
            float deltaTime = (float) (currentFrameTime - lastFrameTime);
            lastFrameTime = currentFrameTime;
            float fps = deltaTime > 0.0f ? 1.0f / deltaTime : 0.0f;

            var nearestMissing = world.getNearestMissingChunk(player);
            if (nearestMissing.isPresent()) {
                networkManager.requestChunk(nearestMissing.get().x, nearestMissing.get().y);
            }

            world.unloadDistantChunks(player);

            updatePlayer(deltaTime);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderChunks();
            drawSelectionBox();
            imGuiOverlay.render(fps, player, world, networkManager);
            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }

    private void updatePlayer(float deltaTime) {
        updatePlaceBreak();
        updatePlayerControls();
        player.update(world, deltaTime);
        player.setupCamera(camera);
    }

    private void renderChunks() {
        world.generateQueuedMeshes();
        mainShader.use();
        mainShader.setUniformMatrix4f("cameraViewProjection", camera.getViewProjectionMatrix());
        texture.bind(0);
        mainShader.setUniform1i("baseTex", 0);
        world.renderChunks(mainShader, "modelMatrix");
        texture.unbind();
    }

    private void drawSelectionBox() {
        var rayCastResult = RayCast.rayCast(camera, world, 5.0f, false);
        if (rayCastResult.isPresent()) {
            Matrix4f modelMatrix = new Matrix4f()
                    .translate(new Vector3f(rayCastResult.get()).sub(0.001f, 0.001f, 0.001f)).scale(1.002f);
            selectionShader.use();
            selectionShader.setUniformMatrix4f("cameraViewProjection", camera.getViewProjectionMatrix());
            selectionShader.setUniformMatrix4f("modelMatrix", modelMatrix);
            cubeMesh.bind();
            cubeMesh.draw();
            cubeMesh.unbind();
        }
    }

    private boolean prevSpace = false;

    private void updatePlayerControls() {
        Vector2f movementInput = new Vector2f();
        if (glfwGetKey(windowHandle, GLFW_KEY_W) == GLFW_PRESS) {
            movementInput.y += 1.0f;
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_S) == GLFW_PRESS) {
            movementInput.y -= 1.0f;
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_D) == GLFW_PRESS) {
            movementInput.x += 1.0f;
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_A) == GLFW_PRESS) {
            movementInput.x -= 1.0f;
        }

        if (movementInput.lengthSquared() > 0) {
            movementInput.normalize();
            player.setSprinting(glfwGetKey(windowHandle, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS && movementInput.y > 0.0f);
            player.move(world, movementInput);
        } else {
            player.setSprinting(false);
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_SPACE) == GLFW_PRESS) {
            if (!prevSpace) {
                player.jump(world);
            }
        } else {
            prevSpace = false;
        }
    }

    private boolean prevLeft = false;
    private boolean prevRight = false;
    private boolean prevMiddle = false;

    private void updatePlaceBreak() {
        var rayCastResult = RayCast.rayCast(camera, world, 5.0f, false);
        if (rayCastResult.isEmpty()) {
            return;
        }
        var previousRayCastResult = RayCast.rayCast(camera, world, 5.0f, true);

        if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
            if (!prevLeft) {
                prevLeft = true;
                networkManager.sendBlockUpdate(rayCastResult.get(), BlockType.Air);
            }
        } else {
            prevLeft = false;
        }

        if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_MIDDLE) == GLFW_PRESS) {
            if (!prevMiddle) {
                prevMiddle = true;
                player.setSelectedBlockType(world.getBlock(rayCastResult.get()).type);
            }
        } else {
            prevMiddle = false;
        }

        if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
            if (!prevRight) {
                prevRight = true;
                if (player.canPlaceBlockAt(previousRayCastResult.get())) {
                    try {
                        networkManager.sendBlockUpdate(previousRayCastResult.get(), player.selectedBlockType());
                    } catch (Exception e) {
                        System.err.println("Failed to place block: " + e.toString());
                        e.printStackTrace(System.err);
                    }
                }
            }
        } else {
            prevRight = false;
        }

    }

    @Override
    public void close() {
        if (networkManager != null) {
            networkManager.close();
        }
        imGuiOverlay.close();
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
