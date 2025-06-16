package com.master.verificamtc.utils;

import org.mindrot.jbcrypt.BCrypt;

public class SecurityHelper {
    // Aumenta el coste para mayor seguridad (rango 10-31)
    private static final int BCRYPT_COST = 12;
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty() ||
                hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isPasswordStrong(String password) {
        // Mínimo 8 caracteres, al menos: 1 mayúscula, 1 minúscula, 1 número
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(pattern);
    }
}