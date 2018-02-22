package microcom.zw.com.microcom;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Kajiva Kinsley on 2/22/2018.
 */

public class CRUD {
    private CRUD(){
        new Throwable ( "Cant be instaiated" );
    }


    public static void savePayment(String accountName ,String cardNumber , String device_id){
        new Thread ( () -> {
             String date_ = new SimpleDateFormat ( "yyyy/MM/dd" ).format ( new Date () );
            String dtime = new SimpleDateFormat ( "HH:mm:ss" ).format ( new Date () );
            Realm realm = Realm.getDefaultInstance ();
            RealmResults< TransactionPojo > results = realm.where ( TransactionPojo.class ).findAll ();

            realm.beginTransaction ();
            TransactionPojo row = realm.createObject ( TransactionPojo.class );
            row.setDate_ (date_  );
            row.setTime_ ( dtime );
            row.setAccountName ( accountName );
            row.setCardNumber ( cardNumber );
            row.setDevceID (  device_id );

            row.setID (
                    (results.isEmpty () && results.size () < 1) ? 0 : (results.max ( "ID" ).intValue () + 1)
            );

            realm.commitTransaction ();
            realm.close ();
        } ).start ();
    }


}
