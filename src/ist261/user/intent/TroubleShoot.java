package ist261.user.intent;

import java.util.Hashtable;

public class TroubleShoot extends AbstractUserIntent{
    public TroubleShoot(String userMsg) { super(userMsg); }

    @Override
    Hashtable<String, Object> extractSlotValuesFromUserMsg(String userMsg) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        System.out.println(userMsg);

        return result;
    }
}