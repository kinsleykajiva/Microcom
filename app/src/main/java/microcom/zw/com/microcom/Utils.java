package microcom.zw.com.microcom;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Kajiva Kinsley on 2/17/2018.
 */

public class Utils {
    public static String checkDetails(String message){
        String Rs="";
        OkHttpClient client = new OkHttpClient();
        JSONObject json;

        Request request = new Request.Builder().url(
                "http://www.rdsol.co.zw/afrocom/silentscripts/nfc_card.php?"+message).build();

        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String mess = response.body().string();
                Log.e ( "xxx 4", "checkDetails: " +mess );
                json=(new JSONObject(mess));
                Rs=json.getString("message");
            }
        }catch(IOException | JSONException e) {e.printStackTrace();}
        return Rs;
    }
    public static String saveUser(String message){
        String Rs="";
        OkHttpClient client = new OkHttpClient();
        JSONObject json;

        Request request = new Request.Builder().url(
                "http://www.rdsol.co.zw/afrocom/silentscripts/nfc_card.php?"+message).build();
        Log.e ( "xxx1", "checkDetails: " +"http://www.rdsol.co.zw/afrocom/silentscripts/adduser.php?"+message );
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String mess = response.body().string();

                json=(new JSONObject(mess));
                Rs=json.getString("message");
            }
        }catch(IOException | JSONException e) {e.printStackTrace();}
        return Rs;
    }
}
