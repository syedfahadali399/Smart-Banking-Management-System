package accountTypes;

import models.Account;

public class SilverAccount extends Account {

    public SilverAccount(int accNum, String name,  String email, String uniId, int pin) {

        super(accNum, name,  email, uniId, pin, "Silver", 100000);

    }

}
