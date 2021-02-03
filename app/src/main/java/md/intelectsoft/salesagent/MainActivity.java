package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import md.intelectsoft.salesagent.AppUtils.BaseEnum;
import md.intelectsoft.salesagent.AppUtils.BrokerServiceEnum;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.BrokerServiceUtils.Body.InformationData;
import md.intelectsoft.salesagent.BrokerServiceUtils.Body.SendGetURI;
import md.intelectsoft.salesagent.BrokerServiceUtils.BrokerRetrofitClient;
import md.intelectsoft.salesagent.BrokerServiceUtils.BrokerServiceAPI;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.AppDataRegisterApplication;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.ErrorMessage;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.GetNews;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.NewsList;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.RegisterApplication;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.AssortmentList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.AuthorizeUser;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientPriceLists;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.PriceList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.RequestList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.TokenResult;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentImages.GetAssortmentImages;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentImages.ImageAssortment;
import md.intelectsoft.salesagent.OrderServiceUtils.body.AssortmentImages;
import md.intelectsoft.salesagent.RealmUtils.Assortment;
import md.intelectsoft.salesagent.RealmUtils.Client;
import md.intelectsoft.salesagent.RealmUtils.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceAssortment;
import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;
@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view_menu) NavigationView navigationView;

    @BindView(R.id.mainAppName) TextView mainAppName;
    @BindView(R.id.textAgentFullName) TextView agentFullName;
    @BindView(R.id.textLastVisitAgent) TextView lastVisitSessionValid;

    @BindView(R.id.textShowAll) TextView textShowAll;
    @BindView(R.id.textShowAllConfirmed) TextView textShoAllConfirmed;
    @BindView(R.id.textShowAllInQueue) TextView textShowAllInQueue;
    @BindView(R.id.textShowAllDraft) TextView textShowAllDraft;

    @BindView(R.id.countAllOrders) TextView countAllOrders;
    @BindView(R.id.countConfirmedOrders) TextView countConfirmedOrders;
    @BindView(R.id.countInQueueOrders) TextView countInQueueOrders;
    @BindView(R.id.countDraftOrders) TextView countDraftOrders;

    @BindView(R.id.textViewShortNameUser) TextView shortNameAgent;

    String androidID, deviceName, publicIp, privateIp, deviceSN, osVersion, deviceModel;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");

    SharedPreferences sharedPreferencesSettings;
    SharedPreferences sharedPreferencesAssortment;
    OrderServiceAPI orderServiceAPI;
    BrokerServiceAPI brokerServiceAPI;
    ProgressDialog progressDialog;
    Realm mRealm;
    String token;
    Context context;

    static Activity activity;

    public static Activity getActivity() {
        return activity;
    }

    @OnClick(R.id.buttonMenuHamburger) void openDrawer(){
        drawer.openDrawer(GravityCompat.START);
    }

    @OnClick(R.id.buttonNewOrder) void createOrder(){
        Intent requestActivity = new Intent(MainActivity.this, ClientOrderActivity.class);
        startActivityForResult(requestActivity,1);
    }
    @OnClick(R.id.layoutButtonProductList) void openProductList(){
        Intent requestActivity = new Intent(MainActivity.this, ProductsActivity.class);
        startActivity(requestActivity);
    }
    @OnClick(R.id.layoutButtonOrderList) void openOrderList(){
        Intent requestActivity = new Intent(MainActivity.this, OrdersListActivity.class);
        requestActivity.putExtra("TypeOrders", 100);

        startActivityForResult(requestActivity, 1);
    }
    @OnClick(R.id.layoutButtonClientHistory) void openClientHistory(){
        Intent requestActivity = new Intent(MainActivity.this, ClientHistoryActivity.class);

        startActivity(requestActivity);
    }
    @OnClick(R.id.layoutButtonClientList) void openClientList(){
        Intent clientsActivity = new Intent(MainActivity.this, ClientActivity.class);

        startActivityForResult(clientsActivity, 1);
    }
    @OnClick(R.id.layoutButtonPointSales) void openDailyReport(){
        Intent clientsActivity = new Intent(MainActivity.this, ItemKindListActivity.class);

        startActivityForResult(clientsActivity, 1);
    }
    @OnClick(R.id.layoutButtonSettings) void openSettings(){
        Intent clientsActivity = new Intent(MainActivity.this, SettingsActivity.class);

        startActivityForResult(clientsActivity, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);

        setContentView(R.layout.drawer_activity_main);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        activity = this;

        setNameTextColor();

        context = this;
        mRealm = Realm.getDefaultInstance();
        simpleDateFormat.setTimeZone(timeZone);
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        sharedPreferencesAssortment = getSharedPreferences(sharedPreferenceAssortment, MODE_PRIVATE);
        brokerServiceAPI = BrokerRetrofitClient.getApiBrokerService();
        progressDialog = new ProgressDialog(context);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, null, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        String fullName = sharedPreferencesSettings.getString("UserAuthFullName","Sales Agent");
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);
        agentFullName.setText(fullName);
        shortNameAgent.setText(fullName.substring(0,2));


        boolean firstStart = sharedPreferencesSettings.getBoolean("FirstStart", false);
        boolean syncToStart = sharedPreferencesSettings.getBoolean("syncToStart", false);
        long tokenValid = sharedPreferencesSettings.getLong("tokenValid", 0);


        deviceModel = Build.MODEL;
        deviceSN = Build.SERIAL;
        deviceName = Build.DEVICE;
        osVersion = Build.VERSION.RELEASE;
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
        publicIp = getPublicIPAddress(this);
        privateIp = getIPAddress(true);

        if(firstStart)
            synchronizationFromStart(true);
        else if(syncToStart){
            synchronizationFromStart(false);
        }
        else {
            getInformationOrders();
            String code = sharedPreferencesSettings.getString("LicenseActivationCode","");
            String licenseID = sharedPreferencesSettings.getString("LicenseID", null);

            getURI(licenseID, code);
        }

        getNews();

        View headerLayout = navigationView.getHeaderView(0);
        TextView sessionNavValidTo = headerLayout.findViewById(R.id.textSessionValidTo);
        TextView navNameAgent = headerLayout.findViewById(R.id.textNameAgentNav);
        ImageButton logOut = headerLayout.findViewById(R.id.buttonLogOut);
        TextView textShortName = headerLayout.findViewById(R.id.textNavShortNameAgent);

        navigationView.setNavigationItemSelectedListener(this);

        textShortName.setText(fullName.substring(0,2));
        navNameAgent.setText(fullName);
        sessionNavValidTo.setText(getString(R.string.session_valid_text) + simpleDateFormat.format(tokenValid));
        logOut.setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.START);
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.dialog_msg_warning))
                    .setMessage(getString(R.string.dialog_msg_log_out_user))
                    .setPositiveButton(getString(R.string.dialog_button_yes), (dialogInterface, i) -> {
                        sharedPreferencesSettings.edit()
                                .putBoolean("KeepMeSigned",false)
                                .putLong("tokenValid",0)
                                .putString("token", null)
                                .putString("UserName","")
                                .putString("UserPass","")
                                .apply();
                        dialogInterface.dismiss();
                        finish();
                        startActivityForResult(new Intent(context, StartActivity.class), 1);
                    })
                    .setNegativeButton(getString(R.string.dialog_button_no),(dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        lastVisitSessionValid.setText(getString(R.string.session_valid_text) + simpleDateFormat.format(tokenValid));

        textShowAll.setOnClickListener(textShowOrders);
        textShoAllConfirmed.setOnClickListener(textShowOrders);
        textShowAllInQueue.setOnClickListener(textShowOrders);
        textShowAllDraft.setOnClickListener(textShowOrders);
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    private String getPublicIPAddress(Context context) {
        //final NetworkInfo info = NetworkUtils.getNetworkInfo(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();

        RunnableFuture<String> futureRun = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                if ((info != null && info.isAvailable()) && (info.isConnected())) {
                    StringBuilder response = new StringBuilder();

                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) (
                                new URL("http://checkip.amazonaws.com/").openConnection());
                        urlConnection.setRequestProperty("User-Agent", "Android-device");
                        //urlConnection.setRequestProperty("Connection", "close");
                        urlConnection.setReadTimeout(1000);
                        urlConnection.setConnectTimeout(1000);
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setRequestProperty("Content-type", "application/json");
                        urlConnection.connect();

                        int responseCode = urlConnection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {

                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                        }
                        urlConnection.disconnect();
                        return response.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Log.w(TAG, "No network available INTERNET OFF!");
                    return null;
                }
                return null;
            }
        });

        new Thread(futureRun).start();

        try {
            return futureRun.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getAppVersion(Context context){
        String result = "";

        try{
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            result = result.replaceAll("[a-zA-Z] |-","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void getURI(String licenseID, String codeActivation) {
        //data send to register app in broker server
        SendGetURI registerApplication = new SendGetURI();

        String ids = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
        registerApplication.setDeviceID(ids);
        registerApplication.setDeviceModel(deviceModel);
        registerApplication.setDeviceName(deviceName);
        registerApplication.setSerialNumber(deviceSN);
        registerApplication.setPrivateIP(privateIp);
        registerApplication.setPublicIP(publicIp);
        registerApplication.setLicenseID(licenseID);
        registerApplication.setOSType(BrokerServiceEnum.Android);
        registerApplication.setApplicationVersion(getAppVersion(this));
        registerApplication.setProductType(BrokerServiceEnum.SalesAgent);
        registerApplication.setOSVersion(osVersion);

        Call<RegisterApplication> getURICall = brokerServiceAPI.getURICall(registerApplication);

        getURICall.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();
                if (result == null){
                    progressDialog.dismiss();
                    Toast.makeText(context, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(result.getErrorCode() == 0) {
                        AppDataRegisterApplication appDataRegisterApplication = result.getAppData();
                        //if app registered successful , save installation id and company name
                        sharedPreferencesSettings.edit()
                                .putString("LicenseID",appDataRegisterApplication.getLicenseID())
                                .putString("LicenseCode",appDataRegisterApplication.getLicenseCode())
                                .putString("CompanyName",appDataRegisterApplication.getCompany())
                                .putString("CompanyIDNO",appDataRegisterApplication.getIDNO())
                                .apply();

                        if(appDataRegisterApplication.getURI() != null && !appDataRegisterApplication.getURI().equals("") && appDataRegisterApplication.getURI().length() > 5) {
                            long nowDate = new Date().getTime();

                            sharedPreferencesSettings.edit()
                                    .putString("URI", appDataRegisterApplication.getURI())
                                    .putLong("DateReceiveURI", nowDate)
                                    .apply();
                        }else{
                            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("URL not set!")
                                    .setMessage("The application is not fully configured.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        finish();
                                    })
                                    .setNegativeButton("Retry",((dialogInterface, i) -> {
                                        getURI(licenseID, codeActivation);
                                    }))
                                    .show();
                        }
                    }else if(result.getErrorCode() == 133){
                        sharedPreferencesSettings.edit()
                                .putString("LicenseID", null)
                                .putString("LicenseCode","")
                                .putString("CompanyName","")
                                .putString("CompanyIDNO", "")
                                .putBoolean("KeepMeSigned", false)
                                .apply();

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("Application not activated!")
                                .setMessage("The application is not activated! Please activate can you continue.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    finish();
                                })
                                .show();
                    }
                    else if(result.getErrorCode() == 134){
                        sharedPreferencesSettings.edit()
                                .putString("LicenseID", null)
                                .putString("LicenseCode","")
                                .putString("CompanyName","")
                                .putString("CompanyIDNO", "")
                                .putBoolean("KeepMeSigned", false)
                                .apply();

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("License not activated!")
                                .setMessage("The license for this application not activated! Please activate can you continue.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    Intent startActivity = new Intent(context, StartActivity.class);
                                    startActivity(startActivity);
                                    finish();
                                })
                                .setNegativeButton("Cancel",((dialogInterface, i) -> {
                                    finish();
                                }))
                                .show();
                    }
                    else
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNews() {
        int id = sharedPreferencesSettings.getInt("NewsID", 0);

        Call<GetNews> call = brokerServiceAPI.getNews(id, BrokerServiceEnum.SalesAgent);

        call.enqueue(new Callback<GetNews>() {
            @Override
            public void onResponse(Call<GetNews> call, Response<GetNews> response) {
                GetNews newsResponse = response.body();
                if(newsResponse != null && newsResponse.getErrorCode() == 0){
                    List<NewsList> listNews = newsResponse.getNewsList();

                    if(listNews != null && listNews.size() > 0){
                        int localID = id;
                        for (NewsList news : listNews){
                            String photo = news.getPhoto();
                            String dateNews = news.getCreateDate();
                            int idNews = news.getID();

                            if(idNews > localID)
                                localID = idNews;

                            if (dateNews != null)
                                dateNews = dateNews.replace("/Date(", "");
                            if (dateNews != null)
                                dateNews = dateNews.substring(0, dateNews.length() - 7);

                            long dateLong = Long.parseLong(dateNews);

                            if(photo != null && photo.length() > 0){
                                photo = photo.replace("data:image/","");
                                String typePhoto = photo.substring(0,3);

                                switch (typePhoto) {
                                    case "jpe":
                                        photo = photo.replace("jpeg;base64,", "");
                                        break;
                                    case "jpg":
                                        photo = photo.replace("jpg;base64,", "");
                                        break;
                                    case "png":
                                        photo = photo.replace("png;base64,", "");
                                        break;
                                }
                                news.setPhoto(photo);
                            }

                            news.setDateLong(dateLong);
                            news.setInternID(UUID.randomUUID().toString());

                            mRealm.executeTransaction(realm -> realm.insert(news));
                        }

                        sharedPreferencesSettings.edit().putInt("NewsID",localID).apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<GetNews> call, Throwable t) {

            }
        });
    }

    View.OnClickListener textShowOrders = v -> {
        int id = v.getId();

        int intentAction = 0;
        switch (id){
            case R.id.textShowAll: intentAction = 100; break;
            case R.id.textShowAllConfirmed: intentAction = 99; break;
            case R.id.textShowAllInQueue: intentAction = 98; break;
            case R.id.textShowAllDraft: intentAction = 97; break;
        }

        Intent requestActivity = new Intent(MainActivity.this, OrdersListActivity.class);
        requestActivity.putExtra("TypeOrders", intentAction);

        startActivityForResult(requestActivity,1);
    };

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showInformationOfRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        long tokenValid = sharedPreferencesSettings.getLong("tokenValid", 0);
        long currentTimeLong = new Date().getTime();
        if(tokenValid < currentTimeLong){
            String userName = sharedPreferencesSettings.getString("UserName","");
            String userPass = sharedPreferencesSettings.getString("UserPass","");

            authorizationToOrderService(uri, userName, userPass);
        }
    }

    private void synchronizationFromStart(boolean auto){
        //load assortment
        Call<AssortmentList> assortmentListCall = orderServiceAPI.getAssortmentList(token);

        progressDialog.setMessage(getString(R.string.dialog_msg_sync_to_start));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        assortmentListCall.enqueue(new Callback<AssortmentList>() {
            @Override
            public void onResponse(Call<AssortmentList> call, Response<AssortmentList> response) {
                AssortmentList assortmentList = response.body();
                if(assortmentList != null){
                    if(assortmentList.getErrorCode() == 0){
                        RealmList<Assortment> assortments = assortmentList.getAssortment();

                        if(assortments != null && assortments.size() > 0){
                            if(!auto){
                                mRealm.executeTransaction(realm -> {
                                    realm.delete(Assortment.class);
                                });
                            }
                            mRealm.executeTransaction(realm -> {
                                realm.insert(assortments);

                                Map<String, ?> allPreferences = sharedPreferencesAssortment.getAll();

                                for(Map.Entry<String, ?> entry : allPreferences.entrySet()){
                                    Assortment item = realm.where(Assortment.class).equalTo("uid",entry.getKey()).findFirst();
                                    if(item != null){
                                        item.setFavoritOrder((Long) entry.getValue());
                                        item.setFavorit(true);
                                    }
                                    else{
                                        sharedPreferencesAssortment.edit().remove(entry.getKey()).apply();
                                    }
                                }
                            });
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getImagesForAssortment(assortments);
                                }
                            });


                            //load price lists
                            getPriceLists(auto);
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(context, "List on the assortment is empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error load orders! Code & Message: " + assortmentList.getErrorCode() + " & " + assortmentList.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(context, "Null response assortment list!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AssortmentList> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure load orders: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getImagesForAssortment(RealmList<Assortment> assortmentList) {
        List<String> listOfUid = new ArrayList<>();
        for(Assortment item: assortmentList){
            if(!item.getFolder())
                listOfUid.add(item.getUid());
        }
        AssortmentImages imageForDownload = new AssortmentImages();
        imageForDownload.setAssortiment(listOfUid);
        imageForDownload.setTokenUid(token);

        Call<GetAssortmentImages> call = orderServiceAPI.getAssortmentImages(imageForDownload);
        call.enqueue(new Callback<GetAssortmentImages>() {
            @Override
            public void onResponse(Call<GetAssortmentImages> call, Response<GetAssortmentImages> response) {
                GetAssortmentImages getAssortmentImages = response.body();
                if(getAssortmentImages != null){
                    if(getAssortmentImages.getErrorCode() == 0){
                        List<ImageAssortment> images = getAssortmentImages.getImages();
                        if(images != null && images.size() > 0){
                            mRealm.executeTransaction(realm -> {
                                for(ImageAssortment image : images){
                                    if(image.getImage1() != null){
                                        if(image.getImage1().length > 0) {
                                            Assortment item = mRealm.where(Assortment.class).equalTo("uid", image.getUid()).findFirst();
                                            if (item != null)
                                                item.setImage(image.getImage1());
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GetAssortmentImages> call, Throwable t) {

            }
        });
    }

    private void getPriceLists(boolean auto) {
        Call<ClientPriceLists> clientPriceListsCall = orderServiceAPI.getClientPriceLists(token);

        clientPriceListsCall.enqueue(new Callback<ClientPriceLists>() {
            @Override
            public void onResponse(Call<ClientPriceLists> call, Response<ClientPriceLists> response) {
                ClientPriceLists clientPriceLists = response.body();
                if(clientPriceLists != null){
                    if(clientPriceLists.getErrorCode() == 0){
                        RealmList<PriceList> priceLists = clientPriceLists.getPriceLists();

                        if(priceLists != null && priceLists.size() > 0){
                            mRealm.executeTransaction(realm -> {
                                realm.insert(priceLists);
                            });
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(context, "List on the prices is empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error load orders! Code & Message: " + clientPriceLists.getErrorCode() + " & " + clientPriceLists.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(context, "Null response price lists!", Toast.LENGTH_SHORT).show();
                }

                //load client list
                getClients(auto);
            }

            @Override
            public void onFailure(Call<ClientPriceLists> call, Throwable t) {

            }
        });
    }

    private void getClients(boolean auto){
        //load clients
        Call<ClientList> clientListCall = orderServiceAPI.getClients(token);

        clientListCall.enqueue(new Callback<ClientList>() {
            @Override
            public void onResponse(Call<ClientList> call, Response<ClientList> response) {
                ClientList clientList = response.body();
                if(clientList != null){
                    if(clientList.getErrorCode() == 0){
                        RealmList<Client> client = clientList.getClients();

                        if(client != null && client.size() > 0){
                            if(!auto){
                                mRealm.executeTransaction(realm -> {
                                    realm.delete(Client.class);
                                });
                            }

                            mRealm.executeTransaction(realm -> realm.insert(client));

                            long dateSync = new Date().getTime();
                            String lastSync = simpleDateFormat.format(dateSync);
                            sharedPreferencesSettings.edit().putString("lastSync", lastSync).apply();
                            sharedPreferencesSettings.edit().putBoolean("FirstStart",false).apply();
                            progressDialog.dismiss();
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(context, "List on the assortment is empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error load orders! Code & Message: " + clientList.getErrorCode() + " & " + clientList.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(context, "Null response client list!", Toast.LENGTH_SHORT).show();
                }
                getInformationOrders();
            }

            @Override
            public void onFailure(Call<ClientList> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure load orders: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getInformationOrders() {
        RealmResults<Request> realmResults = mRealm.where(Request.class).findAll();

        Call<RequestList> requestListCall = orderServiceAPI.getRequestList(token,"2000-01-01", "2200-01-01", null);

        progressDialog.setMessage(getString(R.string.dialog_msg_get_orders));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        requestListCall.enqueue(new Callback<RequestList>() {
            @Override
            public void onResponse(Call<RequestList> call, Response<RequestList> response) {
                RequestList requestList = response.body();
                if (requestList != null){
                    if(requestList.getErrorCode() == 0){
                        RealmList<Request> requests = requestList.getDocuments();

                        if(requests != null && requests.size() > 0){

                            mRealm.beginTransaction();
                            realmResults.setBoolean("toDelete",false);
                            mRealm.commitTransaction();

                            Log.d("TAG", "onResponse: size intern requests before verification = " + realmResults.size() );

                            for(Request internRequest : realmResults){
                                if(internRequest.getUid() == null) {
                                    Log.d("TAG", "onResponse: uid equals null");
                                    continue;
                                }
                                Log.d("TAG", "onResponse: uid not equals null: " + internRequest.getUid() );

                                boolean intoRemoteList = false;
                                for (Request request : requests){
                                    if (request.getUid().equals(internRequest.getUid())) {
                                        intoRemoteList = true;
                                        break;
                                    }
                                }
                                if (!intoRemoteList) {
                                    Log.d("TAG", "onResponse: order not in remote list: " + internRequest.getClientName());
                                    if(internRequest.getSyncState() != BaseEnum.NeSincronizat){
                                        mRealm.beginTransaction();
                                        internRequest.setToDelete(true);
                                        mRealm.commitTransaction();

                                        Log.d("TAG", "onResponse: order to delete set true: " + internRequest.getClientName());
                                    }
                                }
                            }

                            mRealm.beginTransaction();
                            realmResults.where().equalTo("toDelete", true).findAll().deleteAllFromRealm();
                            mRealm.commitTransaction();

                            Log.d("TAG", "onResponse: size intern requests after verification = " + realmResults.size() );

                            for (Request request : requests){
                                String internUUID = UUID.randomUUID().toString();
                                String uid = request.getUid();
                                boolean exist = false;

                                for(Request internRequest : realmResults){

                                    if(internRequest.getUid() != null){
                                        if(internRequest.getUid().equals(uid)){
                                            exist = true;
                                            mRealm.beginTransaction();
                                            if (!request.getState().equals(internRequest.getState()))
                                                internRequest.setState(request.getState());
                                            if (!request.getSum().equals(internRequest.getSum()))
                                                internRequest.setSum(request.getSum());
                                            if (!request.getComment().equals(internRequest.getComment()))
                                                internRequest.setComment(request.getComment());
                                            mRealm.commitTransaction();
                                            break;
                                        }

                                    }
                                }
                                if(!exist){
                                    request.setInternId(internUUID);
                                    request.setSyncState(0);
                                    String dateDocument = request.getDateValid();
                                    if(dateDocument != null){
                                        if (dateDocument != null)
                                            dateDocument = dateDocument.replace("/Date(", "");
                                        if (dateDocument != null)
                                            dateDocument = dateDocument.substring(0, dateDocument.length() - 7);
                                    }
                                    long date = Long.parseLong(dateDocument);
                                    request.setDateToLong(date);

                                    mRealm.executeTransaction(realm -> {
                                        realm.insert(request);
                                    });
                                }
                            }


                            progressDialog.dismiss();
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(context, "Orders list is empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error load orders! Code & Message: " + requestList.getErrorCode() + " & " + requestList.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(context, "Null response from orders list!", Toast.LENGTH_SHORT).show();
                }

                showInformationOfRequests();
            }

            @Override
            public void onFailure(Call<RequestList> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure load orders: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInformationOfRequests() {
        int countDrafts = 0, countInQueue = 0, countConfirmed = 0, allOrders = 0;
        RealmResults<Request> reqDrafts = mRealm.where(Request.class).equalTo("state", BaseEnum.Draft).findAll();
        RealmResults<Request> reqQueue = mRealm.where(Request.class).equalTo("state",BaseEnum.InQueue).findAll();
        RealmResults<Request> reqConfirmed = mRealm.where(Request.class).equalTo("state", BaseEnum.InWork).findAll();  //confirmed = in work
        RealmResults<Request> reqAll = mRealm.where(Request.class).findAll();
        if(!reqDrafts.isEmpty())
            countDrafts = reqDrafts.size();
        if(!reqQueue.isEmpty())
            countInQueue = reqQueue.size();
        if(!reqConfirmed.isEmpty())
            countConfirmed = reqConfirmed.size();
        if(!reqAll.isEmpty())
            allOrders = reqAll.size();

        countAllOrders.setText(String.valueOf(allOrders));
        countConfirmedOrders.setText(String.valueOf(countConfirmed));
        countInQueueOrders.setText(String.valueOf(countInQueue));
        countDraftOrders.setText(String.valueOf(countDrafts));
    }

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + getString(R.string.main_header_text_title));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        mainAppName.setText(s);
    }

    private void authorizationToOrderService(String uri, String userName, String userPass) {
        progressDialog.setMessage(getString(R.string.dialog_msg_update_token));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        String deviceID = sharedPreferencesSettings.getString("DeviceID","");
        Call<AuthorizeUser> authorizeUserCall = orderServiceAPI.authorizeUser( deviceID, userName, userPass);

        authorizeUserCall.enqueue(new Callback<AuthorizeUser>() {
            @Override
            public void onResponse(Call<AuthorizeUser> call, Response<AuthorizeUser> response) {
                AuthorizeUser user = response.body();
                if(user != null){
                    if(user.getErrorCode() == 0){
                        TokenResult tokenResult = user.getToken();

                        token = tokenResult.getUid();
                        String dateValid = tokenResult.getValidTo();
                        if (dateValid != null) {
                            if (dateValid != null)
                                dateValid = dateValid.replace("/Date(", "");
                            if (dateValid != null)
                                dateValid = dateValid.substring(0, dateValid.length() - 7);
                        }
                            long timeValid = Long.parseLong(dateValid);
                            sharedPreferencesSettings.edit()
                                    .putLong("tokenValid",timeValid)
                                    .putString("token",token)
                                    .apply();
                            lastVisitSessionValid.setText(getString(R.string.session_valid_text) + simpleDateFormat.format(timeValid));

                            getInformationOrders();
                    }
                    else{
                        Toast.makeText(context, "Error authorize user: " + user.getErrorCode() + " Message: " + user.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(context, "User list is empty", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<AuthorizeUser> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure authorize user: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                //TODO change to login form if token is not possible update
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.newsItemMenu) {
            startActivity(new Intent(MainActivity.this, NewsActivity.class));
        }

        else if(id == R.id.syncItemMenu){
            synchronizationFromStart(false);
        }
        else if(id == R.id.diagnosticItemMenu){

            JSONObject informationArray = new JSONObject();
            JSONObject battery = getBatteryInformation(this);
            JSONObject memory = getMemoryInformation();
            JSONObject cpu = getCPUInformation();
            JSONObject wifi = getWIFIInformation();

            try {

                informationArray.put("Battery", battery);
                informationArray.put("Memory", memory);
                informationArray.put("CPU", cpu);
                informationArray.put("WiFi", wifi);

                Log.e("TAG", "onNavigationItemSelected JSON array: " + informationArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String licenseID = sharedPreferencesSettings.getString("LicenseID", null);

            InformationData data = new InformationData();
            data.setLicenseID(licenseID);
            data.setInformation(informationArray.toString());

            Call<ErrorMessage> call = brokerServiceAPI.updateDiagnosticInfo(data);
            call.enqueue(new Callback<ErrorMessage>() {
                @Override
                public void onResponse(Call<ErrorMessage> call, Response<ErrorMessage> response) {
                    ErrorMessage message = response.body();
                    if(message.getErrorCode() == 0)
                        Toast.makeText(context, "Diagnostic report send!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, "Diagnostic report not send!Message: " + message.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ErrorMessage> call, Throwable t) {
                    Toast.makeText(context, "Diagnostic report not send!" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private JSONObject getBatteryInformation(Context context) {
        JSONObject battery = new JSONObject();

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);


        try {
            battery.put("Level", level);
            battery.put("Voltage", voltage);
            battery.put("Plugged", plugged);
            battery.put("Status", status);
            battery.put("Health", health);
            battery.put("Temperature", temperature);
            battery.put("Technology", technology);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return battery;
    }
    private JSONObject getMemoryInformation(){
        JSONObject memory = new JSONObject();

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        double installed = mi.totalMem / 0x100000L;
        double available = mi.availMem / 0x100000L;
        double used = (installed  - available);

        try {
            memory.put("Installed", installed + " MB");
            memory.put("Free", available + " MB");
            memory.put("Used", used + " MB");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return memory;
    }
    private JSONObject getCPUInformation(){
        JSONObject cpu = new JSONObject();


        return cpu;
    }

    private JSONObject getWIFIInformation(){
        JSONObject wifi = new JSONObject();

        WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
        int state = wifiManager.getWifiState();

        switch (state){
            case WifiManager.WIFI_STATE_ENABLED :{

            }
        }

        WifiInfo info = wifiManager.getConnectionInfo();

        String ssid = info.getSSID();

        Log.e("TAG", "getWIFIInformation: " + ssid );

        return wifi;
    }

}