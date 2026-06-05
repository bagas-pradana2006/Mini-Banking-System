package model;

public class Account {
    private String accountNumber;
    private String name;
    private String pinHash; // Simpan PIN acak
    private double balance;
    private String status;  // ACTIVE atau FROZEN

    public Account(String accountNumber, String name, String pinHash, double balance, String status) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.pinHash = pinHash;
        this.balance = balance;
        this.status = status;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public String getPinHash() { return pinHash; }
    public double getBalance() { return balance; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public synchronized void addBalance(double amount) {
        this.balance += amount;
    }

    public synchronized void subtractBalance(double amount) {
        this.balance -= amount;
    }
}