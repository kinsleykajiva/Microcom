package microcom.zw.com.microcom;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * Created by Kajiva Kinsley on 2/5/2018.
 */

public class App extends Application {
    @Override
    public void onCreate () {
        super.onCreate ();
        Realm.init ( this );
        Realm.setDefaultConfiguration (
                new RealmConfiguration.Builder ()
                        .deleteRealmIfMigrationNeeded ()
                        .build ()
        );

    }
}
