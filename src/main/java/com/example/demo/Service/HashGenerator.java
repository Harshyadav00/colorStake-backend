package com.example.demo.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HashGenerator {
    public static String generateHash(String key, String txnId, String amount, String productInfo,
                                      String firstName, String email, String salt) {
        String hashString = key + "|" + txnId + "|" + amount + "|" + productInfo + "|" +
                firstName + "|" + email + "|||||||||||" + salt;
        return getSHA512(hashString);
    }

    public static String getSHA512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            Formatter formatter = new Formatter();
            for (byte b : digest) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
