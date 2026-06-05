package util;

import java.security.MessageDigest;

public class SecurityUtil {
    // Fungsi untuk mengacak PIN menggunakan SHA-256 (Standar Keamanan Bank)
    public static String hashPIN(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error saat enkripsi PIN!", e);
        }
    }
}