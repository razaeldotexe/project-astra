package com.astra;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

/**
 * StatFoxAnalyst - AI Data Scientist for StatFox bot.
 * Analyzes server activity metadata and provides actionable insights.
 */
public class StatFoxAnalyst {

    public static class AnalysisResult {
        public String executiveSummary;
        public List<String> keyFindings = new ArrayList<>();
        public List<String> actionableRecommendations = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("**1. Executive Summary:**\n").append(executiveSummary).append("\n\n");
            sb.append("**2. Key Findings:**\n");
            for (String finding : keyFindings) {
                sb.append("- ").append(finding).append("\n");
            }
            sb.append("\n**3. Actionable Recommendations:**\n");
            for (String rec : actionableRecommendations) {
                sb.append("- ").append(rec).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Menganalisis data JSON aktivitas server.
     * @param jsonData String JSON berisi metrik aktivitas.
     * @return Objek AnalysisResult berisi wawasan.
     */
    public AnalysisResult analyze(String jsonData) {
        AnalysisResult result = new AnalysisResult();
        
        if (jsonData == null || jsonData.isEmpty() || jsonData.equals("{}")) {
            result.executiveSummary = "Data tidak mencukupi untuk menghasilkan wawasan pada periode ini.";
            return result;
        }

        try {
            JsonObject data = JsonParser.parseString(jsonData).getAsJsonObject();
            
            int totalMessages = data.has("total_messages") ? data.get("total_messages").getAsInt() : 0;
            int voiceMinutes = data.has("voice_minutes") ? data.get("voice_minutes").getAsInt() : 0;
            double memberRetention = data.has("member_retention") ? data.get("member_retention").getAsDouble() : 0.0;
            int peakHour = data.has("peak_hour") ? data.get("peak_hour").getAsInt() : -1;

            // Synthesis & Analysis
            result.executiveSummary = String.format(
                "Kesehatan server minggu ini terlihat %s. Dengan total %d pesan dan %d menit aktivitas voice, komunitas Anda menunjukkan pola interaksi yang %s.",
                (memberRetention > 0.7 ? "sangat baik" : "stabil"),
                totalMessages,
                voiceMinutes,
                (totalMessages > 1000 ? "dinamis" : "santai")
            );

            // Key Findings
            if (peakHour != -1) {
                result.keyFindings.add(String.format("Lonjakan aktivitas tertinggi terdeteksi di jam %d:00.", peakHour));
            }
            if (voiceMinutes > (totalMessages / 2)) {
                result.keyFindings.add("Aktivitas voice channel lebih dominan dibandingkan chat teks minggu ini.");
            } else {
                result.keyFindings.add("Chat teks (General) tetap menjadi pusat interaksi utama server.");
            }
            result.keyFindings.add(String.format("Tingkat retensi anggota baru berada di angka %.1f%%.", memberRetention * 100));

            // Actionable Recommendations
            if (peakHour >= 18 && peakHour <= 22) {
                result.actionableRecommendations.add("Karena aktivitas tinggi di malam hari, pertimbangkan untuk mengadakan event game jam 8 malam.");
            } else if (peakHour >= 12 && peakHour <= 15) {
                result.actionableRecommendations.add("Komunitas aktif di siang hari. Cobalah posting pengumuman penting di jam makan siang.");
            }
            
            if (memberRetention < 0.5) {
                result.actionableRecommendations.add("Retensi anggota rendah. Pertimbangkan untuk memperbaiki sistem 'Welcome' atau pengenalan member baru.");
            }
            result.actionableRecommendations.add("Gunakan kanal pengumuman untuk merayakan pencapaian aktivitas minggu ini agar member merasa dihargai.");

        } catch (Exception e) {
            result.executiveSummary = "Terjadi kesalahan saat memproses data analisis: " + e.getMessage();
        }

        return result;
    }
}
