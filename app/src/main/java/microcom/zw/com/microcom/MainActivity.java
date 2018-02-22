package microcom.zw.com.microcom;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.ihongqiqu.util.NetUtil.isNetworkAvailable;
import static microcom.zw.com.microcom.CRUD.savePayment;
import static microcom.zw.com.microcom.Utils.androidId;
import static microcom.zw.com.microcom.Utils.checkDetails;
import static microcom.zw.com.microcom.Utils.pay;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    private NifftyDialogs nifftyDialogs;
   private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode;
    private  String accountName,cardNumber;
    private TextView tvNFCContent ,status ,tagStatus;
    private Tag myTag;
    private static boolean isTaskRunning =false;
    private  boolean canWrite = false;
    private     MakeCheks makeCheks ;
    private String  balance="";
    private Button  brnadd;

    private Context context = MainActivity.this;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
     ///   startActivity( new Intent(this, Home.class) );
        initObjects();
        initViews();

        brnadd.setOnClickListener ( v -> startActivity ( new Intent ( MainActivity.this, AddUser.class ) ) );

    }
    private void showProgressDialog (final boolean isToShow) {

            if ( isToShow ) {
                if ( ! progressDialog.isShowing () ) {
                    progressDialog.setMessage ( "Processing ...Please wait." );
                    progressDialog.setCancelable ( false );
                    progressDialog.show ();
                }
            } else {
                if ( progressDialog.isShowing () ) {
                    progressDialog.dismiss ();
                }
            }

    }

    private void initObjects () {
        progressDialog = new ProgressDialog (this );
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nifftyDialogs = new NifftyDialogs ( context );
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        readFromIntent(getIntent());


        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory( Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    private void initViews () {
        status = findViewById ( R.id.status );
        tvNFCContent = findViewById(R.id.nfc_contents);
        tagStatus = findViewById ( R.id.tagStatus );
        brnadd = findViewById ( R.id.brnadd );
        getSupportActionBar ().setTitle ( "Swipa Kombi" );
        getSupportActionBar ().setDisplayHomeAsUpEnabled ( true );
    }
    /**
     * Read From NFC Tag
     * @param intent receive intent to read the nfc tag
     * */
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private class MakeCheks extends AsyncTask< String, Void, String > {

        @Override
        protected String doInBackground (String... strings) {
            return pay (strings[0]);
        }

        @Override
        protected void onPostExecute (String s) {
            super.onPostExecute ( s );
            showProgressDialog ( false );
            isTaskRunning = false;
            Log.e ( "xxx", "onPostExecute: "+s  );
            if(s.isEmpty ()){
                status.setText ( "Connection Error" );
                nifftyDialogs.messageOkError ("Server Response",  "Connection Error"  );
                status.setTextColor ( Color.parseColor ( "#ea4a34" ) ); // green
                Toast.makeText ( context, "Connection Error", Toast.LENGTH_LONG ).show ();
                return;
            }
            if(s.equals ( "none" )){
                nifftyDialogs.messageOkError ("Error !" ,"User doesn't Exist,Register!");
            }
            if(s.equals ( "err_update" )){
                nifftyDialogs.messageOkError ("Error !" ,"Transaction Failed !");
            }
            if(s.contains ( "broke" )){
                String[] value_split = s.split("\\|");
                nifftyDialogs.messageOkError ("Insufficient Funds !" ," Balance $" + value_split[1]);
                tvNFCContent.setText("Card Name:" + accountName + "\n Number:" +cardNumber + "\n Newer Balance :"+value_split[1]);
            }
            if(s.contains ( "done" )){
                String[] value_split = s.split("\\|");
                nifftyDialogs.messageOk ("Finished !" ,"New Balance: $" +value_split[1]);

            try {
                if ( myTag == null ) {
                    canWrite = false ;
                    Toast.makeText ( context, ERROR_DETECTED, Toast.LENGTH_LONG ).show ();
                } else {
                    write (
                            accountName + "|" + cardNumber + "|" + value_split[1],

                            myTag
                    );
                    canWrite = true ;
                    savePayment(accountName ,cardNumber ,androidId(context));
                    tagStatus.setText ( "Saved To Card" );
                    tvNFCContent.setText("Card Name:" + accountName + "\n Number:" +cardNumber + "\n Newer Balance :"+value_split[1]);
                    Toast.makeText ( context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show ();
                }
            } catch (IOException | FormatException e) {
                Toast.makeText ( context, WRITE_ERROR, Toast.LENGTH_LONG ).show ();
                e.printStackTrace ();
            }
            }



        }
    }
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"


        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace ();
            Log.e("xxxx","UnsupportedEncoding", e);
        }
        Log.e ( "xxx", "buildTagViews: " + text  );
        String[] value_split = text.split("\\|");


        if(/*makeCheks.getStatus () != AsyncTask.Status.RUNNING*/ !isTaskRunning) {
            if ( value_split.length == 3 ) {
                 accountName = value_split[ 0 ];
                 cardNumber = value_split[ 1 ];
                balance = value_split[ 2 ];
                tvNFCContent.setText("Card Name:" + accountName + "\n Number:" +cardNumber + "\n Balance :"+balance);
                showProgressDialog ( true );
                makeCheks = new MakeCheks ();
                isTaskRunning = true;
                if(isNetworkAvailable( context)){

                    makeCheks.execute ( "accountName=" + accountName + "&cardNumber=" + cardNumber + "&balance=" + balance + "&deviceid="+androidId(context) );
                }else{
                    nifftyDialogs.messageOkError ( "Connection Error" , "No Internet Connection" );
                }
            }
        }
    }

    /**
     * Write to NFC Tag
     * @param text the text to write
     * @param tag the target Tag.
     *
     * */
    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }
    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }
    /**Enable Write*/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /**Disable Write*/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
