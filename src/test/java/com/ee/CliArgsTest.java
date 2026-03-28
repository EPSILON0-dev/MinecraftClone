package com.ee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.ee.Common.CliArgs;
import com.ee.Common.Config;

public class CliArgsTest {

    @Test
    public void clientParserUsesDefaultsWhenNoArgsProvided() {
        CliArgs.ClientOptions options = CliArgs.parseClient(new String[0]);

        assertEquals(Config.NETWORK_SERVER_HOST, options.serverHost());
        assertEquals(Config.NETWORK_SERVER_PORT, options.serverPort());
        assertEquals(Config.WORLD_CHUNK_DISTANCE, options.renderDistance());
    }

    @Test
    public void clientParserAcceptsServerAndRenderDistanceOverrides() {
        CliArgs.ClientOptions options = CliArgs.parseClient(new String[] {
                "--server-ip=192.168.1.20",
                "--server-port=7001",
                "--render-distance=6"
        });

        assertEquals("192.168.1.20", options.serverHost());
        assertEquals(7001, options.serverPort());
        assertEquals(6, options.renderDistance());
    }

    @Test
    public void serverParserAcceptsPortOverride() {
        CliArgs.ServerOptions options = CliArgs.parseServer(new String[] { "--port=9000" });

        assertEquals(9000, options.port());
    }

    @Test
    public void parserRejectsUnknownArguments() {
        assertThrows(IllegalArgumentException.class, () -> CliArgs.parseClient(new String[] { "--unknown=value" }));
        assertThrows(IllegalArgumentException.class, () -> CliArgs.parseServer(new String[] { "--server-ip=host" }));
    }

    @Test
    public void parserRejectsMalformedOrInvalidNumericArguments() {
        assertThrows(IllegalArgumentException.class, () -> CliArgs.parseClient(new String[] { "--port" }));
        assertThrows(IllegalArgumentException.class, () -> CliArgs.parseClient(new String[] { "--render-distance=0" }));
        assertThrows(IllegalArgumentException.class, () -> CliArgs.parseServer(new String[] { "--port=abc" }));
    }
}
