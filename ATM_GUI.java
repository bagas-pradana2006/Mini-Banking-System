import model.Account;
import repository.DatabaseAccountRepo;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ATM_GUI extends JFrame {
    private DatabaseAccountRepo repo = new DatabaseAccountRepo();
    private Account loggedInAccount = null; // Menyimpan memori siapa yang sedang login
    private int failedAttempts = 0;
    private Timer sessionTimer;
    private boolean sessionActive = false;

    // Komponen Layar Login
    private JPanel panelLogin;
    private JTextField txtAccountId;
    private JPasswordField txtPin;

    // Komponen Layar Menu Utama
    private JPanel panelMenu;
    private JLabel lblWelcome;



    public ATM_GUI() {
        setTitle("Mesin ATM BankSystem");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Di tengah layar
        setLayout(new CardLayout()); // Memungkinkan kita ganti-ganti layar (Card)

        initLoginScreen();
        initMenuScreen();

        // Tambahkan kedua layar ke dalam Frame
        add(panelLogin, "LOGIN");
        add(panelMenu, "MENU");

        showScreen("LOGIN"); // Layar pertama yang muncul
    }

    private void initLoginScreen() {
        panelLogin = new JPanel(new GridLayout(6, 1, 10, 10));
        panelLogin.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        panelLogin.setBackground(new Color(0, 102, 204)); // Warna Biru Bank BCA/Mandiri

        JLabel title = new JLabel("SELAMAT DATANG", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        txtAccountId = new JTextField();
        txtAccountId.setBorder(BorderFactory.createTitledBorder("Nomor Rekening"));

        txtPin = new JPasswordField();
        txtPin.setBorder(BorderFactory.createTitledBorder("PIN (6 Digit)"));

        JButton btnLogin = new JButton("MASUK / ENTER");
        btnLogin.setBackground(Color.GREEN);
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    String id = txtAccountId.getText();
                    String pin = new String(txtPin.getPassword());

                    // LOGIN KE DATABASE
                    loggedInAccount = repo.authenticate(id, pin);

                    // RESET jika login berhasil
                    failedAttempts = 0;

                    // PIN BENAR -> MASUK MENU
                    lblWelcome.setText(
                            "Halo, " + loggedInAccount.getName() + "!"
                    );

                    txtAccountId.setText("");
                    txtPin.setText("");

                    sessionActive = true;
                    showScreen("MENU");
                    resetSessionTimer();

                } catch (Exception ex) {
                    if (ex.getMessage().contains("diblokir")) {

                        JOptionPane.showMessageDialog(
                                null,
                                ex.getMessage(),
                                "KARTU DIBLOKIR",
                                JOptionPane.ERROR_MESSAGE
                        );

                        return;
                    }
                    failedAttempts++;

                    // ==========================
                    // JIKA GAGAL 3x
                    // ==========================
                    if (failedAttempts >= 3) {

                        try {

                            repo.freezeAccount(
                                    txtAccountId.getText()
                            );

                            JOptionPane.showMessageDialog(
                                    null,
                                    "PIN salah 3x!\nKartu diblokir!",
                                    "KARTU DIBLOKIR",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } catch (Exception freezeEx) {

                            JOptionPane.showMessageDialog(
                                    null,
                                    freezeEx.getMessage(),
                                    "ERROR",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }

                        // RESET COUNTER
                        failedAttempts = 0;

                    } else {

                        JOptionPane.showMessageDialog(
                                null,
                                ex.getMessage()
                                        + "\nSisa percobaan: "
                                        + (3 - failedAttempts),
                                "LOGIN GAGAL",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });

        panelLogin.add(title);
        panelLogin.add(txtAccountId);
        panelLogin.add(txtPin);
        panelLogin.add(new JLabel("")); // Spasi kosong
        panelLogin.add(btnLogin);
    }

    private void initMenuScreen() {
        panelMenu = new JPanel(new GridLayout(6, 1, 10, 10));
        panelMenu.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panelMenu.setBackground(new Color(230, 240, 255));

        lblWelcome = new JLabel("Halo, User!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));

        JButton btnCekSaldo = new JButton("CEK SALDO");
        JButton btnTarik = new JButton("TARIK TUNAI");
        JButton btnSetor = new JButton("SETOR TUNAI");
        JButton btnTransfer = new JButton("TRANSFER");
        JButton btnMutasi = new JButton("MUTASI REKENING");
        JButton btnGantiPin = new JButton("GANTI PIN");
        JButton btnToday =
                new JButton("TRANSAKSI HARI INI");
        JButton btnLogout = new JButton("KELUAR / CANCEL");

        btnLogout.setBackground(Color.RED);
        btnLogout.setForeground(Color.WHITE);

        // Aksi Tombol Cek Saldo
        btnCekSaldo.addActionListener(e -> {
            resetSessionTimer();
            // Kita ambil data terbaru dari database
            Account freshData = repo.findById(loggedInAccount.getAccountNumber());
            JOptionPane.showMessageDialog(null, "Saldo Anda saat ini:\nRp " + freshData.getBalance(), "INFORMASI SALDO", JOptionPane.INFORMATION_MESSAGE);
        });
        // ==========================================
// AKSI TOMBOL TARIK TUNAI
// ==========================================
        btnTarik.addActionListener(e -> {
            resetSessionTimer();
            try {

                String input = JOptionPane.showInputDialog(
                        "Masukkan jumlah tarik tunai:"
                );

                if (input == null) return;

                double amount = Double.parseDouble(input);

                boolean success = repo.withdraw(
                        loggedInAccount.getAccountNumber(),
                        amount
                );

                if (success) {

                    // Ambil data saldo terbaru
                    Account freshData = repo.findById(
                            loggedInAccount.getAccountNumber()
                    );

                    // Simpan struk TXT
                    util.ReceiptUtil.saveReceipt(
                            "WITHDRAW",
                            amount,
                            freshData.getBalance()
                    );

                    JOptionPane.showMessageDialog(
                            null,
                            "Tarik tunai berhasil!"
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // ==========================================
// AKSI TOMBOL SETOR TUNAI
// ==========================================
        btnSetor.addActionListener(e -> {

            try {

                // RESET TIMER SESSION
                resetSessionTimer();

                String input = JOptionPane.showInputDialog(
                        "Masukkan jumlah setor tunai:"
                );

                if (input == null) return;

                double amount = Double.parseDouble(input);

                boolean success = repo.deposit(
                        loggedInAccount.getAccountNumber(),
                        amount
                );

                if (success) {

                    Account freshData = repo.findById(
                            loggedInAccount.getAccountNumber()
                    );

                    util.ReceiptUtil.saveReceipt(
                            "DEPOSIT",
                            amount,
                            freshData.getBalance()
                    );

                    JOptionPane.showMessageDialog(
                            null,
                            "Setor tunai berhasil!"
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });


// ==========================================
// AKSI TOMBOL TRANSFER
// ==========================================
        btnTransfer.addActionListener(e -> {
            resetSessionTimer();
            try {
                String targetId = JOptionPane.showInputDialog("Masukkan rekening tujuan:");

                if (targetId == null) return;

                String inputAmount = JOptionPane.showInputDialog("Masukkan jumlah transfer:");

                if (inputAmount == null) return;

                double amount = Double.parseDouble(inputAmount);

                boolean success = repo.transferWithACID(
                        loggedInAccount.getAccountNumber(),
                        targetId,
                        amount
                );

                if (success) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Transfer berhasil!"
                    );
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });


// ==========================================
// AKSI TOMBOL MUTASI REKENING
// ==========================================
        btnMutasi.addActionListener(e -> {
            resetSessionTimer();

            String history = repo.getTransactionHistory(
                    loggedInAccount.getAccountNumber()
            );

            JTextArea textArea = new JTextArea(history);
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(
                    null,
                    scrollPane,
                    "MUTASI REKENING",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        // Aksi Tombol Keluar
        btnLogout.addActionListener(e -> {

            if (sessionTimer != null) {
                sessionTimer.cancel();
            }

            loggedInAccount = null;

            showScreen("LOGIN");
        });

        btnGantiPin.addActionListener(e -> {
            resetSessionTimer();
            try {

                // INPUT PIN LAMA
                String oldPin = JOptionPane.showInputDialog(
                        "Masukkan PIN lama:"
                );

                if (oldPin == null) return;

                // VALIDASI PIN LAMA
                repo.authenticate(
                        loggedInAccount.getAccountNumber(),
                        oldPin
                );

                // INPUT PIN BARU
                String newPin = JOptionPane.showInputDialog(
                        "Masukkan PIN baru:"
                );

                if (newPin == null) return;

                // KONFIRMASI PIN
                String confirmPin = JOptionPane.showInputDialog(
                        "Konfirmasi PIN baru:"
                );

                if (confirmPin == null) return;

                // VALIDASI KONFIRMASI
                if (!newPin.equals(confirmPin)) {

                    JOptionPane.showMessageDialog(
                            null,
                            "Konfirmasi PIN tidak cocok!"
                    );

                    return;
                }

                // VALIDASI 6 DIGIT
                if (newPin.length() != 6) {

                    JOptionPane.showMessageDialog(
                            null,
                            "PIN harus 6 digit!"
                    );

                    return;
                }

                // UPDATE DATABASE
                repo.changePin(
                        loggedInAccount.getAccountNumber(),
                        newPin
                );

                JOptionPane.showMessageDialog(
                        null,
                        "PIN berhasil diubah!"
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // ==========================================
// TRANSAKSI HARI INI
// ==========================================
        btnToday.addActionListener(e -> {

            resetSessionTimer();

            String history =
                    repo.getTodayTransactions(
                            loggedInAccount.getAccountNumber()
                    );

            JTextArea textArea =
                    new JTextArea(history);

            textArea.setEditable(false);

            JScrollPane scrollPane =
                    new JScrollPane(textArea);

            scrollPane.setPreferredSize(
                    new Dimension(400, 300)
            );

            JOptionPane.showMessageDialog(
                    null,
                    scrollPane,
                    "TRANSAKSI HARI INI",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });


        panelMenu.add(lblWelcome);
        panelMenu.add(btnCekSaldo);
        panelMenu.add(btnTarik);
        panelMenu.add(btnSetor);
        panelMenu.add(btnTransfer);
        panelMenu.add(btnMutasi);
        panelMenu.add(btnToday);
        panelMenu.add(btnGantiPin);
        panelMenu.add(btnLogout);
    }

    private void showScreen(String screenName) {
        CardLayout cl = (CardLayout) (getContentPane().getLayout());
        cl.show(getContentPane(), screenName);
    }
    private void resetSessionTimer() {

        // Matikan timer lama
        if (sessionTimer != null) {

            sessionTimer.cancel();
            sessionTimer.purge();
        }

        // Buat timer baru
        sessionTimer = new Timer(true);

        sessionTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                SwingUtilities.invokeLater(() -> {

                    // CEK SESSION MASIH AKTIF?
                    if (!sessionActive) {
                        return;
                    }

                    sessionActive = false;

                    loggedInAccount = null;

                    JOptionPane.showMessageDialog(
                            null,
                            "Session habis!\nSilakan login kembali."
                    );

                    showScreen("LOGIN");
                });
            }

        }, 30000);
    }


    public static void main(String[] args) {
        // Run Aplikasi ATM
        SwingUtilities.invokeLater(() -> {
            new ATM_GUI().setVisible(true);
        });
    }
}