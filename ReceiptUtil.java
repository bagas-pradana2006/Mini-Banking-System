package util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class ReceiptUtil {

    public static void saveReceipt(
            String type,
            double amount,
            double balance
    ) {

        try {

            FileWriter writer =
                    new FileWriter(
                            "C:/Coding/BankSystem/struk.txt",
                            true
                    );
            writer.write("===== STRUK ATM =====\n");

            writer.write(
                    "Jenis Transaksi : "
                            + type + "\n"
            );

            writer.write(
                    "Jumlah          : Rp "
                            + amount + "\n"
            );

            writer.write(
                    "Saldo Sekarang  : Rp "
                            + balance + "\n"
            );

            writer.write(
                    "Waktu           : "
                            + LocalDateTime.now()
                            + "\n"
            );

            writer.write(
                    "========================\n\n"
            );

            writer.close();

        } catch (IOException e) {

            System.out.println(
                    "Gagal menyimpan struk!"
            );
        }
    }
}