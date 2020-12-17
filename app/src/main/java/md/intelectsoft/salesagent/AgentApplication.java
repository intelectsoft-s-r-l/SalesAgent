package md.intelectsoft.salesagent;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;
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
        final RealmConfiguration configuration = new RealmConfiguration.Builder().name("e_agent.realm").schemaVersion(1).migration(new RealmMigrations()).build();
        Realm.setDefaultConfiguration(configuration);
        Realm.getInstance(configuration);

        instance = this;
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
}
