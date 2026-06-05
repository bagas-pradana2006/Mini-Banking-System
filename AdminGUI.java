import model.Account;
import repository.DatabaseAccountRepo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminGUI extends JFrame {

    private DatabaseAccountRepo repo =
            new DatabaseAccountRepo();

    private JTable table;
    private DefaultTableModel model;

    public AdminGUI() {

        setTitle("ADMIN PANEL BANK");
        setSize(700, 400);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLayout(new BorderLayout());

        // ==========================
        // TABLE
        // ==========================
        String[] columns = {
                "No Rekening",
                "Nama",
                "Saldo",
                "Status"
        };

        model = new DefaultTableModel(
                columns,
                0
        );

        table = new JTable(model);

        JScrollPane scrollPane =
                new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);

        // ==========================
        // BUTTON PANEL
        // ==========================
        JPanel panelButton = new JPanel();

        JButton btnRefresh =
                new JButton("REFRESH");

        JButton btnFreeze =
                new JButton("BLOCK");

        JButton btnUnfreeze =
                new JButton("UNBLOCK");

        JButton btnTotal =
                new JButton("TOTAL UANG BANK");
        JButton btnTambah =
                new JButton("TAMBAH NASABAH");
        JButton btnStats =
                new JButton("STATISTIK");

        panelButton.add(btnRefresh);
        panelButton.add(btnFreeze);
        panelButton.add(btnUnfreeze);
        panelButton.add(btnTotal);
        panelButton.add(btnTambah);
        panelButton.add(btnStats);

        add(panelButton, BorderLayout.SOUTH);

        // ==========================
        // ACTION REFRESH
        // ==========================
        btnRefresh.addActionListener(e -> {
            loadAccounts();
        });

        // ==========================
        // ACTION BLOCK
        // ==========================
        btnFreeze.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        null,
                        "Pilih rekening dulu!"
                );
                return;
            }

            String accId =
                    model.getValueAt(row, 0)
                            .toString();

            repo.freezeAccount(accId);

            JOptionPane.showMessageDialog(
                    null,
                    "Rekening diblokir!"
            );

            loadAccounts();
        });

        // ==========================
        // ACTION UNBLOCK
        // ==========================
        btnUnfreeze.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        null,
                        "Pilih rekening dulu!"
                );
                return;
            }

            String accId =
                    model.getValueAt(row, 0)
                            .toString();

            repo.unfreezeAccount(accId);

            JOptionPane.showMessageDialog(
                    null,
                    "Rekening dibuka!"
            );

            loadAccounts();
        });

        // ==========================
        // ACTION TOTAL UANG
        // ==========================
        btnTotal.addActionListener(e -> {

            double total =
                    repo.getTotalBankBalance();

            JOptionPane.showMessageDialog(
                    null,
                    "Total uang bank:\n"
                            + String.format(
                            "Rp %,.0f",
                            total
                    )
            );
        });

        // ==========================
// ACTION TAMBAH NASABAH
// ==========================
        btnTambah.addActionListener(e -> {

            try {

                // INPUT DATA
                String id =
                        JOptionPane.showInputDialog(
                                "Nomor rekening:"
                        );

                if (id == null) return;

                String name =
                        JOptionPane.showInputDialog(
                                "Nama nasabah:"
                        );

                if (name == null) return;

                String pin =
                        JOptionPane.showInputDialog(
                                "PIN awal (6 digit):"
                        );

                if (pin == null) return;

                String saldoInput =
                        JOptionPane.showInputDialog(
                                "Saldo awal:"
                        );

                if (saldoInput == null) return;

                double saldo =
                        Double.parseDouble(saldoInput);

                // VALIDASI PIN
                if (pin.length() != 6) {

                    JOptionPane.showMessageDialog(
                            null,
                            "PIN harus 6 digit!"
                    );

                    return;
                }

                // SIMPAN KE DATABASE
                repo.createNewAccount(
                        id,
                        name,
                        pin,
                        saldo
                );

                JOptionPane.showMessageDialog(
                        null,
                        "Nasabah berhasil ditambahkan!"
                );

                // REFRESH TABLE
                loadAccounts();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // ==========================
// ACTION STATISTIK
// ==========================
        btnStats.addActionListener(e -> {

            int totalAccounts =
                    repo.getTotalAccounts();

            int activeAccounts =
                    repo.getActiveAccounts();

            int frozenAccounts =
                    repo.getFrozenAccounts();

            double totalBalance =
                    repo.getTotalBankBalance();

            String message =
                    """
                    ===== STATISTIK BANK =====
        
                    Total Nasabah : %d
                    Rekening Aktif : %d
                    Rekening Diblokir : %d
        
                    Total Uang Bank :
                    %s
                    """
                            .formatted(
                                    totalAccounts,
                                    activeAccounts,
                                    frozenAccounts,
                                    String.format(
                                            "Rp %,.0f",
                                            totalBalance
                                    )
                            );

            JOptionPane.showMessageDialog(
                    null,
                    message
            );
        });

        // LOAD DATA AWAL
        loadAccounts();
    }

    // ==================================
    // LOAD TABLE DATA
    // ==================================
    private void loadAccounts() {

        model.setRowCount(0);

        List<Account> accounts =
                repo.getAllAccounts();

        for (Account acc : accounts) {

            model.addRow(new Object[]{

                    acc.getAccountNumber(),
                    acc.getName(),

                    String.format(
                            "Rp %,.0f",
                            acc.getBalance()
                    ),

                    acc.getStatus()
            });
        }
    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new AdminGUI().setVisible(true);

        });
    }
}