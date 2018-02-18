package microcom.zw.com.microcom;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static microcom.zw.com.microcom.Utils.checkDetails;
import static microcom.zw.com.microcom.Utils.saveUser;

public class AddUser extends AppCompatActivity {
private Button brnSave;
private TextView status;
    private ProgressDialog progressDialog;
private EditText Name ,Balance ,number;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_add_user );
        initObjects();
        initViews();
        brnSave.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                String Name_ =Name.getText ().toString ().trim ();
                String Balance_ =Balance.getText ().toString ().trim ();
                String number_ =number.getText ().toString ().trim ();

                if(Name_.isEmpty ()){
                    Toast.makeText (AddUser.this,"Name cant be Empty",Toast.LENGTH_LONG).show();
                    return;
                }

                if(number_.isEmpty ()){
                    Toast.makeText (AddUser.this,"Card Number cant be Empty",Toast.LENGTH_LONG).show();
                    return;
                }
                if(Balance_.isEmpty ()){
                    Toast.makeText (AddUser.this,"Balance cant be Empty",Toast.LENGTH_LONG).show();
                    return;
                }
                showProgressDialog ( true );
                new MakeCheks ().execute ("accountName=" + Name_ +"&cardNumber="+number_ + "&balance="+Balance_   );


            }
        } );
    }
    private class MakeCheks extends AsyncTask< String, Void, String > {

        @Override
        protected String doInBackground (String... strings) {
            return saveUser(strings[0]);
        }

        @Override
        protected void onPostExecute (String s) {
            super.onPostExecute ( s );
            showProgressDialog ( false );
            Log.e ( "xxx", "onPostExecute: "+s  );
            if ( s.equals ( "exists" ) ) {
                status.setText ( "User Already Exists" );
                Toast.makeText (AddUser.this,"User Already Exists",Toast.LENGTH_LONG).show();

            }
            if ( s.equals ( "done" ) ) {
                status.setText ("User Saved. Go Back"  );
                Toast.makeText (AddUser.this,"User Saved. Go Back",Toast.LENGTH_LONG).show();

            }
            if ( s.equals ( "failed" ) ) {
                status.setText ( "Saving failed. Try again" );
                Toast.makeText (AddUser.this,"Saving failed. Try again",Toast.LENGTH_LONG).show();

            }
            /*else{
                Toast.makeText (AddUser.this,"User not Found",Toast.LENGTH_LONG).show();
            }*/

        }
    }
    private void initObjects () {
        progressDialog = new ProgressDialog (this );
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
}
