# MCC

Small Java voxel sandbox prototype built with LWJGL, OpenGL, JOML, and ImGui.

## Screenshot

![Game Screenshot Placeholder](docs/screenshot-placeholder.png)

Replace the image above with an actual in-game screenshot later.

## Current Features

- Chunk-based voxel world generation
- Client/server architecture over UDP
- On-demand chunk streaming from the server
- Block breaking and placement synchronized over the network
- Heartbeat packets to keep client connections alive
- Client-side chunk request TTL to avoid spamming duplicate requests
- Automatic unloading of chunks that stay outside render distance for too long
- ImGui debug overlay with runtime information
- CLI arguments for server IP, port, and render distance

## Controls

- `W A S D`: move
- `Mouse`: look around
- `Space`: jump
- `Left Shift`: sprint
- `Left Click`: break block
- `Right Click`: place block
- `Middle Click`: pick block
- `Mouse Wheel`: cycle selected block
- `Esc`: close the game

## Requirements

- Java 21
- Maven
- A platform that supports LWJGL/OpenGL

## How To Run

### 1. Compile

```bash
mvn -q -DskipTests compile
```

If your shell still defaults to an older JDK, set `JAVA_HOME` to Java 21 first.

Example on Linux:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
mvn -q -DskipTests compile
```

### Build The JAR

To build the packaged JARs with Maven:

```bash
mvn -q -DskipTests package
```

The generated files will be placed in `target/`.

This build now produces two runnable shaded JARs:

- `target/mcc-0.1-client.jar`
- `target/mcc-0.1-server.jar`

To run the packaged server JAR:

```bash
java -jar target/mcc-0.1-server.jar --port=6767
```

To run the packaged client JAR:

```bash
java -jar target/mcc-0.1-client.jar --server-ip=localhost --server-port=6767 --render-distance=3
```

### 2. Start The Server

Run the server main class with an optional port override:

```text
com.ee.Server.Main --port=6767
```

### 3. Start The Client

Run the client main class with the server address and render distance:

```text
com.ee.Client.Main --server-ip=localhost --server-port=6767 --render-distance=3
```

## CLI Arguments

### Server

- `--port=<number>`

### Client

- `--server-ip=<host>`
- `--server-host=<host>`
- `--server-port=<number>`
- `--port=<number>`
- `--render-distance=<number>`

## Debug Overlay

The in-game debug menu currently shows:

- FPS
- player position, direction, and velocity
- current chunk and chunk hash
- compressed chunk size
- selected block
- loaded chunk count
- connected server IP and port
- sent packet count
- received packet count

## Notes

This is a simple Minecraft clone I made to learn Java. Since we all know that the minecraft is the only non-enterprise use case for Java this seemed like the only viable project idea.
