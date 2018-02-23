package microcom.zw.com.microcom.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import microcom.zw.com.microcom.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kajiva Kinsley on 2/17/2018.
 */

public class Utils {
    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Saved to Card Successfully!";
    public static final String WRITE_ERROR = "Error during writting, make sure the NFC tag is close enough to the device?";
    public static int TIME_OUT = 10, READ_TIME_OUT = 30;

    public static String checkDetails (String message) {
        String Rs = "";

        OkHttpClient client = new OkHttpClient.Builder ()
                .connectTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .writeTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .readTimeout ( READ_TIME_OUT, TimeUnit.SECONDS )
                .build ();

        JSONObject json;

        Request request = new Request.Builder ().url (
                "http://www.rdsol.co.zw/afrocom/silentscripts/nfc_card.php?" + message ).build ();

        try (Response response = client.newCall ( request ).execute ()) {
            // Response response = client.newCall(request).execute();
            if ( response.isSuccessful () ) {
                String mess = response.body ().string ();
                Log.e ( "xxx 4", "checkDetails: " + mess );
                json = (new JSONObject ( mess ));
                Rs = json.getString ( "message" );
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace ();
        }
        return Rs;
    }
    public static String checkBalance (String message) {
        String Rs = "";

        OkHttpClient client = new OkHttpClient.Builder ()
                .connectTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .writeTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .readTimeout ( READ_TIME_OUT, TimeUnit.SECONDS )
                .build ();

        JSONObject json;

        Request request = new Request.Builder ().url (
                "http://www.rdsol.co.zw/afrocom/silentscripts/checkbalance.php?" + message ).build ();
        Log.e ( "xxx1", "checkBalance: " + "http://www.rdsol.co.zw/afrocom/silentscripts/checkbalance.php?" + message );
        try (Response response = client.newCall ( request ).execute ()) {
            // Response response = client.newCall(request).execute();
            if ( response.isSuccessful () ) {
                String mess = response.body ().string ();
                Log.e ( "xxx 4", "checkBalance: " + mess );
                json = (new JSONObject ( mess ));
                Rs = json.getString ( "message" );
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace ();
        }
        return Rs;
    }

    public static String saveUser (String message) {
        String Rs = "";
        OkHttpClient client = new OkHttpClient.Builder ()
                .connectTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .writeTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .readTimeout ( READ_TIME_OUT, TimeUnit.SECONDS )
                .build ();
        JSONObject json;

        Request request = new Request.Builder ().url (
                "http://www.rdsol.co.zw/afrocom/silentscripts/adduser.php?" + message ).build ();
        Log.e ( "xxx1", "saveUser: " + "http://www.rdsol.co.zw/afrocom/silentscripts/adduser.php?" + message );
        try (Response response = client.newCall ( request ).execute ()) {

            if ( response.isSuccessful () ) {
                String mess = response.body ().string ();

                json = (new JSONObject ( mess ));
                Rs = json.getString ( "message" );
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace ();
        }
        return Rs;
    }

    public static String updateBalance (String message) {
        String Rs = "";
        OkHttpClient client = new OkHttpClient.Builder ()
                .connectTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .writeTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .readTimeout ( READ_TIME_OUT, TimeUnit.SECONDS )
                .build ();
        JSONObject json;

        Request request = new Request.Builder ().url (
                "http://www.rdsol.co.zw/afrocom/silentscripts/rechargebalance.php?" + message ).build ();
        Log.e ( "xxx4", "updateBalance: " + "http://www.rdsol.co.zw/afrocom/silentscripts/rechargebalance.php?" + message );
        try (Response response = client.newCall ( request ).execute ()) {

            if ( response.isSuccessful () ) {
                String mess = response.body ().string ();

                json = (new JSONObject ( mess ));
                Rs = json.getString ( "message" );
                if(Rs.equals ( "saved" )){
                    Rs =  "done" + "|"+json.getString ( "new_balance" ) ;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace ();
        }
        return Rs;
    }

    public static String pay (String message) {
        String Rs = "";
        OkHttpClient client = new OkHttpClient.Builder ()
                .connectTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .writeTimeout ( TIME_OUT, TimeUnit.SECONDS )
                .readTimeout ( READ_TIME_OUT, TimeUnit.SECONDS )
                .build ();
        JSONObject json;

        Request request = new Request.Builder ().url (
                "http://www.rdsol.co.zw/afrocom/silentscripts/makepayment.php?" + message ).build ();
        Log.e ( "xxx1", "pay: " + "http://www.rdsol.co.zw/afrocom/silentscripts/makepayment.php?" + message );
        try (Response response = client.newCall ( request ).execute ()) {

            if ( response.isSuccessful () ) {
                String mess = response.body ().string ();

                json = (new JSONObject ( mess ));
                Rs = json.getString ( "message" );

                if(Rs.equals ( "done" )){
                    Rs =  "done" + "|"+json.getString ( "new_balance" ) +  "|"+json.getString ( "last_balance" );
                }
                if(Rs.equals ( "broke" )){
                    Rs =  "broke" + "|"+json.getString ( "last_balance" ) ;
                }

            }
        } catch (IOException | JSONException e) {
            e.printStackTrace ();
        }
        return Rs;
    }
    @SuppressLint("MissingPermission")
    public static String getPhoneNumber (Context context) {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService ( Context.TELEPHONY_SERVICE );

        return  tMgr.getLine1Number ();


    }
    public static String getChars(int numberOFCharsToGet , String str) {
        return str.length() < numberOFCharsToGet ? str : str.substring(0, numberOFCharsToGet);
    }
    public static String androidId(Context context){
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
    public static   String getPhone(Context context ,Activity activity) {
        TelephonyManager phoneMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        return phoneMgr.getLine1Number();
    }
    public static boolean isThereSim(Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT){
            //the phone has a sim card
            return true;
        } else {
            //no sim card available
            return false;
        }

    }
    /**Play sound Effect
     * if type - 1 => general
     * if type -2 => error
     * */
    public static void playSound(Context context , int type){
        MediaPlayer mp = MediaPlayer.create(context, type == 1 ? R.raw.general_beep : R.raw.error);
        mp.start();
    }
}
