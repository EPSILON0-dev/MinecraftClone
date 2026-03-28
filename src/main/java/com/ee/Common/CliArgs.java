package com.ee.Common;

public final class CliArgs {
    public record ClientOptions(String serverHost, int serverPort, int renderDistance) {
    }

    public record ServerOptions(int port) {
    }

    private CliArgs() {
    }

    public static ClientOptions parseClient(String[] args) {
        String serverHost = Config.NETWORK_SERVER_HOST;
        int serverPort = Config.NETWORK_SERVER_PORT;
        int renderDistance = Config.WORLD_CHUNK_DISTANCE;

        for (String arg : args) {
            String[] option = splitOption(arg);
            String key = option[0];
            String value = option[1];

            switch (key) {
                case "--server-ip":
                case "--server-host":
                    serverHost = value;
                    break;
                case "--port":
                case "--server-port":
                    serverPort = parsePositiveInt(key, value);
                    break;
                case "--render-distance":
                    renderDistance = parsePositiveInt(key, value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown client argument: " + key);
            }
        }

        return new ClientOptions(serverHost, serverPort, renderDistance);
    }

    public static ServerOptions parseServer(String[] args) {
        int port = Config.NETWORK_SERVER_PORT;

        for (String arg : args) {
            String[] option = splitOption(arg);
            String key = option[0];
            String value = option[1];

            switch (key) {
                case "--port":
                case "--server-port":
                    port = parsePositiveInt(key, value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown server argument: " + key);
            }
        }

        return new ServerOptions(port);
    }

    private static String[] splitOption(String arg) {
        int separatorIndex = arg.indexOf('=');
        if (separatorIndex <= 0 || separatorIndex == arg.length() - 1) {
            throw new IllegalArgumentException("Arguments must use --name=value format: " + arg);
        }

        return new String[] { arg.substring(0, separatorIndex), arg.substring(separatorIndex + 1) };
    }

    private static int parsePositiveInt(String key, String value) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException("Argument must be > 0: " + key);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Argument must be an integer: " + key, e);
        }
    }
}
