package md.intelectsoft.salesagent;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.RealmUtils.RealmMigrations;
import md.intelectsoft.salesagent.RealmUtils.Request;

public class AgentApplication extends Application {
    public static final String sharedPreferenceSettings = "SettingsApplication";
    public static final String sharedPreferenceAssortment = "SettingsAssortment";
    public static AgentApplication instance = null;

    public Request requestToView;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        final RealmConfiguration configuration = new RealmConfiguration.Builder().name("e_agent.realm").schemaVersion(7).migration(new RealmMigrations()).allowWritesOnUiThread(true).build();
        Realm.setDefaultConfiguration(configuration);
        Realm.getInstance(configuration);

        instance = this;

        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
    }

    public static AgentApplication getInstance(){
        return instance;
    }

    public Request getRequestToView() {
        return requestToView;
    }

    public void setRequestToView(Request requestToView) {
        this.requestToView = requestToView;
    }

    private void setAppLocale(String localeCode){
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            config.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }
}
