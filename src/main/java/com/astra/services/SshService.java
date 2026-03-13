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
            return;
        }

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

            // Read initial banner
            readOutputAsync();

        } catch (Exception e) {
            logger.error("Failed to connect to VPS via SSH", e);
        }
    }

    public synchronized String executeCommand(String command) {
        if (session == null || !session.isConnected() || channel == null || !channel.isConnected()) {
            connect();
        }

        if (session == null || !session.isConnected()) {
            return "[ERROR] Not connected to VPS.";
        }

        try {
            out.write((command + "\n").getBytes());
            out.flush();

            // Wait a bit for output and read it
            // Note: This is a simplified approach for a shell channel.
            // For a more robust implementation, one might use ChannelExec for single commands,
            // but the requirement asks for persistent session (like cd being maintained).
            Thread.sleep(500); 
            
            StringBuilder response = new StringBuilder();
            while (in.available() > 0) {
                byte[] buff = new byte[1024];
                int i = in.read(buff);
                if (i <= 0) break;
                response.append(new String(buff, 0, i));
            }
            
            return response.length() > 0 ? response.toString() : "[No Output]";

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
