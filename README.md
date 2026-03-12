# Astra Discord Bot 🚀

Bot Discord berbasis Java (JDA) dengan fitur sistem ekonomi lengkap, monitoring server Minecraft, dan analisis aktivitas server.

## ✨ Fitur Utama
- **Sistem Ekonomi**: Wallet, Bank, Kerja, Hadiah Harian, Toko Item, dan Leaderboard.
- **Monitoring Minecraft**: Cek kesehatan server (TPS, RAM) dan status server luar via IP.
- **StatFox Analyst**: Analisis aktivitas chat dan voice server secara real-time.
- **Slash Commands**: Semua perintah terintegrasi dengan antarmuka Discord terbaru.

## 🛠️ Persyaratan
- Java 17 atau lebih baru.
- Maven.
- Bot Discord Token (dari [Discord Developer Portal](https://discord.com/developers/applications)).

## 🚀 Cara Instalasi
1. Clone repositori ini.
2. Salin file `.env.example` menjadi `.env`.
3. Isi `DISCORD_TOKEN` dan `GUILD_ID` di file `.env`.
4. Jalankan perintah berikut untuk meng-compile:
   ```bash
   mvn clean package
   ```
5. Jalankan bot:
   ```bash
   mvn exec:java
   ```

## 💰 Perintah Ekonomi
- `/balance` - Cek saldo wallet & bank.
- `/work` - Cari uang dengan bekerja.
- `/daily` - Klaim hadiah harian.
- `/shop` & `/buy` - Belanja item menarik.
- `/leaderboard` - Lihat siapa yang paling sultan.

## 📄 Lisensi
[MIT License](LICENSE)
