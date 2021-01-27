package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.salesagent.Adapters.AdapterClientsList;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.RealmUtils.Client;

@SuppressLint("NonConstantResourceId")
public class ClientActivity extends AppCompatActivity {
    @BindView(R.id.clientsListAppName) TextView headerActivityName;
    @BindView(R.id.gridClients) GridView gridView;
    @BindView(R.id.searchClients) SearchView searchClients;

    TimerTask timerTaskSearchText;
    AdapterClientsList adapterClientsList;
    Context context;
    Realm mRealm;
    Timer timerSearch;

    @OnClick(R.id.textBackMainFromClients) void onBckClick() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_client);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        setNameTextColor();
        context = this;
        mRealm = Realm.getDefaultInstance();

        showClients("",false);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String Uid = adapterClientsList.getItem(position).getUid();
                Intent clientDetail = new Intent(context, ClientDetailActivity.class);
                clientDetail.putExtra("Uid", Uid);
                startActivity(clientDetail);
            }
        });

        searchClients.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                if (timerSearch != null)
                    timerSearch.cancel();
                timerSearch = new Timer();

                startTimerSearchText(searchText);
                timerSearch.schedule(timerTaskSearchText, 1200);

                return true;
            }
        });
        searchClients.setOnCloseListener(() -> {
            showClients("",false);
            return false;
        });
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

    private void startTimerSearchText(final String newText) {
        timerTaskSearchText = new TimerTask() {
            @Override
            public void run() {
                ClientActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (newText.length() >= 3) {
                            showClients(newText,true);
                        }
//                        else if (newText.equals("")) {
//                            showProducts("00000000-0000-0000-0000-000000000000");
//                        }
                    }
                });
            }
        };
    }

    private RealmChangeListener<RealmResults<Client>> realmChangeListener = new RealmChangeListener<RealmResults<Client>>() {
        @Override
        public void onChange(RealmResults<Client> realmResults) {
            adapterClientsList.setData(realmResults);
        }
    };

    private void showClients(String text, boolean search) {
        RealmResults<Client> result = null;
        mRealm.beginTransaction();
        if(search)
        result = mRealm.where(Client.class)
                .contains("name",text, Case.INSENSITIVE)
                .or()
                .contains("iDNP", text, Case.INSENSITIVE)
                .and()
                .notEqualTo("name","")
                .and()
                .notEqualTo("name", " ")
                .and()
                .isNotNull("name")
                .and()
                .not()
                .contains("name", "bank", Case.INSENSITIVE)
                .and()
                .not()
                .contains("name", "banca", Case.INSENSITIVE)
                .sort("name", Sort.ASCENDING).findAllAsync();
        else
            result = mRealm.where(Client.class)
                    .notEqualTo("name","")
                    .and()
                    .notEqualTo("name", " ")
                    .and()
                    .isNotNull("name")
                    .and()
                    .not()
                    .contains("name", "bank", Case.INSENSITIVE)
                    .and()
                    .not()
                    .contains("name", "banca", Case.INSENSITIVE)
                    .sort("name", Sort.ASCENDING).findAllAsync();
        mRealm.commitTransaction();

        adapterClientsList = new AdapterClientsList(this, result);
        gridView.setAdapter(adapterClientsList);

        result.addChangeListener(realmChangeListener);

//        productsRecyclerViewAdapter = new ProductsRecyclerViewAdapter(this, result,false, false);
//        realmRecyclerView.setAdapter(productsRecyclerViewAdapter);
    }

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + getString(R.string.client_title));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
    }
}