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
        logger.info("Current Working Directory: {}", System.getProperty("user.dir"));
        String host = dotenv.get("VPS_HOST");
        if (host != null) host = host.trim();
        
        String portStr = dotenv.get("VPS_PORT", "22");
        if (portStr != null) portStr = portStr.trim();
        
        String user = dotenv.get("VPS_USER");
        if (user != null) user = user.trim();
        
        String password = dotenv.get("VPS_PASSWORD");
        if (password != null) password = password.trim();

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
            lastErrorMessage = e.getMessage() + " (Target: " + host + ":" + portStr + ")";
            logger.error("Failed to connect to VPS via SSH: {}", lastErrorMessage);
        }
    }

    public synchronized String executeCommand(String command) {
        StringBuilder finalOutput = new StringBuilder();
        executeCommandStreaming(command, finalOutput::append);
        String result = finalOutput.toString().trim();
        return result.isEmpty() ? "[No Output]" : result;
    }

    public synchronized void executeCommandStreaming(String command, java.util.function.Consumer<String> onUpdate) {
        if (session == null || !session.isConnected() || channel == null || !channel.isConnected()) {
            connect();
        }

        if (session == null || !session.isConnected()) {
            onUpdate.accept("❌ [v1.1 ERROR] Not connected to VPS: " + (lastErrorMessage != null ? lastErrorMessage : "Unknown Error"));
            return;
        }

        try {
            // Clear existing buffer
            while (in.available() > 0) {
                in.read(new byte[1024]);
            }

            out.write((command + "\n").getBytes());
            out.flush();

            long startTime = System.currentTimeMillis();
            long timeout = 10000; // Increase to 10 seconds for streaming commands
            StringBuilder currentBatch = new StringBuilder();

            while (System.currentTimeMillis() - startTime < timeout) {
                if (in.available() > 0) {
                    byte[] buff = new byte[1024];
                    int i = in.read(buff);
                    if (i > 0) {
                        String rawChunk = new String(buff, 0, i);
                        String cleanChunk = stripAnsiCodes(rawChunk);
                        if (!cleanChunk.isEmpty()) {
                            onUpdate.accept(cleanChunk);
                        }
                        startTime = System.currentTimeMillis();
                    }
                } else {
                    Thread.sleep(200);
                    if (System.currentTimeMillis() - startTime > 1000 && !command.contains("npm")) {
                        // If no data for 1s and not a slow command like npm, assume done
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error executing SSH command", e);
            onUpdate.accept("[ERROR] " + e.getMessage());
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

    private String stripAnsiCodes(String text) {
        if (text == null) return "";
        // More aggressive regex to catchDEC private mode sequences and complex ANSI codes
        return text.replaceAll("\u001B\\[[;?]*[\\d;]*[A-Za-z]", "")
                   .replaceAll("\u001B\\(B", "")
                   .replaceAll("\r", "") // Also strip carriage returns which mess up Discord code blocks
                   .replace("\u001B", ""); // Last resort: strip any orphaned ESC characters
    }
}
