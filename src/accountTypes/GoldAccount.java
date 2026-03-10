package accountTypes;

import models.Account;

public class GoldAccount extends Account {

    public GoldAccount(int accNum, String name,  String email, String uniId, int pin) {

        super(accNum, name,  email, uniId, pin, "Gold", 250000);

    }

}
