package models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
    private String type;
    private double amount;
    private String dateTime;

    // Constructor 1: Used when creating a NEW transaction (Deposit/Withdraw)
    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // Constructor 2: Used when reading from FILE (Fixes your list.add error)
    public Transaction(String line) {
        // We just store the raw line for history display purposes in this demo
        this.type = line;
        this.amount = 0;
        this.dateTime = "";
    }

    @Override
    public String toString() {
        // If it was loaded from a file, it's already a full string, just return it.
        if (dateTime.equals("")) return type;

        return "[" + dateTime + "] " + type + ": " + amount;
    }
}