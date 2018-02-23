package microcom.zw.com.microcom.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.RealmResults;
import microcom.zw.com.microcom.DBAccess.TransactionPojo;
import microcom.zw.com.microcom.R;

/**
 * Created by Kajiva Kinsley on 2/23/2018.
 */

public class PaidLogsRecycler extends RecyclerView.Adapter<PaidLogsRecycler.CustomViewHolder> {
    private RealmResults<TransactionPojo > results;

    public PaidLogsRecycler ( RealmResults < TransactionPojo > results) {

        this.results = results;
    }
    static class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView descrp, date;

        CustomViewHolder (View view) {
            super(view);
            this.descrp = view.findViewById ( R.id.description );

            this.date =  view.findViewById(R.id.date);

        }
    }
    @Override
    public CustomViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_item, null);
        return new CustomViewHolder(view);
    }
    @Override
    public void onBindViewHolder (CustomViewHolder holder, int position) {
        TransactionPojo feeditem = results.get ( position );

        holder.date.setText (feeditem.getTime_ () +" - " + feeditem.getDate_ ());
        holder.descrp.setText(feeditem.getAccountName () + "  , " + feeditem.getCardNumber ());
    }

    @Override
    public int getItemCount() {

        return (null != results ? results.size() : 0);

    }
}
