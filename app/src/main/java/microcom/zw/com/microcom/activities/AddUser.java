package microcom.zw.com.microcom.activities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import static microcom.zw.com.microcom.utils.Utils.saveUser;

public class AddUser extends AppCompatActivity {
    private Button brnSave;
    private TextView status;
    private Tag myTag;
    private Context context = AddUser.this;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode;
    private NifftyDialogs nifftyDialogs;
    private ProgressDialog progressDialog;
    private EditText Name, Balance, number;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_add_user );
        initObjects ();
        initViews ();
        brnSave.setOnClickListener ( v -> {
            String Name_ = Name.getText ().toString ().trim ();
            String Balance_ = Balance.getText ().toString ().trim ();
            String number_ = number.getText ().toString ().trim ();
            boolean canWrite = false;
            if ( Name_.isEmpty () ) {
                Toast.makeText ( AddUser.this, "Name cant be Empty", Toast.LENGTH_LONG ).show ();
                return;
            }

            if ( number_.isEmpty () ) {
                Toast.makeText ( AddUser.this, "Card Number cant be Empty", Toast.LENGTH_LONG ).show ();
                return;
            }
            if ( Balance_.isEmpty () ) {
                Toast.makeText ( AddUser.this, "Balance cant be Empty", Toast.LENGTH_LONG ).show ();
                return;
            }
            if(Integer.parseInt ( Balance_ ) > 50 ){
                Toast.makeText ( context, "Amount Cant exceed 50", Toast.LENGTH_SHORT ).show ();
                return;
            }
            showProgressDialog ( true );
            try {
                if ( myTag == null ) {
                    canWrite = false ;
                    Toast.makeText ( context, ERROR_DETECTED, Toast.LENGTH_LONG ).show ();
                } else {
                    write (
                            Name_ + "|" + number_ + "|" + Balance_,

                            myTag
                    );
                    canWrite = true ;
                    Toast.makeText ( context,WRITE_SUCCESS, Toast.LENGTH_LONG ).show ();
                }
            } catch (IOException e) {
                Toast.makeText ( context, WRITE_ERROR, Toast.LENGTH_LONG ).show ();
                e.printStackTrace ();
            } catch (FormatException e) {
                Toast.makeText ( context,WRITE_ERROR, Toast.LENGTH_LONG ).show ();
                e.printStackTrace ();
            }
            if(canWrite){
                if(isNetworkAvailable( context)) {
                    new MakeCheks ().execute ( "accountName=" + Name_ + "&cardNumber=" + number_ + "&balance=" + Balance_ );
                }else{
                    nifftyDialogs.messageOkError ( "Connection Error" , "No Internet Connection" );
                }
            }


        } );
    }

    private class MakeCheks extends AsyncTask < String, Void, String > {

        @Override
        protected String doInBackground (String... strings) {
            return saveUser ( strings[ 0 ] );
        }

        @Override
        protected void onPostExecute (String s) {
            super.onPostExecute ( s );
            showProgressDialog ( false );
            Log.e ( "xxx", "onPostExecute: " + s );
            if(s.isEmpty ()){
                status.setText ( "Connection Error" );
                nifftyDialogs.messageOkError ("Server Response",  "Connection Error"  );
                status.setTextColor ( Color.parseColor ( "#ea4a34" ) ); // green
                Toast.makeText ( AddUser.this, "Connection Error", Toast.LENGTH_LONG ).show ();
                return;
            }
            if ( s.equals ( "exists" ) ) {
                status.setText ( "User Already Exists" );
                nifftyDialogs.messageOkError ("Server Response",  "User Already Exists"  );
                status.setTextColor ( Color.parseColor ( "#5fba7d" ) ); // green
                Toast.makeText ( AddUser.this, "User Already Exists", Toast.LENGTH_LONG ).show ();

            }
            if ( s.equals ( "done" ) ) {
                status.setText ( "User Saved. Go Back" );
                nifftyDialogs.messageOk ( "User Saved. Go Back"  );
                status.setTextColor ( Color.parseColor ( "#5184c1" ) ); // blue
                Toast.makeText ( AddUser.this, "User Saved. Go Back", Toast.LENGTH_LONG ).show ();

            }
            if ( s.equals ( "failed" ) ) {
                nifftyDialogs.messageOkError ( "Response","Saving failed. Try again"  );
                status.setText ( "Saving failed. Try again" );
                status.setTextColor ( Color.parseColor ( "#ea4a34" ) ); // red
                Toast.makeText ( AddUser.this, "Saving failed. Try again", Toast.LENGTH_LONG ).show ();

            }
            /*else{
                Toast.makeText (AddUser.this,"User not Found",Toast.LENGTH_LONG).show();
            }*/

        }
    }

    private void initObjects () {
        getSupportActionBar ().setTitle ( "Add User" );
        getSupportActionBar ().setDisplayHomeAsUpEnabled ( true );
        nifftyDialogs = new NifftyDialogs ( context );
        progressDialog = new ProgressDialog ( this );
        nfcAdapter = NfcAdapter.getDefaultAdapter ( this );
        if ( nfcAdapter == null ) {
            // Stop here, we definitely need NFC
            Toast.makeText ( this, "This device doesn't support NFC.", Toast.LENGTH_LONG ).show ();
            finish ();
            return;
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity ( this, 0, new Intent ( this, getClass () ).addFlags ( Intent.FLAG_ACTIVITY_SINGLE_TOP ), 0 );
        IntentFilter tagDetected = new IntentFilter ( NfcAdapter.ACTION_TAG_DISCOVERED );
        tagDetected.addCategory ( Intent.CATEGORY_DEFAULT );
        writeTagFilters = new IntentFilter[]{tagDetected};

    }

    @Override
    public void onResume () {
        super.onResume ();
        WriteModeOn ();
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
            //buildTagViews(msgs);
        }
    }
    @Override
    public void onPause () {
        super.onPause ();
        WriteModeOff ();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }
    private void showProgressDialog (final boolean isToShow) {

        if ( isToShow ) {
            if ( ! progressDialog.isShowing () ) {
                progressDialog.setMessage ( "Processing.Keep Card very Close !" );
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
        getSupportActionBar ().setTitle ( "Add User" );

        Name = findViewById ( R.id.Name );
        Balance = findViewById ( R.id.Balance );
        number = findViewById ( R.id.number );
        status = findViewById ( R.id.status );
        brnSave = findViewById ( R.id.brnSave );
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

    private void WriteModeOn () {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch ( this, pendingIntent, writeTagFilters, null );
    }

    private void WriteModeOff () {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch ( this );
    }

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
