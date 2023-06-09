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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import microcom.zw.com.microcom.utils.NifftyDialogs;
import microcom.zw.com.microcom.R;

import static com.ihongqiqu.util.NetUtil.isNetworkAvailable;
import static microcom.zw.com.microcom.utils.Utils.checkBalance;

public class CheckBalance extends AppCompatActivity {
    private TextView status;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode, canWrite = false;
    private String accountName = "", cardNumber = "", balance = "", Alltxt = "";
    private Tag myTag;
    private NifftyDialogs nifftyDialogs;
    private Context context = CheckBalance.this;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_check_balance );
        initObjects ();
        initViews ();

    }

    private class MakeChecks extends AsyncTask < String, Void, String > {

        @Override
        protected String doInBackground (String... strings) {
            return checkBalance ( strings[ 0 ] );
        }

        @Override
        protected void onPostExecute (String s) {
            super.onPostExecute ( s );
            Log.e ( "xxx", "onPostExecute: " + s );
            if ( s.isEmpty () ) {
                //status.setText ( "Connection Error" );
                nifftyDialogs.messageOkError ( "Server Response", "Connection Error" );
                // status.setTextColor ( Color.parseColor ( "#ea4a34" ) ); // green
                Toast.makeText ( context, "Connection Error", Toast.LENGTH_LONG ).show ();
                return;
            }

            if ( s.equals ( "unfound" ) ) {
                nifftyDialogs.messageOkError ( "Error !", "User doesn't Exist,Register!" );
                Toast.makeText ( context, "User doesn't Exist,Register", Toast.LENGTH_LONG ).show ();
                return;
            }

            nifftyDialogs.messageOk ( "Information", "Balance is $" + s );


        }
    }

    private void initViews () {
        status = findViewById ( R.id.status );
        getSupportActionBar ().setTitle ( "Check Balance" );
        getSupportActionBar ().setDisplayHomeAsUpEnabled ( true );
    }

    /**
     * Write to NFC Tag
     *
     * @param text the text to write
     * @param tag  the target Tag.
     */
    private void write (String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = {createRecord ( text )};
        NdefMessage message = new NdefMessage ( records );
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get ( tag );
        // Enable I/O
        ndef.connect ();
        // Write the message
        ndef.writeNdefMessage ( message );
        // Close the connection
        ndef.close ();
    }

    private NdefRecord createRecord (String text) throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes ();
        byte[] langBytes = lang.getBytes ( "US-ASCII" );
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[ 1 + langLength + textLength ];

        // set status byte (see NDEF spec for actual bits)
        payload[ 0 ] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy ( langBytes, 0, payload, 1, langLength );
        System.arraycopy ( textBytes, 0, payload, 1 + langLength, textLength );

        NdefRecord recordNFC = new NdefRecord ( NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[ 0 ], payload );

        return recordNFC;
    }

    @Override
    protected void onNewIntent (Intent intent) {
        setIntent ( intent );
        readFromIntent ( intent );
        if ( NfcAdapter.ACTION_TAG_DISCOVERED.equals ( intent.getAction () ) ) {
            myTag = intent.getParcelableExtra ( NfcAdapter.EXTRA_TAG );
        }
    }

    /**
     * Read From NFC Tag
     *
     * @param intent receive intent to read the nfc tag
     */
    private void readFromIntent (Intent intent) {
        String action = intent.getAction ();
        if ( NfcAdapter.ACTION_TAG_DISCOVERED.equals ( action )
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals ( action )
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals ( action ) ) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra ( NfcAdapter.EXTRA_NDEF_MESSAGES );
            NdefMessage[] msgs = null;
            if ( rawMsgs != null ) {
                msgs = new NdefMessage[ rawMsgs.length ];
                for ( int i = 0; i < rawMsgs.length; i++ ) {
                    msgs[ i ] = (NdefMessage) rawMsgs[ i ];
                }
            }
            buildTagViews ( msgs );
        }
    }

    private void buildTagViews (NdefMessage[] msgs) {
        if ( msgs == null || msgs.length == 0 ) return;

        String text = "";
//
        byte[] payload = msgs[ 0 ].getRecords ()[ 0 ].getPayload ();
        String textEncoding = ((payload[ 0 ] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[ 0 ] & 0063; // Get the Language Code, e.g. "en"


        try {
            // Get the Text
            text = new String ( payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace ();
            Log.e ( "xxxx", "UnsupportedEncoding", e );
        }
        Log.e ( "xxx", "buildTagViews: " + text );
        String[] value_split = text.split ( "\\|" );
        if ( value_split.length != 3 ) {
            nifftyDialogs.messageOkError ( "Error", "Error .Card Compromised" );
            Toast.makeText ( context, "Error .Card Compromised", Toast.LENGTH_SHORT ).show ();
            return;
        }


        if ( value_split.length == 3 ) {
            accountName = value_split[ 0 ];
            cardNumber = value_split[ 1 ];
            balance = value_split[ 2 ];
            Alltxt = text;
            status.setText ( "Account : " + accountName + "\n Car Number :" + cardNumber + "\n Current Balance :" + balance );
            if ( isNetworkAvailable ( context ) ) {
                new MakeChecks ().execute ( "cardNumber=" + cardNumber );
            } else {
                status.setText ( "No Internet Connection" );
                nifftyDialogs.messageOkError ( "Connection Error", "No Internet Connection" );

            }
        }

    }

    @Override
    public void onPause () {
        super.onPause ();
        WriteModeOff ();
    }

    @Override
    public void onResume () {
        super.onResume ();
        WriteModeOn ();
    }

    /**
     * Enable Write
     */
    private void WriteModeOn () {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch ( this, pendingIntent, writeTagFilters, null );
    }

    /**
     * Disable Write
     */
    private void WriteModeOff () {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch ( this );
    }

    private void showProgressDialog (final boolean isToShow) {

        if ( isToShow ) {
            if ( ! progressDialog.isShowing () ) {
                progressDialog.setMessage ( "Processing.Keep Card very Close until Done!" );
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
        nifftyDialogs = new NifftyDialogs ( this );
        progressDialog = new ProgressDialog ( this );
        nfcAdapter = NfcAdapter.getDefaultAdapter ( this );
        nifftyDialogs = new NifftyDialogs ( context );
        if ( nfcAdapter == null ) {
            // Stop here, we definitely need NFC
            Toast.makeText ( this, "This device doesn't support NFC.", Toast.LENGTH_LONG ).show ();
            finish ();
            return;
        }
        readFromIntent ( getIntent () );
        pendingIntent = PendingIntent.getActivity ( this, 0, new Intent ( this, getClass () ).addFlags ( Intent.FLAG_ACTIVITY_SINGLE_TOP ), 0 );
        IntentFilter tagDetected = new IntentFilter ( NfcAdapter.ACTION_TAG_DISCOVERED );
        tagDetected.addCategory ( Intent.CATEGORY_DEFAULT );
        writeTagFilters = new IntentFilter[]{tagDetected};
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        int id = item.getItemId ();
        if ( id == android.R.id.home ) {
            onBackPressed ();
            return true;
        }
        return super.onOptionsItemSelected ( item );
    }
}
