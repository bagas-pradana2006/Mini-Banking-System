# 🏧 Mini Banking System

![JAVA](https://img.shields.io/badge/JAVA-orange?style=for-the-badge)
![GUI](https://img.shields.io/badge/GUI-blue?style=for-the-badge)
![SQL SERVER](https://img.shields.io/badge/SQL%20SERVER-red?style=for-the-badge)
![OOP](https://img.shields.io/badge/OOP-purple?style=for-the-badge)
![JDBC](https://img.shields.io/badge/JDBC-green?style=for-the-badge)
![STATUS](https://img.shields.io/badge/STATUS-COMPLETED-brightgreen?style=for-the-badge)

Aplikasi desktop berbasis Java yang mensimulasikan sistem ATM dan panel admin bank sederhana, dilengkapi dengan keamanan PIN hashing, session timeout otomatis, dan manajemen transaksi berbasis ACID.

---

## ✨ Fitur

### 🏧 User ATM
- 🔐 **Login ATM** — Autentikasi menggunakan nomor rekening dan PIN dengan SHA-256 hashing
- 🚫 **Auto Block PIN 3x** — Akun otomatis dibekukan setelah 3x salah PIN
- 💰 **Cek Saldo** — Melihat saldo rekening secara realtime dari database
- 💵 **Setor Tunai** — Menambahkan saldo dengan histori transaksi dan cetak struk otomatis
- 💸 **Tarik Tunai** — Menarik saldo dengan validasi dan mutasi transaksi
- 🔄 **Transfer Antar Rekening** — Transfer uang menggunakan ACID Transaction dengan rollback otomatis
- 📄 **Mutasi Rekening** — Histori lengkap transaksi (transfer, withdraw, deposit)
- 🔑 **Ganti PIN** — Mengganti PIN dengan validasi dan hashing PIN baru
- 🕒 **Session Timeout** — Auto logout otomatis setelah 30 detik tidak aktif
- 🧾 **Cetak Struk TXT** — Generate file `struk.txt` setiap transaksi berhasil

### 👨‍💼 Admin Panel
- 📋 **Lihat Semua Nasabah** — Tampilkan data nasabah lengkap menggunakan JTable
- 🚫 **Block / Unblock Rekening** — Memblokir atau membuka blokir rekening nasabah
- 👤 **Tambah Nasabah** — Membuat rekening baru dengan nomor rekening, nama, PIN, dan saldo awal
- 📊 **Statistik Bank** — Total nasabah, rekening aktif, rekening diblokir, dan total uang bank

---

## 🗂️ Struktur Project

```
MiniBankingSystem/
└── src/
    ├── model/
    │   ├── Account.java          → Model data rekening nasabah
    │   └── Transaction.java      → Model data transaksi
    ├── repository/
    │   ├── AccountRepository.java    → CRUD operasi rekening
    │   └── TransactionRepository.java → CRUD operasi transaksi
    ├── util/
    │   ├── KoneksiDB.java        → Pusat koneksi database
    │   ├── HashUtil.java         → SHA-256 PIN hashing
    │   └── StrukUtil.java        → Generate file struk.txt
    ├── session/
    │   └── SessionManager.java   → Timer & session timeout
    ├── form/
    │   ├── Form_Login.java       → Halaman login ATM
    │   ├── Form_ATM.java         → Menu utama ATM
    │   ├── Form_Transfer.java    → Form transfer antar rekening
    │   ├── Form_Mutasi.java      → Histori transaksi
    │   └── Form_AdminPanel.java  → Dashboard admin
    └── Main.java                 → Entry point aplikasi
```

---

## 🗄️ Struktur Database

```
MiniBankingDB
├── accounts
│   ├── id_rekening   → Nomor rekening unik
│   ├── nama          → Nama nasabah
│   ├── pin_hash      → PIN terenkripsi SHA-256
│   ├── saldo         → Saldo rekening
│   └── status        → ACTIVE / FROZEN
│
└── transactions
    ├── id_transaksi  → ID transaksi unik
    ├── id_rekening   → Referensi ke rekening
    ├── jenis         → DEPOSIT / WITHDRAW / TRANSFER
    ├── nominal       → Jumlah transaksi
    ├── saldo_akhir   → Saldo setelah transaksi
    └── timestamp     → Waktu transaksi
```

---

## 🧠 Materi Java yang Digunakan

| Kategori | Materi |
|----------|--------|
| **Fundamental** | Class, Object, Constructor, Encapsulation |
| **OOP** | Inheritance, Abstraction, Polymorphism, Interface |
| **Advanced** | JDBC, File I/O, Timer & TimerTask, Exception Handling |
| **Security** | SHA-256 Hashing, Session Management |
| **Architecture** | SOLID Principle, Repository Pattern, Utility Class |
| **Database** | SQL Server, ACID Transaction, CRUD, Aggregate Query |

---

## ⚙️ Teknologi yang Digunakan

| Teknologi | Fungsi |
|-----------|--------|
| Java Swing | GUI ATM & Admin Panel |
| JDBC | Koneksi ke SQL Server |
| SQL Server | Penyimpanan data nasabah & transaksi |
| SHA-256 | Enkripsi/hashing PIN |
| Timer & TimerTask | Session timeout otomatis |
| FileWriter | Generate struk transaksi .txt |
| JTable | Tampilan data admin dashboard |

---

## 🔄 Alur Aplikasi

```
[Main.java]
     │
     ├── Role: Nasabah ──► [Form_Login]
     │                          └── [Form_ATM]
     │                                ├── Cek Saldo
     │                                ├── Setor Tunai
     │                                ├── Tarik Tunai
     │                                ├── Transfer
     │                                ├── Mutasi Rekening
     │                                └── Ganti PIN
     │
     └── Role: Admin ──► [Form_AdminPanel]
                               ├── Lihat Semua Nasabah
                               ├── Block / Unblock Rekening
                               ├── Tambah Nasabah
                               └── Statistik Bank
```

---

## 🧵 Fitur Concurrency

**Session Timer** — Background timer menggunakan `Timer` dan `TimerTask` untuk auto logout setelah 30 detik tidak aktif.

**Multi ATM Simulation** — Mendukung pembukaan beberapa window ATM secara bersamaan dengan shared database dan transaksi realtime.

---

## 🚀 Cara Menjalankan

**Persyaratan:**
- Java JDK 11 atau lebih baru
- SQL Server & SSMS
- IDE NetBeans / IntelliJ / Eclipse

**Langkah:**
1. Buat database `MiniBankingDB` di SQL Server
2. Jalankan script SQL untuk membuat tabel `accounts` dan `transactions`
3. Sesuaikan koneksi di `KoneksiDB.java`
4. Jalankan `Main.java`

---

## 🚀 Pengembangan Selanjutnya

- JavaFX UI yang lebih modern
- Export struk ke format PDF
- Grafik statistik transaksi
- Login khusus admin
- Stress test concurrency
- Simulasi mobile banking
- Integrasi REST API

---

## 👤 Identitas

**Nama:** Ibrahim Bagas Pradana  
**NIM:** 255150207111046  
**Program Studi:** Teknik Informatika  
**Universitas Brawijaya**  

---

## 📚 Referensi

- Oracle Java Documentation — Java Swing & JDBC
- Oracle Java Documentation — Concurrency (Timer & TimerTask)
- Schildt, H. (2018). *Java: The Complete Reference*. McGraw-Hill Education.
- Microsoft SQL Server Documentation
