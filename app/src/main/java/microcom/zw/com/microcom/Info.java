package microcom.zw.com.microcom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import static microcom.zw.com.microcom.Utils.androidId;
import static microcom.zw.com.microcom.Utils.getPhoneNumber;

public class Info extends AppCompatActivity {
private TextView deviceID ;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_info );
        initObjects();
        initViews();

        deviceID.setText ( androidId(this) + "\n" + getPhoneNumber(this));
    }

    private void initViews () {
        getSupportActionBar().setTitle ( "Check Balance" );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        deviceID = findViewById ( R.id.deviceID  );
    }

    private void initObjects () {

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
