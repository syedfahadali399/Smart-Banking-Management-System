package database;

import models.Account;
import accountTypes.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    private static final String ACCOUNTS_FILE = "approved_accounts.txt";
    private static final String PENDING_FILE = "pending_accounts.txt";

    // ---------------- SAVE ACCOUNTS ----------------
    public static void saveAccounts(List<Account> accounts, boolean isPending) {
        String filename = isPending ? PENDING_FILE : ACCOUNTS_FILE;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (Account a : accounts) {
                bw.write(a.toCSVLine());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    // ---------------- LOAD ACCOUNTS ----------------
    public static List<Account> loadAccounts(boolean isPending) {
        String filename = isPending ? PENDING_FILE : ACCOUNTS_FILE;
        List<Account> list = new ArrayList<>();

        File f = new File(filename);
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Parsing CSV: Number|Name|Email|UniID|Pin|Type|Balance|Status
                String[] parts = line.split("\\|");

                if (parts.length < 8) continue; // Skip broken lines

                // 1. EXTRACT DATA FROM FILE
                int accNum = Integer.parseInt(parts[0]);
                String name = parts[1];
                String email = parts[2];
                String uniId = parts[3];

                // --- THIS IS THE LINE YOU WERE MISSING ---
                int pin = Integer.parseInt(parts[4]);
                // ----------------------------------------

                String cardType = parts[5];
                double bal = Double.parseDouble(parts[6]);
                String status = parts[7];

                Account a = null;

                // 2. CREATE OBJECT (Now 'pin' is defined and works)
                if (cardType.equals("Student")) {
                    a = new StudentAccount(accNum, name, email, uniId, pin);
                } else if (cardType.equals("Silver")) {
                    a = new SilverAccount(accNum, name, email, uniId, pin);
                } else if (cardType.equals("Gold")) {
                    a = new GoldAccount(accNum, name, email, uniId, pin);
                } else if (cardType.equals("Premium")) {
                    a = new PremiumAccount(accNum, name, email, uniId, pin);
                }

                if (a != null) {
                    a.setBalance(bal);
                    a.setStatus(status);
                    list.add(a);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
        return list;
    }
}