package accountTypes;

import models.Account;

public class StudentAccount extends Account {

    public StudentAccount(int accNum, String name,  String email, String uniId, int pin) {

        super(accNum, name,  email, uniId, pin, "Student", 25000);

    }

}
