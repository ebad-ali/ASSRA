package assra.bahria.fyp.Common;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import assra.bahria.fyp.Common.Utils.CookieManager;

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        CookieManager.getInstance().setCookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this)));
    }

}
