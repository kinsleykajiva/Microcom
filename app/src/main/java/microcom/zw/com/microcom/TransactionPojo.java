package microcom.zw.com.microcom;

import io.realm.RealmObject;

/**
 * Created by Kajiva Kinsley on 2/22/2018.
 */

public class TransactionPojo extends RealmObject {
    private  int ID;
    private String devceID , accountName ,cardNumber , date_ ,time_ ;


    public TransactionPojo () {
    }

    public String getAccountName () {
        return accountName;
    }

    public void setAccountName (String accountName) {
        this.accountName = accountName;
    }

    public String getCardNumber () {
        return cardNumber;
    }

    public void setCardNumber (String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getID () {
        return ID;
    }

    public String getDate_ () {
        return date_;
    }

    public void setDate_ (String date_) {
        this.date_ = date_;
    }

    public String getTime_ () {
        return time_;
    }

    public void setTime_ (String time_) {
        this.time_ = time_;
    }

    public void setID (int ID) {
        this.ID = ID;
    }

    public String getDevceID () {
        return devceID;
    }

    public void setDevceID (String devceID) {
        this.devceID = devceID;
    }



}
