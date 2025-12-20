
# 📊 Finote - Smart Finance Note

**Finote** adalah aplikasi manajemen keuangan pribadi berbasis Android yang dirancang untuk membantu pengguna mencatat transaksi, melacak tujuan keuangan (*Goal Tracker*), dan melakukan pencatatan otomatis menggunakan teknologi OCR (Optical Character Recognition).

## 🚀 Fitur Utama

* **Smart Receipt Scanning (OCR):** Menggunakan sensor kamera dan ML Kit untuk membaca nominal saldo dari struk belanja secara otomatis.
* **Goal Tracker:** Membantu pengguna menetapkan dan memantau progres target tabungan mereka.
* **Real-time Synchronization:** Integrasi dengan Supabase untuk memastikan data tersimpan secara aman dan sinkron di berbagai perangkat.
* **Financial Insights:** Ringkasan visual mengenai pemasukan dan pengeluaran pengguna.
* **Authentication:** Sistem login yang aman didukung oleh Supabase Auth dan Google Credential Manager.

---

## 🛠️ Arsitektur & Teknologi

Aplikasi ini dibangun menggunakan teknologi terkini di ekosistem Android:

* **UI Framework:** Jetpack Compose (Declarative UI).
* **Navigation:** Jetpack Compose Navigation dengan mekanisme *Path Arguments* dan *SavedStateHandle*.
* **Backend / Database:** Supabase (Postgrest, Auth, Storage, Realtime).
* **Networking:** Retrofit & OkHttp untuk komunikasi REST API.
* **Machine Learning:** Google ML Kit Text Recognition untuk fitur scan struk.
* **DI & State Management:** ViewModel dengan pola *Shared ViewModel* untuk sinkronisasi data antar halaman.

---

## 📁 Struktur Navigasi

Navigasi antar halaman diatur dalam `AppNavHost.kt` dengan alur sebagai berikut:

1. **Auth Flow:** Login -> Register.
2. **Main Flow:** Home -> Transaction List -> Insight -> Goal Tracker.
3. **Action Flow:** Add Transaction -> Camera Scanner (Sensor) -> Detail Goal.

---

## 📦 Instalasi & Konfigurasi

### Prasyarat

* Android Studio Ladybug atau versi lebih baru.
* JDK 11 atau versi lebih baru.
* Min SDK 24 / Target SDK 36.

### Library Utama (Dependencies)

Tambahkan dependensi berikut jika Anda ingin mengembangkan ulang:

```kotlin
dependencies {
  // Feature Dependencies (OCR, Camera, Retrofit)
    implementation(libs.androidx.navigation.compose)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.compose.material:material-icons-extended")
    
    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ML Kit Text Recognition
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    // HEAD Dependencies (Supabase, Auth, etc)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt") 
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // Ktor Client (Engine untuk Supabase)
    implementation("io.ktor:ktor-client-android:3.0.0")

    // Serialization (Combined, use newer 1.6.3 from HEAD)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --- 2. CREDENTIAL MANAGER (Untuk Google Login) ---
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
    // implementation("androidx.navigation:navigation-compose:2.8.0") // Already included via libs.androidx.navigation.compose

    // TAMBAHKAN INI UNTUK IKON LENGKAP
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

}

```

---

## 📷 Penggunaan Sensor

Aplikasi ini menggunakan sensor **Kamera** sebagai alat input data otomatis.

1. Buka halaman **Add Transaction**.
2. Klik ikon kamera untuk mengaktifkan **CameraX**.
3. Arahkan pada struk; **ML Kit** akan mengekstraksi nominal angka.
4. Data hasil scan dikirim kembali ke formulir input menggunakan `SavedStateHandle`.

---

## 🤝 Kontribusi

Kontribusi selalu terbuka! Silakan lakukan *fork* pada repositori ini dan ajukan *pull request* untuk fitur-fitur baru.

## 📄 Lisensi

Distribusi di bawah Lisensi MIT. Lihat `LICENSE` untuk informasi lebih lanjut.

---

**Finote** - *Managing finance, simplified.*

---

**Langkah selanjutnya yang bisa saya lakukan:**
Apakah Anda ingin saya membuatkan bagian **"Cara Kerja API"** yang lebih detail untuk ditambahkan ke README ini?
