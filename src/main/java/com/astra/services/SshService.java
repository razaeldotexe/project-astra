package com.astra.services;

import com.jcraft.jsch.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class SshService {
    private static final Logger logger = LoggerFactory.getLogger(SshService.class);
    private final Dotenv dotenv = Dotenv.load();
    
    private Session session;
    private ChannelShell channel;
    private OutputStream out;
    private InputStream in;
    private BufferedReader reader;
    private String lastErrorMessage = "No connection attempt yet.";

    public SshService() {
        // Initialization can be lazy or immediate. We'll attempt immediate connection.
        connect();
    }

    public synchronized void connect() {
        String host = dotenv.get("VPS_HOST");
        String portStr = dotenv.get("VPS_PORT", "22");
        String user = dotenv.get("VPS_USER");
        String password = dotenv.get("VPS_PASSWORD");

        if (host == null || user == null) {
            logger.warn("VPS credentials not fully configured in .env. SSH Service disabled.");
            lastErrorMessage = "Missing VPS_HOST or VPS_USER in .env";
            return;
        }

        logger.info("Attempting SSH connection to {}@{} on port {}", user, host, portStr);

        try {
            JSch jsch = new JSch();
            int port = Integer.parseInt(portStr);
            session = jsch.getSession(user, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();
            channel = (ChannelShell) session.openChannel("shell");
            
            in = channel.getInputStream();
            out = channel.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(in));

            channel.connect();
            logger.info("SSH Session established with VPS: {}@{}", user, host);
            lastErrorMessage = null;

            // Read initial banner
            readOutputAsync();

        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            logger.error("Failed to connect to VPS via SSH: {}", lastErrorMessage);
        }
    }

    public synchronized String executeCommand(String command) {
        if (session == null || !session.isConnected() || channel == null || !channel.isConnected()) {
            connect();
        }

        if (session == null || !session.isConnected()) {
            return "❌ [v1.1 ERROR] Not connected to VPS: " + (lastErrorMessage != null ? lastErrorMessage : "Unknown Error");
        }

        try {
            // Clear existing buffer before sending new command
            while (in.available() > 0) {
                in.read(new byte[1024]);
            }

            out.write((command + "\n").getBytes());
            out.flush();

            StringBuilder response = new StringBuilder();
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 seconds timeout

            while (System.currentTimeMillis() - startTime < timeout) {
                if (in.available() > 0) {
                    byte[] buff = new byte[1024];
                    int i = in.read(buff);
                    if (i > 0) {
                        response.append(new String(buff, 0, i));
                        startTime = System.currentTimeMillis(); // Reset timeout as we are getting data
                    }
                } else {
                    Thread.sleep(100); // Poll delay
                    if (response.length() > 0 && in.available() <= 0) {
                        // We have some data and nothing more is coming immediately
                        break;
                    }
                }
            }
            
            String result = response.toString().trim();
            return result.isEmpty() ? "[No Output]" : result;

        } catch (Exception e) {
            logger.error("Error executing SSH command", e);
            return "[ERROR] " + e.getMessage();
        }
    }

    private void readOutputAsync() {
        // Just clear the initial buffer
        try {
            Thread.sleep(1000);
            while (in.available() > 0) {
                in.read(new byte[1024]);
            }
        } catch (Exception ignored) {}
    }

    public void disconnect() {
        if (channel != null) channel.disconnect();
        if (session != null) session.disconnect();
        logger.info("SSH Session disconnected.");
    }
}
