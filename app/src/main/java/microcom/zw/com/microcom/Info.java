package microcom.zw.com.microcom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        deviceID = findViewById ( R.id.deviceID  );
    }

    private void initObjects () {

    }
}
