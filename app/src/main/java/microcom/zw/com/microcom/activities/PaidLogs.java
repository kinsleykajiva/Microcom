package microcom.zw.com.microcom.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import microcom.zw.com.microcom.DBAccess.TransactionPojo;
import microcom.zw.com.microcom.R;
import microcom.zw.com.microcom.adapters.PaidLogsRecycler;
import microcom.zw.com.microcom.cwidgets.VerticalSpaceItemDecoration;

public class PaidLogs extends AppCompatActivity {
    private TextView recycViewStatus;
    private RecyclerView recycler_view;
    private PaidLogsRecycler mAdapter;
    private Realm realm;
    private final static int VERTICAL_ITEM_SPACE = 4;//14
    private RealmResults<TransactionPojo > results;
    private Context context = PaidLogs.this;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        initObjects ();
        setContentView ( R.layout.activity_paid_logs );
        initViews ();
        setViewsValues ();

    }

    private void initObjects () {
        realm = Realm.getDefaultInstance();
    }

    protected void onDestroy () {
        super.onDestroy ();
        if (realm != null) {

            realm.close();
        }
    }
    protected void setViewsValues () {

        results = realm.where(TransactionPojo.class).sort("ID", Sort.DESCENDING).findAll ();
        mAdapter = new PaidLogsRecycler( results );
        if(results.size ()==0){

            recycViewStatus.setVisibility ( View.VISIBLE );

        }
        recycler_view.setAdapter(mAdapter);
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
    protected void initViews () {
        getSupportActionBar().setTitle ( "Logs" );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recycler_view = findViewById ( R.id.recyclerview );
        recycViewStatus = findViewById ( R.id.recycViewStatus );
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(new LinearLayoutManager (context));
        recycler_view.addItemDecoration(new VerticalSpaceItemDecoration (VERTICAL_ITEM_SPACE));

    }
}
