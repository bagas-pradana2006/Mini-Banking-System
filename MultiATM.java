import javax.swing.*;

public class MultiATM {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // ATM 1
            ATM_GUI atm1 = new ATM_GUI();
            atm1.setTitle("ATM 1");
            atm1.setLocation(100, 100);
            atm1.setVisible(true);

            // ATM 2
            ATM_GUI atm2 = new ATM_GUI();
            atm2.setTitle("ATM 2");
            atm2.setLocation(550, 100);
            atm2.setVisible(true);

            // ATM 3
            ATM_GUI atm3 = new ATM_GUI();
            atm3.setTitle("ATM 3");
            atm3.setLocation(1000, 100);
            atm3.setVisible(true);

        });
    }
}