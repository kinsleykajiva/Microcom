package microcom.zw.com.microcom.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import microcom.zw.com.microcom.utils.NifftyDialogs;
import microcom.zw.com.microcom.R;

import static com.ihongqiqu.util.NetUtil.isNetworkAvailable;
import static microcom.zw.com.microcom.DBAccess.CRUD.savePayment;
import static microcom.zw.com.microcom.utils.Utils.ERROR_DETECTED;
import static microcom.zw.com.microcom.utils.Utils.WRITE_ERROR;
import static microcom.zw.com.microcom.utils.Utils.WRITE_SUCCESS;
import static microcom.zw.com.microcom.utils.Utils.androidId;
import static microcom.zw.com.microcom.utils.Utils.pay;
import static microcom.zw.com.microcom.utils.Utils.playSound;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    private NifftyDialogs nifftyDialogs;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode;
    private String accountName, cardNumber;
    private TextView tvNFCContent, status, tagStatus;
    private Tag myTag;
    private ImageView justSwipe;
    private static boolean isTaskRunning = false;
    private boolean canWrite = false, isDoneWritting = true;
    private MakeCheks makeCheks;
    private String balance = "";

    private Button brnadd;

    private Context context = MainActivity.this;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
        ///   startActivity( new Intent(this, Home.class) );
        initObjects ();
        initViews ();
        animateViewBounce ( justSwipe );

        brnadd.setOnClickListener ( v -> startActivity ( new Intent ( MainActivity.this, AddUser.class ) ) );

    }

    private void animateViewBounce (View v) {
        Animation animation = AnimationUtils.loadAnimation ( context, R.anim.bounce );
        v.startAnimation ( animation );
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

    private void initViews () {
        status = findViewById ( R.id.status );
        tvNFCContent = findViewById ( R.id.nfc_contents );
        tagStatus = findViewById ( R.id.tagStatus );
        brnadd = findViewById ( R.id.brnadd );
        justSwipe = findViewById ( R.id.justSwipe );
        getSupportActionBar ().setTitle ( "Swipa Kombi" );
        getSupportActionBar ().setDisplayHomeAsUpEnabled ( true );
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

    private class MakeCheks extends AsyncTask < String, Void, String > {

        @Override
        protected String doInBackground (String... strings) {
            return pay ( strings[ 0 ] );
        }

        @Override
        protected void onPostExecute (String s) {
            super.onPostExecute ( s );
            showProgressDialog ( false );
            isTaskRunning = false;
            Log.e ( "xxx", "onPostExecute: " + s );
            if ( s.isEmpty () ) {
                isDoneWritting = true;
                status.setText ( "Connection Error" );
                playSound ( context, 2 );
                nifftyDialogs.messageOkError ( "Server Response", "Connection Error" );
                status.setTextColor ( Color.parseColor ( "#ea4a34" ) ); // green
                Toast.makeText ( context, "Connection Error", Toast.LENGTH_LONG ).show ();
                return;
            }
            if ( s.equals ( "none" ) ) {
                isDoneWritting = true;
                playSound ( context, 2 );
                nifftyDialogs.messageOkError ( "Error !", "User doesn't Exist,Register!" );
            }
            if ( s.equals ( "err_update" ) ) {
                playSound ( context, 2 );
                isDoneWritting = true;
                nifftyDialogs.messageOkError ( "Error !", "Transaction Failed !" );
            }
            if ( s.contains ( "broke" ) ) {
                isDoneWritting = true;
                playSound ( context, 2 );
                String[] value_split = s.split ( "\\|" );
                nifftyDialogs.messageOkError ( "Insufficient Funds !", " Balance $" + value_split[ 1 ] );
                tvNFCContent.setText ( "Card Name:" + accountName + "\n Number:" + cardNumber + "\n Newer Balance :" + value_split[ 1 ] );
            }
            if ( s.contains ( "done" ) ) {
                String[] value_split = s.split ( "\\|" );
                nifftyDialogs.messageOk ( "Finished !", "New Balance: $" + value_split[ 1 ] );

                try {
                    if ( myTag == null ) {
                        canWrite = false;
                        Toast.makeText ( context, ERROR_DETECTED, Toast.LENGTH_LONG ).show ();
                        playSound ( context, 2 );
                        writeToCardFailed ( accountName + "|" + cardNumber + "|" + value_split[ 1 ] );
                    } else {
                        write (
                                accountName + "|" + cardNumber + "|" + value_split[ 1 ],

                                myTag
                        );
                        canWrite = true;
                        if ( canWrite ) {
                            playSound ( context, 1 );
                            savePayment ( accountName, cardNumber, androidId ( context ) );
                            tagStatus.setText ( "Saved To Card" );
                            tvNFCContent.append ( "\n\n\n Newer Balance :" + value_split[ 1 ] );
                            Toast.makeText ( context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show ();
                            isDoneWritting = true;
                        } else {
                            writeToCardFailed ( accountName + "|" + cardNumber + "|" + value_split[ 1 ] );
                        }
                    }
                } catch (IOException | FormatException e) {
                    playSound ( context, 2 );
                    canWrite = false;
                    writeToCardFailed ( accountName + "|" + cardNumber + "|" + value_split[ 1 ] );
                    Toast.makeText ( context, WRITE_ERROR, Toast.LENGTH_LONG ).show ();
                    e.printStackTrace ();
                }
            }


        }
    }

    private void writeToCardFailed (String textToWrite) {
        AlertDialog.Builder builder = new AlertDialog.Builder ( context );
        builder.setTitle ( "Writing to Card  Error" );


        builder.setMessage ( "Please put Card Near to POS to save last transaction,Before Loosing information" );


        //Yes Button
        builder.setPositiveButton ( "Write", (dialog, which) -> {
            String[] value_split = textToWrite.split ( "\\|" );
            try {
                write (
                        textToWrite,

                        myTag
                );
                canWrite = true;
                if ( canWrite ) {
                    playSound ( context, 1 );
                    savePayment ( accountName, cardNumber, androidId ( context ) );
                    tagStatus.setText ( "Saved To Card" );

                    tvNFCContent.append ( "\n\n\n Newer Balance :" + value_split[ 1 ] );
                    Toast.makeText ( context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show ();
                    isDoneWritting = true;
                } else {
                    writeToCardFailed ( accountName + "|" + cardNumber + "|" + value_split[ 1 ] );
                }
            } catch (IOException | FormatException e) {
                playSound ( context, 2 );
                canWrite = false;
                writeToCardFailed ( textToWrite );
                Toast.makeText ( context, WRITE_ERROR, Toast.LENGTH_LONG ).show ();
                e.printStackTrace ();
            }
        } );
        builder.setNegativeButton ( "Cancel", (dialog, which) -> {
            isDoneWritting = true;
            Toast.makeText ( getApplicationContext (), "Information Lost", Toast.LENGTH_LONG ).show ();
            dialog.dismiss ();

        } );

        AlertDialog alertDialog = builder.create ();
        alertDialog.setCancelable ( false );
        alertDialog.show ();
    }

    private void buildTagViews (NdefMessage[] msgs) {
        if ( msgs == null || msgs.length == 0 ) {
            return;
        }
        if ( ! isDoneWritting ) {
            return;
        }

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


        if (/*makeCheks.getStatus () != AsyncTask.Status.RUNNING*/ ! isTaskRunning ) {
            if ( value_split.length == 3 ) {
                accountName = value_split[ 0 ];
                cardNumber = value_split[ 1 ];
                balance = value_split[ 2 ];
                tvNFCContent.setText ( "Card Name:" + accountName + "\n Number:" + cardNumber + "\n Balance :" + balance );
                showProgressDialog ( true );
                makeCheks = new MakeCheks ();
                isTaskRunning = true;

                if ( isNetworkAvailable ( context ) ) {
                    isDoneWritting = false;
                    makeCheks.execute ( "accountName=" + accountName + "&cardNumber=" + cardNumber + "&balance=" + balance + "&deviceid=" + androidId ( context ) );
                } else {
                    playSound ( context, 2 );
                    nifftyDialogs.messageOkError ( "Connection Error", "No Internet Connection" );
                }
            }
        }
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
        canWrite = true;
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

    @Override
    public void onPause () {
        super.onPause ();
        WriteModeOff ();
        isDoneWritting = true;
    }

    @Override
    public void onResume () {
        super.onResume ();
        WriteModeOn ();
        isDoneWritting = true;
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
