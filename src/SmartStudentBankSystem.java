import accountTypes.*;
import models.*;
import database.*;

import java.util.*;

public class SmartStudentBankSystem {

    public static final int MAX_ACCOUNTS = 1000;

    // Lists to hold loaded data
    private static List<Account> approvedAccounts = new ArrayList<>();
    private static List<Account> pendingAccounts = new ArrayList<>();

    private static Scanner scanner = new Scanner(System.in);
    private static int accountCounter = 1001;

    public static void main(String[] args) {
        // 1. Load Data
        approvedAccounts = FileHandler.loadAccounts(false);
        pendingAccounts = FileHandler.loadAccounts(true);

        // Update counter so we don't reuse IDs
        updateCounter();

        System.out.println("=== Smart Student Banking Management System ===");

        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Register (Create Account)");
            System.out.println("2. Login (Student)");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": createAccountFlow(); break;
                case "2": studentLoginFlow(); break;
                case "3": adminLoginFlow(); break;
                case "4":
                    // Save Data
                    FileHandler.saveAccounts(approvedAccounts, false);
                    FileHandler.saveAccounts(pendingAccounts, true);
                    System.out.println("Data saved. Shutting down. Bye!");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void updateCounter() {
        for(Account a : approvedAccounts) if(a.getAccountNumber() >= accountCounter) accountCounter = a.getAccountNumber() + 1;
        for(Account a : pendingAccounts) if(a.getAccountNumber() >= accountCounter) accountCounter = a.getAccountNumber() + 1;
    }

    // ---------------- Registration ----------------
    public static void createAccountFlow() {
        System.out.println("--- Create Account ---");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("University ID: ");
        String uniId = scanner.nextLine().trim();

        int pin = 0;
        while (true) {
            System.out.print("Set 4-digit PIN: ");
            String pstr = scanner.nextLine().trim();
            try {
                pin = Integer.parseInt(pstr);
                if (pstr.length()==4) break;
            } catch (Exception e) {}
            System.out.println("PIN must be 4 digits.");
        }

        System.out.println("Select Card Type: 1.Student 2.Silver 3.Gold 4.Premium");
        String t = scanner.nextLine().trim();

        Account newAcc = null;
        int accNum = accountCounter++;

        if (t.equals("1")) newAcc = new StudentAccount(accNum, name, email, uniId, pin);
        else if (t.equals("2")) newAcc = new SilverAccount(accNum, name, email, uniId, pin);
        else if (t.equals("3")) newAcc = new GoldAccount(accNum, name, email, uniId, pin);
        else if (t.equals("4")) newAcc = new PremiumAccount(accNum, name, email, uniId, pin);
        else {
            System.out.println("Invalid card.");
            return;
        }

        pendingAccounts.add(newAcc);
        FileHandler.saveAccounts(pendingAccounts, true);

        System.out.println("Registration submitted. Account Number: " + accNum);
        System.out.println("Please wait for Admin Approval.");
    }

    // ---------------- Student Login ----------------
    public static void studentLoginFlow() {
        System.out.print("Enter Account Number: ");
        try {
            int accNum = Integer.parseInt(scanner.nextLine().trim());
            Account acc = findApprovedAccount(accNum);

            if (acc == null) { System.out.println("Account not found or not approved."); return; }

            System.out.print("Enter PIN: ");
            int pin = Integer.parseInt(scanner.nextLine().trim());

            if (!acc.validatePin(pin)) { System.out.println("Invalid PIN."); return; }

            boolean session = true;
            while (session) {
                System.out.println("\n--- Dashboard for " + acc.getName() + " ---");
                System.out.println("1. Check Balance");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Transfer");
                System.out.println("5. Transaction History");
                System.out.println("6. Logout");
                System.out.print("Choose: ");
                String a = scanner.nextLine().trim();

                try {
                    switch (a) {
                        case "1": acc.checkBalance(); break;
                        case "2":
                            System.out.print("Deposit Amount: ");
                            double damt = Double.parseDouble(scanner.nextLine().trim());
                            acc.deposit(damt);
                            break;
                        case "3":
                            System.out.print("Withdraw Amount: ");
                            double wamt = Double.parseDouble(scanner.nextLine().trim());
                            acc.withdraw(wamt);
                            break;
                        case "4":
                            System.out.print("Target Account ID: ");
                            int tid = Integer.parseInt(scanner.nextLine());
                            Account target = findApprovedAccount(tid);
                            if(target == null) { System.out.println("Target not found."); break; }

                            System.out.print("Transfer Amount: ");
                            double tamt = Double.parseDouble(scanner.nextLine());
                            acc.transfer(target, tamt);
                            break;
                        case "5": acc.printHistory(); break;
                        case "6": session = false; break;
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid Input.");
        }
    }

    // ---------------- Admin Logic (Restored Full Menu) ----------------
    public static void adminLoginFlow() {
        Admin admin = new Admin();
        System.out.print("Admin User: "); String u = scanner.nextLine();
        System.out.print("Admin Pass: "); String p = scanner.nextLine();

        if (!admin.login(u, p)) { System.out.println("Failed."); return; }

        boolean session = true;
        while(session) {
            System.out.println("\n--- Admin Panel ---");
            System.out.println("1. View Pending Accounts");
            System.out.println("2. View Approved Accounts");
            System.out.println("3. Approve Account");
            System.out.println("4. Reject (Delete) Pending Account");
            System.out.println("5. View Transactions for Account");
            System.out.println("6. Logout");
            System.out.print("Choose: ");
            String ch = scanner.nextLine().trim();

            switch(ch) {
                case "1": // View Pending
                    if (pendingAccounts.isEmpty()) System.out.println("No pending accounts.");
                    else {
                        System.out.println("--- Pending List ---");
                        for(Account a : pendingAccounts)
                            System.out.println(a.getAccountNumber() + " | " + a.getName() + " | " + a.getCardType());
                    }
                    break;

                case "2": // View Approved
                    if (approvedAccounts.isEmpty()) System.out.println("No approved accounts.");
                    else {
                        System.out.println("--- Approved List ---");
                        for(Account a : approvedAccounts)
                            System.out.println(a.getAccountNumber() + " | " + a.getName() + " | Balance: " + a.getBalance());
                    }
                    break;

                case "3": // Approve
                    System.out.print("Enter Pending ID to Approve: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine());
                        Account target = findPendingAccount(id);
                        if(target != null) {
                            target.setStatus("ACTIVE");
                            approvedAccounts.add(target);
                            pendingAccounts.remove(target);
                            System.out.println("Account Approved!");
                            FileHandler.saveAccounts(approvedAccounts, false);
                            FileHandler.saveAccounts(pendingAccounts, true);
                        } else {
                            System.out.println("ID not found in Pending list.");
                        }
                    } catch(Exception e) { System.out.println("Invalid ID."); }
                    break;

                case "4": // Reject
                    System.out.print("Enter Pending ID to Reject: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine());
                        Account target = findPendingAccount(id);
                        if(target != null) {
                            pendingAccounts.remove(target);
                            System.out.println("Account Rejected and Removed.");
                            FileHandler.saveAccounts(pendingAccounts, true);
                        } else {
                            System.out.println("ID not found.");
                        }
                    } catch(Exception e) { System.out.println("Invalid ID."); }
                    break;

                case "5": // View Transactions
                    System.out.print("Enter Approved Account ID: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine());
                        Account target = findApprovedAccount(id);
                        if(target != null) {
                            target.printHistory();
                        } else {
                            System.out.println("Account not found.");
                        }
                    } catch(Exception e) { System.out.println("Invalid ID."); }
                    break;

                case "6": // Logout
                    session = false;
                    break;

                default: System.out.println("Invalid Option.");
            }
        }
    }

    // ---------------- Helpers ----------------
    public static Account findApprovedAccount(int accNum) {
        for (Account a : approvedAccounts) if (a.getAccountNumber() == accNum) return a;
        return null;
    }

    public static Account findPendingAccount(int accNum) {
        for (Account a : pendingAccounts) if (a.getAccountNumber() == accNum) return a;
        return null;
    }
}
