package models;

public class Admin {

    private String username = "admin";

    private String password = "1234"; // demo only


    public boolean login(String u, String p) {

        return username.equals(u) && password.equals(p);

    }

}
