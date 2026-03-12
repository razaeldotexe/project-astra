package com.astra;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * FoxSync Server Warden - AI System Administrator for Minecraft servers.
 * Diagnoses server telemetry (TPS, RAM, CPU) and provides optimization advice.
 */
public class MinecraftWarden {

    public static class WardenReport {
        public String statusDiagnosis;
        public String rootCauseAnalysis;
        public List<String> recommendedActions = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("🛡️ **FoxSync Server Warden Report** 🐺\n\n");
            sb.append("**1. Status Diagnosis:**\n").append(statusDiagnosis).append("\n\n");
            sb.append("**2. Root Cause Analysis:**\n").append(rootCauseAnalysis).append("\n\n");
            sb.append("**3. Recommended Actions:**\n");
            for (String action : recommendedActions) {
                sb.append("- ").append(action).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Menganalisis telemetri server Minecraft.
     * @param telemetryJson String JSON berisi metrik server.
     * @return Objek WardenReport berisi diagnosis dan saran.
     */
    public WardenReport diagnose(String telemetryJson) {
        WardenReport report = new WardenReport();

        if (telemetryJson == null || telemetryJson.isEmpty() || telemetryJson.equals("{}")) {
            report.statusDiagnosis = "Data telemetri tidak ditemukan atau tidak mencukupi.";
            report.rootCauseAnalysis = "Sistem tidak dapat menarik kesimpulan tanpa data metrik hardware.";
            return report;
        }

        try {
            JsonObject data = JsonParser.parseString(telemetryJson).getAsJsonObject();
            
            double tps = data.has("tps") ? data.get("tps").getAsDouble() : 20.0;
            double ramUsagePercent = data.has("ram_usage_percent") ? data.get("ram_usage_percent").getAsDouble() : 0.0;
            double cpuUsagePercent = data.has("cpu_usage_percent") ? data.get("cpu_usage_percent").getAsDouble() : 0.0;
            int playerCount = data.has("player_count") ? data.get("player_count").getAsInt() : 0;

            JsonObject config = data.has("config") ? data.getAsJsonObject("config") : new JsonObject();
            int viewDistance = config.has("view-distance") ? config.get("view-distance").getAsInt() : 10;

            JsonArray activePlugins = data.has("active_plugins") ? data.getAsJsonArray("active_plugins") : new JsonArray();

            // Step 1: Telemetry Diagnosis
            if (tps < 15.0) {
                report.statusDiagnosis = String.format("Server mengalami TPS drop parah (%.1f) yang mengindikasikan lag sistemik.", tps);
            } else if (tps < 18.5) {
                report.statusDiagnosis = String.format("Kesehatan server mulai menurun (TPS: %.1f). Ada sedikit hambatan pada tick rate.", tps);
            } else {
                report.statusDiagnosis = String.format("Server dalam kondisi sehat (TPS: %.1f). Performa berjalan optimal.", tps);
            }

            // Step 2: Root Cause Analysis
            StringBuilder rca = new StringBuilder();
            if (tps < 18.5 && playerCount < 5 && cpuUsagePercent > 80) {
                rca.append("TPS rendah pada populasi rendah dengan CPU tinggi menunjukkan adanya plugin berat atau entity loop.");
            } else if (ramUsagePercent > 90) {
                rca.append("Pemakaian RAM yang sangat tinggi (%.1f%%) berpotensi menyebabkan micro-stutter akibat Garbage Collection.");
            } else if (viewDistance > 8 && ramUsagePercent > 80) {
                rca.append(String.format("View-distance (%d) terlalu tinggi untuk beban RAM saat ini.", viewDistance));
            } else {
                rca.append("Kondisi hardware dan konfigurasi terlihat seimbang.");
            }
            report.rootCauseAnalysis = rca.toString();

            // Step 3: Incident Response / Recommendations
            if (tps < 18.0) {
                report.recommendedActions.add("Segera jalankan perintah untuk membersihkan dropped items atau entities berlebih.");
            }
            if (viewDistance > 8) {
                report.recommendedActions.add(String.format("Turunkan `view-distance` dari %d ke 8 di server.properties.", viewDistance));
            }
            if (ramUsagePercent > 85) {
                report.recommendedActions.add("Pertimbangkan untuk melakukan restart server terjadwal untuk membersihkan cache RAM.");
            }

            // Plugin specific check
            boolean hasHeavyPlugin = false;
            for (JsonElement p : activePlugins) {
                String pluginName = p.getAsString().toLowerCase();
                if (pluginName.contains("dynmap") || pluginName.contains("worldedit")) {
                    hasHeavyPlugin = true;
                    break;
                }
            }
            if (hasHeavyPlugin && cpuUsagePercent > 70) {
                report.recommendedActions.add("Batasi penggunaan plugin berat seperti Dynmap/WorldEdit di jam sibuk.");
            }

            if (report.recommendedActions.isEmpty()) {
                report.recommendedActions.add("Pertahankan konfigurasi saat ini dan monitor pertumbuhan entity secara berkala.");
            }

        } catch (Exception e) {
            report.statusDiagnosis = "Kesalahan Diagnosis: Gagal memproses data telemetri.";
            report.rootCauseAnalysis = "Detail Error: " + e.getMessage();
        }

        return report;
    }

    /**
     * Mengambil status server Minecraft dari IP publik menggunakan api.mcsrvstat.us.
     * @param ip Alamat IP atau hostname server.
     * @return String JSON telemetri yang diformat untuk diagnosis internal.
     */
    public String fetchRemoteStatus(String ip) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mcsrvstat.us/3/" + ip))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject remoteData = JsonParser.parseString(response.body()).getAsJsonObject();
                
                if (remoteData.has("online") && remoteData.get("online").getAsBoolean()) {
                    JsonObject players = remoteData.getAsJsonObject("players");
                    
                    // Convert remote API format to Warden's internal telemetry format
                    JsonObject internalTelemetry = new JsonObject();
                    internalTelemetry.addProperty("tps", 20.0); // Remote API doesn't provide real-time TPS easily
                    internalTelemetry.addProperty("ram_usage_percent", 0.0); // Unknown for remote
                    internalTelemetry.addProperty("cpu_usage_percent", 0.0); // Unknown for remote
                    internalTelemetry.addProperty("player_count", players.get("online").getAsInt());
                    internalTelemetry.addProperty("is_remote", true);
                    internalTelemetry.addProperty("hostname", ip);

                    if (remoteData.has("version")) {
                        internalTelemetry.addProperty("version", remoteData.get("version").getAsString());
                    }

                    // Mock config and plugins from remote MOTD/Info if available
                    JsonObject config = new JsonObject();
                    config.addProperty("view-distance", 8); // Safe estimate
                    internalTelemetry.add("config", config);
                    
                    JsonArray plugins = new JsonArray();
                    if (remoteData.has("plugins")) {
                        JsonObject pluginsObj = remoteData.getAsJsonObject("plugins");
                        if (pluginsObj.has("names")) {
                            internalTelemetry.add("active_plugins", pluginsObj.getAsJsonArray("names"));
                        }
                    } else {
                        internalTelemetry.add("active_plugins", plugins);
                    }

                    return internalTelemetry.toString();
                } else {
                    return "{\"error\": \"Server is offline or unreachable.\"}";
                }
            } else {
                return "{\"error\": \"Failed to fetch data from API (Status: " + response.statusCode() + ")\"}";
            }
        } catch (Exception e) {
            return "{\"error\": \"Exception during fetch: " + e.getMessage() + "\"}";
        }
    }
}
