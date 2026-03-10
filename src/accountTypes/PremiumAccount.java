package accountTypes;

import models.Account;

public class PremiumAccount extends Account {

    public PremiumAccount(int accNum, String name,  String email, String uniId, int pin) {

        super(accNum, name,  email, uniId, pin, "Premium", 1000000);

    }

}
