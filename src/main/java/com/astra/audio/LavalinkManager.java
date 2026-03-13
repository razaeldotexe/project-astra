package com.astra.audio;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LavalinkManager {
    private static final Logger logger = LoggerFactory.getLogger(LavalinkManager.class);
    private static LavalinkClient client;

    public static LavalinkClient createClient(long userId) {
        client = LavalinkClient.create(userId);
        client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());
        return client;
    }

    public static void initializeNode(Dotenv dotenv) {
        if (client == null) return;

        String host = dotenv.get("LAVALINK_HOST", "localhost");
        int port = Integer.parseInt(dotenv.get("LAVALINK_PORT", "2333"));
        String password = dotenv.get("LAVALINK_PASSWORD", "youshallnotpass");
        boolean secure = Boolean.parseBoolean(dotenv.get("LAVALINK_SECURE", "false"));

        NodeOptions node = new NodeOptions.Builder()
                .setName("main-node")
                .setServerUri((secure ? "wss" : "ws") + "://" + host + ":" + port)
                .setPassword(password)
                .build();

        client.addNode(node);
        logger.info("Lavalink Client node added: {}:{}", host, port);
    }

    public static LavalinkClient getClient() {
        return client;
    }
}
