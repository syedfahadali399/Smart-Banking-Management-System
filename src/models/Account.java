package models;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Account {

    protected int accountNumber;
    protected String name;
    protected String email;
    protected String uniId;
    protected int pin;
    protected double balance;
    protected String cardType;
    protected double withdrawLimit;
    protected List<Transaction> transactions;
    protected String status;

    public Account(int accountNumber, String name, String email, String uniId,
                   int pin, String cardType, double withdrawLimit) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.email = email;
        this.uniId = uniId;
        this.pin = pin;
        this.cardType = cardType;
        this.withdrawLimit = withdrawLimit;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
        this.status = "PENDING";
    }

    public int getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCardType() { return cardType; }
    public String getStatus() { return status; }
    public double getBalance() { return balance; }

    public void setStatus(String s) { status = s; }
    public void setBalance(double b) { this.balance = b; } // Needed for FileHandler

    // ---------------- AUTHENTICATION ----------------
    public boolean validatePin(int inputPin) {
        return this.pin == inputPin;
    }

    // ---------------- BANKING OPERATIONS ----------------
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            addTransaction("DEPOSIT", amount);
            System.out.println("Success: Deposited " + amount);
            appendTransactionToFile(new Transaction("DEPOSIT", amount));
        } else {
            System.out.println("Error: Invalid amount.");
        }
    }

    public void withdraw(double amount) {
        if (amount > balance) {
            System.out.println("Error: Insufficient funds.");
        } else if (amount > withdrawLimit) {
            System.out.println("Error: Exceeds " + cardType + " limit of " + withdrawLimit);
        } else {
            balance -= amount;
            addTransaction("WITHDRAW", amount);
            System.out.println("Success: Withdrawn " + amount);
            appendTransactionToFile(new Transaction("WITHDRAW", amount));
        }
    }

public void transfer(Account target, double amount) {

    // 1. Check for Self-Transfer
    if (this.accountNumber == target.accountNumber) {
        System.out.println("Error: Cannot transfer money to yourself.");
        return; // Stop the code here
    }

    // 2. Check Balance
    if (amount > balance) {
        System.out.println("Error: Insufficient funds for transfer.");
    } else {
        this.balance -= amount;
        target.balance += amount;

        this.addTransaction("TRANSFER OUT to " + target.name, amount);
        target.addTransaction("TRANSFER IN from " + this.name, amount);

        System.out.println("Success: Transferred " + amount + " to " + target.name);

        this.appendTransactionToFile(new Transaction("TRANSFER OUT to " + target.name, amount));
        target.appendTransactionToFile(new Transaction("TRANSFER IN from " + this.name, amount));
    }
}
    public void checkBalance() {
        System.out.println("Account: " + accountNumber + " | Type: " + cardType);
        System.out.println("Current Balance: " + balance);
    }

    protected void addTransaction(String type, double amount) {
        transactions.add(new Transaction(type, amount));
    }

    public void printHistory() {
        System.out.println("--- Transaction History for " + name + " ---");
        List<Transaction> list = readTransactionFile();
        if (list.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (Transaction t : list) System.out.println(t);
        }
    }

    // ---------------- PERSISTENCE HELPERS ----------------
    // CSV Format: account|name|email|uniId|pin|type|balance|status
    public String toCSVLine() {
        return accountNumber + "|" + name + "|" + email + "|" + uniId + "|" +
                pin + "|" + cardType + "|" + balance + "|" + status;
    }

    // Helper to save transaction history to a separate file
    public void appendTransactionToFile(Transaction t) {
        String fileName = "transactions_" + accountNumber + ".txt";
        try (FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(t.toString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error saving transaction file: " + e.getMessage());
        }
    }

    public List<Transaction> readTransactionFile() {
        List<Transaction> list = new ArrayList<>();
        String fileName = "transactions_" + accountNumber + ".txt";
        File f = new File(fileName);
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Pass the raw line to Transaction constructor
                list.add(new Transaction(line));
            }
        } catch (IOException e) {
            // ignore
        }
        return list;
    }
}