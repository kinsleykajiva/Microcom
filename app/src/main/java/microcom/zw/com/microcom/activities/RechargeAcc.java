package microcom.zw.com.microcom.activities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import microcom.zw.com.microcom.utils.NifftyDialogs;
import microcom.zw.com.microcom.R;

import static com.ihongqiqu.util.NetUtil.isNetworkAvailable;
import static microcom.zw.com.microcom.utils.Utils.ERROR_DETECTED;
import static microcom.zw.com.microcom.utils.Utils.WRITE_ERROR;
import static microcom.zw.com.microcom.utils.Utils.WRITE_SUCCESS;
import static microcom.zw.com.microcom.utils.Utils.playSound;
import static microcom.zw.com.microcom.utils.Utils.updateBalance;

public class RechargeAcc extends AppCompatActivity {

private TextView UserName;
private EditText amountToAdd;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode ,canWrite = false;
    private  String accountName ="" ,cardNumber  ="",balance ="" ,Alltxt = "";
    private Tag myTag;
private Button btnUpdate;
    private NifftyDialogs nifftyDialogs;
    private Context context = RechargeAcc.this;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_recharge_acc );
        initObjects();
        initViews();
        btnUpdate.setOnClickListener ( ev->{
            String amount  = amountToAdd.getText ().toString ().trim ();

            String[] value_split = Alltxt.split("\\|");
            if ( value_split.length != 3 ) {
                nifftyDialogs.messageOkError ( "Error" , "Error .Card Compromised" );
                Toast.makeText ( context, "Error .Card Compromised", Toast.LENGTH_SHORT ).show ();
                return;
            }
            if(amount.isEmpty ()){
                Toast.makeText ( context, "Amount Cant be Empty", Toast.LENGTH_SHORT ).show ();
                return;
            }
            showProgressDialog ( true );
                    if ( isNetworkAvailable ( context ) ) {
                        new MakeChecks ().execute ( "accountName=" + accountName + "&cardNumber=" + cardNumber + "&amount=" + amount );
                    }else {
                        nifftyDialogs.messageOkError ( "Connection Error", "No Internet Connection" );
                    }
        } );

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
        if ( value_split.length != 3 ) {
            nifftyDialogs.messageOkError ( "Error" , "Error .Card Compromised" );
            Toast.makeText ( context, "Error .Card Compromised", Toast.LENGTH_SHORT ).show ();
            return;
        }


            if ( value_split.length == 3 ) {
                 accountName = value_split[ 0 ];
                 cardNumber = value_split[ 1 ];
                  balance = value_split[ 2 ];
                Alltxt= text;

                UserName.setText("Account : " + accountName +  " \n"+ "Car Number"+cardNumber + "Current Balance "+balance);

            }

    }


    private class MakeChecks extends AsyncTask<String ,Void,String>{

        @Override
        protected String doInBackground (String... strings) {
            return updateBalance(strings[0]);
        }

        @Override
        protected void onPostExecute (String s) {
            super.onPostExecute ( s );
            showProgressDialog ( false);
            Log.e ( "xxx", "onPostExecute: "+s  );
            if(s.isEmpty ()){
                //status.setText ( "Connection Error" );
                playSound(context ,2);
                nifftyDialogs.messageOkError ("Server Response",  "Connection Error"  );
               // status.setTextColor ( Color.parseColor ( "#ea4a34" ) ); // green
                Toast.makeText ( context, "Connection Error", Toast.LENGTH_LONG ).show ();
                return;
            }
            if(s.equals ( "unfound" )){
                playSound(context ,2);
                nifftyDialogs.messageOkError ("Error !" ,"User doesn't Exist,Register!");
                Toast.makeText ( context, "User doesn't Exist,Register", Toast.LENGTH_LONG ).show ();
                return;
            }
            if(s.contains ( "done" )){

                String[] value_split = s.split("\\|");
                nifftyDialogs.messageOk ("Recharged !" ,"New Balance: $" +value_split[1]);

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
                       // tagStatus.setText ( "Saved To Card" );
                        UserName.append ( "\n\n" );
                        playSound(context ,1);
                        UserName.append( "\n Newer Balance :"+value_split[1]);
                        Toast.makeText ( context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show ();
                    }
                } catch (IOException | FormatException e) {
                    playSound(context ,2);
                    Toast.makeText ( context, WRITE_ERROR, Toast.LENGTH_LONG ).show ();
                    e.printStackTrace ();
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
    private void initViews () {
        getSupportActionBar().setTitle ( "Top-Up Balance" );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        UserName = findViewById ( R.id.UserName );
        amountToAdd = findViewById ( R.id.amountToAdd );
        btnUpdate = findViewById ( R.id.btnUpdate );
    }

    private void initObjects () {
        nifftyDialogs = new NifftyDialogs ( this );
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
