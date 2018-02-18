package microcom.zw.com.microcom;

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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static microcom.zw.com.microcom.MainActivity.ERROR_DETECTED;
import static microcom.zw.com.microcom.MainActivity.WRITE_ERROR;
import static microcom.zw.com.microcom.MainActivity.WRITE_SUCCESS;
import static microcom.zw.com.microcom.Utils.checkDetails;
import static microcom.zw.com.microcom.Utils.saveUser;

public class AddUser extends AppCompatActivity {
    private Button brnSave;
    private TextView status;
    private Tag myTag;
    private Context context = AddUser.this;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode;
    private ProgressDialog progressDialog;
    private EditText Name, Balance, number;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_add_user );
        initObjects ();
        initViews ();
        brnSave.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                String Name_ = Name.getText ().toString ().trim ();
                String Balance_ = Balance.getText ().toString ().trim ();
                String number_ = number.getText ().toString ().trim ();

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
                showProgressDialog ( true );
                try {
                    if ( myTag == null ) {
                        Toast.makeText ( context, ERROR_DETECTED, Toast.LENGTH_LONG ).show ();
                    } else {
                        write (
                                Name_ + "|" + number_ + "|" + Balance_,
                                /*"person|12345|23.00",*/
                                myTag
                        );
                        Toast.makeText ( context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show ();
                    }
                } catch (IOException e) {
                    Toast.makeText ( context, WRITE_ERROR, Toast.LENGTH_LONG ).show ();
                    e.printStackTrace ();
                } catch (FormatException e) {
                    Toast.makeText ( context, WRITE_ERROR, Toast.LENGTH_LONG ).show ();
                    e.printStackTrace ();
                }
                new MakeCheks ().execute ( "accountName=" + Name_ + "&cardNumber=" + number_ + "&balance=" + Balance_ );


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
            if ( s.equals ( "exists" ) ) {
                status.setText ( "User Already Exists" );
                status.setTextColor ( Color.parseColor ( "#5fba7d" ) ); // green
                Toast.makeText ( AddUser.this, "User Already Exists", Toast.LENGTH_LONG ).show ();

            }
            if ( s.equals ( "done" ) ) {
                status.setText ( "User Saved. Go Back" );
                status.setTextColor ( Color.parseColor ( "#5184c1" ) ); // blue
                Toast.makeText ( AddUser.this, "User Saved. Go Back", Toast.LENGTH_LONG ).show ();

            }
            if ( s.equals ( "failed" ) ) {
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
        progressDialog = new ProgressDialog ( this );
        nfcAdapter = NfcAdapter.getDefaultAdapter ( this );
        Intent intent = new Intent ( this, getClass () );
        myTag = intent.getParcelableExtra ( NfcAdapter.EXTRA_TAG );
        if ( nfcAdapter == null ) {
            // Stop here, we definitely need NFC
            Toast.makeText ( this, "This device doesn't support NFC.", Toast.LENGTH_LONG ).show ();
            finish ();
            return;
        }


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

    @Override
    public void onPause () {
        super.onPause ();
        WriteModeOff ();
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
}
