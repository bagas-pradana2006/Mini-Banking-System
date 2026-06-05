package repository;

import model.Account;
import util.SecurityUtil;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAccountRepo implements AccountRepository {

    @Override
    public void save(Account account) {
        // (Tetap sama seperti sebelumnya, untuk V3 kita fokus ke tarik/transfer dulu)
    }

    @Override
    public Account findById(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM accounts WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, accountNumber);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Sekarang membaca 5 kolom: id, name, pin, balance, status
                return new Account(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("pin"),
                        rs.getDouble("balance"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.out.println("DB Error (Find): " + e.getMessage());
        }
        return null;
    }

    // ==========================================
    // FITUR BARU: LOGIN MESIN ATM
    // ==========================================
    public Account authenticate(String id, String inputPin) {
        Account account = findById(id);

        // 1. Cek apakah rekening ada
        if (account == null) {
            throw new RuntimeException("Rekening tidak ditemukan!");
        }
        // 2. Cek apakah rekening diblokir
        if (account.getStatus().equals("FROZEN")) {
            throw new RuntimeException("Kartu Anda diblokir! Hubungi Customer Service.");
        }

        // 3. Enkripsi PIN inputan, lalu cocokkan dengan PIN di Database
        String hashedInput = SecurityUtil.hashPIN(inputPin);
        if (!account.getPinHash().equals(hashedInput)) {
            throw new RuntimeException("PIN SALAH!");
        }

        return account; // Jika sukses, kembalikan data akun
    }

    // (Method transferWithACID biarkan tetap ada di bawah sini untuk nanti kita pakai)
    // ==========================================
    // UPDATE: TRANSFER DENGAN PENCATATAN MUTASI
    // ==========================================
    public boolean transferWithACID(String fromId, String toId, double amount) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Cek saldo pengirim dulu biar aman
            Account fromAcc = findById(fromId);
            if (fromAcc.getBalance() < amount) throw new SQLException("Saldo tidak cukup!");

            Account toAcc = findById(toId);
            if (toAcc == null) throw new SQLException("Rekening tujuan tidak ditemukan!");

            // 1. Kurangi Saldo Pengirim
            String sqlMinus = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            PreparedStatement pstMinus = conn.prepareStatement(sqlMinus);
            pstMinus.setDouble(1, amount);
            pstMinus.setString(2, fromId);
            pstMinus.executeUpdate();

            // Catat Mutasi Pengirim
            recordTransaction(conn, fromId, "TRANSFER_OUT", amount, fromAcc.getBalance() - amount);

            // 2. Tambah Saldo Penerima
            String sqlPlus = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            PreparedStatement pstPlus = conn.prepareStatement(sqlPlus);
            pstPlus.setDouble(1, amount);
            pstPlus.setString(2, toId);
            pstPlus.executeUpdate();

            // Catat Mutasi Penerima
            recordTransaction(conn, toId, "TRANSFER_IN", amount, toAcc.getBalance() + amount);

            // 3. COMMIT SEMUA JIKA SUKSES
            conn.commit();
            return true;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException(e.getMessage()); // Lempar error ke GUI biar dibaca user
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }
    // ==========================================
    // FITUR BARU: TARIK TUNAI
    // ==========================================
    public boolean withdraw(String accId, double amount) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Account acc = findById(accId);
            if (acc.getBalance() < amount) throw new SQLException("Saldo tidak cukup!");

            String sql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setDouble(1, amount);
            pst.setString(2, accId);
            pst.executeUpdate();

            // Catat Struk
            recordTransaction(conn, accId, "WITHDRAW", amount, acc.getBalance() - amount);

            conn.commit();
            return true;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException(e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }
    public String getTransactionHistory(String accountId) {
        StringBuilder history = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection()) {

            String sql = """
                SELECT * FROM transactions
                WHERE account_id = ?
                ORDER BY timestamp DESC
                """;

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, accountId);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                history.append(
                        "Jenis: " + rs.getString("type")
                ).append("\n");

                history.append(
                        "Jumlah: " +
                                String.format(
                                        "Rp %,.0f",
                                        rs.getDouble("amount")
                                )
                ).append("\n");

                history.append(
                        "Saldo Akhir: " +
                                String.format(
                                        "Rp %,.0f",
                                        rs.getDouble("balance_after")
                                )
                ).append("\n");

                history.append(
                        "Waktu: " + rs.getTimestamp("timestamp")
                ).append("\n");

                history.append(
                        "========================\n"
                );
            }

        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }

        if (history.length() == 0) {
            return "Belum ada transaksi.";
        }

        return history.toString();
    }

    public void freezeAccount(String accountId) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            String sql = """
                UPDATE accounts
                SET status = 'FROZEN'
                WHERE id = ?
                """;

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1, accountId);

            pst.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Gagal memblokir akun!"
            );
        }
    }
    public void changePin(
            String accountId,
            String newPin
    ) {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql = """
                UPDATE accounts
                SET pin = ?
                WHERE id = ?
                """;

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            // HASH PIN BARU
            String hashedPin =
                    util.SecurityUtil.hashPIN(newPin);

            pst.setString(1, hashedPin);
            pst.setString(2, accountId);

            pst.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Gagal mengubah PIN!"
            );
        }
    }
    public boolean deposit(
            String accId,
            double amount
    ) {

        Connection conn = null;

        try {

            conn = DatabaseConnection.getConnection();

            conn.setAutoCommit(false);

            String sql = """
                UPDATE accounts
                SET balance = balance + ?
                WHERE id = ?
                """;

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setDouble(1, amount);
            pst.setString(2, accId);

            pst.executeUpdate();

            // Ambil saldo terbaru
            Account acc = findById(accId);

            // Catat mutasi
            recordTransaction(
                    conn,
                    accId,
                    "DEPOSIT",
                    amount,
                    acc.getBalance()
            );

            conn.commit();

            return true;

        } catch (SQLException e) {

            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}

            throw new RuntimeException(
                    "Gagal setor tunai!"
            );

        } finally {

            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    }

    public List<Account> getAllAccounts() {

        List<Account> accounts =
                new ArrayList<>();

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT * FROM accounts";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    pst.executeQuery();

            while (rs.next()) {

                accounts.add(
                        new Account(
                                rs.getString("id"),
                                rs.getString("name"),
                                rs.getString("pin"),
                                rs.getDouble("balance"),
                                rs.getString("status")
                        )
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    e.getMessage()
            );
        }

        return accounts;
    }

    public void unfreezeAccount(
            String accountId
    ) {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql = """
                UPDATE accounts
                SET status = 'ACTIVE'
                WHERE id = ?
                """;

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1, accountId);

            pst.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Gagal membuka blokir akun!"
            );
        }
    }

    public double getTotalBankBalance() {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT SUM(balance) as total FROM accounts";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    pst.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    e.getMessage()
            );
        }

        return 0;
    }

    public void createNewAccount(
            String id,
            String name,
            String pin,
            double balance
    ) {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql = """
                INSERT INTO accounts
                (id, name, pin, balance, status)
                VALUES (?, ?, ?, ?, 'ACTIVE')
                """;

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            // HASH PIN
            String hashedPin =
                    util.SecurityUtil.hashPIN(pin);

            pst.setString(1, id);
            pst.setString(2, name);
            pst.setString(3, hashedPin);
            pst.setDouble(4, balance);

            pst.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Gagal menambah nasabah!"
            );
        }
    }

    public int getTotalAccounts() {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT COUNT(*) as total FROM accounts";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    pst.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return 0;
    }

    public int getActiveAccounts() {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql = """
                SELECT COUNT(*) as total
                FROM accounts
                WHERE status = 'ACTIVE'
                """;

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    pst.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return 0;
    }

    public int getFrozenAccounts() {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql = """
                SELECT COUNT(*) as total
                FROM accounts
                WHERE status = 'FROZEN'
                """;

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    pst.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return 0;
    }

    public String getTodayTransactions(
            String accountId
    ) {

        StringBuilder history =
                new StringBuilder();

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql = """
                SELECT *
                FROM transactions
                WHERE account_id = ?
                AND CAST(timestamp AS DATE)
                    = CAST(GETDATE() AS DATE)
                ORDER BY timestamp DESC
                """;

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1, accountId);

            ResultSet rs =
                    pst.executeQuery();

            while (rs.next()) {

                history.append(
                        rs.getString("type")
                );

                history.append(" | ");

                history.append(
                        String.format(
                                "Rp %,.0f",
                                rs.getDouble("amount")
                        )
                );

                history.append("\n");
            }

        } catch (SQLException e) {

            return e.getMessage();
        }

        if (history.length() == 0) {

            return "Belum ada transaksi hari ini.";
        }

        return history.toString();
    }

    // ==========================================
    // FITUR BARU: MENCATAT MUTASI REKENING (STRUK)
    // ==========================================
    public void recordTransaction(Connection conn, String accId, String type, double amount, double balAfter) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount, balance_after) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, accId);
        pst.setString(2, type);
        pst.setDouble(3, amount);
        pst.setDouble(4, balAfter);
        pst.executeUpdate();
    }
}