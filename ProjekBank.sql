CREATE DATABASE BankSystem;
GO

USE BankSystem;
GO

-- 1. Hapus Tabel Lama dengan cara tradisional (Dijamin berhasil di semua versi)
IF OBJECT_ID('transactions', 'U') IS NOT NULL DROP TABLE transactions;
IF OBJECT_ID('accounts', 'U') IS NOT NULL DROP TABLE accounts;
GO

-- 2. Buat Tabel Akun Baru (Ada PIN dan Status)
CREATE TABLE accounts (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50),
    pin VARCHAR(64), 
    balance DECIMAL(15, 2),
    status VARCHAR(20) DEFAULT 'ACTIVE' 
);
GO

-- 3. Buat Tabel Mutasi / Riwayat Transaksi
CREATE TABLE transactions (
    id INT IDENTITY(1,1) PRIMARY KEY, 
    account_id VARCHAR(20) FOREIGN KEY REFERENCES accounts(id),
    type VARCHAR(20), 
    amount DECIMAL(15, 2),
    balance_after DECIMAL(15, 2),
    timestamp DATETIME DEFAULT GETDATE() 
);
GO

-- 4. Masukkan Data Nasabah (PIN: 123456)
INSERT INTO accounts (id, name, pin, balance, status) 
VALUES ('123', 'Bagas', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 500000, 'ACTIVE');

INSERT INTO accounts (id, name, pin, balance, status) 
VALUES ('456', 'Aca', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1000000, 'ACTIVE');
GO

SELECT * FROM accounts

UPDATE accounts
SET status = 'ACTIVE'
WHERE id = '123';