package md.intelectsoft.salesagent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import md.intelectsoft.salesagent.AppUtils.BrokerServiceEnum;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.BrokerServiceUtils.Body.SendGetURI;
import md.intelectsoft.salesagent.BrokerServiceUtils.Body.SendRegisterApplication;
import md.intelectsoft.salesagent.BrokerServiceUtils.BrokerRetrofitClient;
import md.intelectsoft.salesagent.BrokerServiceUtils.BrokerServiceAPI;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.AppDataRegisterApplication;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.RegisterApplication;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.AuthorizeUser;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.TokenResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;
@SuppressLint("NonConstantResourceId")
public class StartActivity extends AppCompatActivity {
    @BindView(R.id.formRegisterApp) ConstraintLayout registerForm;
    @BindView(R.id.formAuthorization) ConstraintLayout authForm;
    @BindView(R.id.textAppName) TextView textAppName;

    // Register form views
    @BindView(R.id.layoutCode) TextInputLayout inputLayoutIdno;
    @BindView(R.id.inputCode) TextInputEditText inputEditTextCode;
    //Sign in form views
    @BindView(R.id.layoutLogin) TextInputLayout inputLayoutLogin;
    @BindView(R.id.layoutPasswordLogin) TextInputLayout inputLayoutPasswordLogin;
    @BindView(R.id.inputLogin) TextInputEditText inputEditTextLogin;
    @BindView(R.id.inputPasswordLogin) TextInputEditText inputEditTextPasswordLogin;

    SharedPreferences sharedPreferencesSettings;
    BrokerServiceAPI brokerServiceAPI;
    OrderServiceAPI orderServiceAPI;
    ProgressDialog progressDialog;
    Context context;
    String androidID, deviceName, publicIp, privateIp, deviceSN, osVersion, deviceModel;

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

    @OnClick(R.id.registerButton) void registerDeviceToBrokerService() {
        String codeLicense = inputEditTextCode.getText().toString();

        if(codeLicense.equals("")){
            inputLayoutIdno.setError(getString(R.string.input_filed_error));
        }
        else{
            //data send to register app in broker server
            SendRegisterApplication registerApplication = new SendRegisterApplication();

            String ids = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
            registerApplication.setDeviceID(ids);
            registerApplication.setDeviceModel(deviceModel);
            registerApplication.setDeviceName(deviceName);
            registerApplication.setSerialNumber(deviceSN);
            registerApplication.setPrivateIP(privateIp);
            registerApplication.setPublicIP(publicIp);
            registerApplication.setOSType(BrokerServiceEnum.Android);
            registerApplication.setApplicationVersion(getAppVersion(this));
            registerApplication.setProductType(BrokerServiceEnum.SalesAgent);
            registerApplication.setOSVersion(osVersion);
            registerApplication.setLicenseActivationCode(codeLicense);

            Log.e("TAG", "registerDeviceToBrokerService: "
                    + "setDeviceID: " + ids
                    + "\n setDeviceModel: " + deviceModel
                    + "\n setDeviceName: " + deviceName
                    + "\n setSerialNumber: " + deviceSN
                    + "\n setPrivateIP: " + privateIp
                    + "\n setPublicIP: " + publicIp
                    + "\n setOSType: " + BrokerServiceEnum.Android
                    + "\n setApplicationVersion: " + getAppVersion(this)
                    + "\n setProductType: " + BrokerServiceEnum.SalesAgent
                    + "\n setOSVersion: " + osVersion
                    + "\n setLicenseActivationCode: " + codeLicense);



            registerApplicationToBroker(registerApplication, codeLicense);
        }

    }

    @OnClick(R.id.loginButton) void authorizeUser() {
        String userName = inputEditTextLogin.getText().toString();
        String userPass = inputEditTextPasswordLogin.getText().toString();

        if(userName.equals("") && userPass.equals("")){
            inputLayoutLogin.setError(getString(R.string.input_filed_error));
            inputLayoutPasswordLogin.setError(getString(R.string.input_filed_error));
        }
        else{
            if(userName.equals("") || userPass.equals("")){
                if(userName.equals(""))
                   inputLayoutLogin.setError(getString(R.string.input_filed_error));
                if(userPass.equals(""))
                    inputLayoutPasswordLogin.setError(getString(R.string.input_filed_error));
            }
            else{
                String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
                Log.e("TAG", "authorizeUser uri: " + uri );
                authorizationToOrderService(uri,userName, userPass);
            }
        }
    }

    @OnCheckedChanged(R.id.keepMeSigned) void keepMeSigned(boolean b){
        sharedPreferencesSettings.edit().putBoolean("KeepMeSigned", b).apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);

        AskForPermissions();

        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        context = this;
        brokerServiceAPI = BrokerRetrofitClient.getApiBrokerService();
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings,MODE_PRIVATE);
        progressDialog = new ProgressDialog(context);

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hideSystemUI();
            }
        });

        String licenseID = sharedPreferencesSettings.getString("LicenseID", null);
        Log.d("TAG", "onCreate broker ID: " + licenseID);

        deviceModel = Build.MODEL;
        deviceSN = Build.SERIAL;
        deviceName = Build.DEVICE;
        osVersion = Build.VERSION.RELEASE;
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
        publicIp = getPublicIPAddress(this);
        privateIp = getIPAddress(true);

        TelephonyManager sd = (TelephonyManager) StartActivity.this.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        String test = sd.getSimOperatorName();

        Log.e("TAG", "registerDeviceToBrokerService: "
                + "\n setDeviceModel: " + deviceModel
                + "\n setDeviceName: " + deviceName
                + "\n setSerialNumber: " + deviceSN
                + "\n setPrivateIP: " + privateIp
                + "\n setPublicIP: " + publicIp
                + "\n setOSType: " + BrokerServiceEnum.Android
                + "\n setApplicationVersion: " + getAppVersion(this)
                + "\n setOSVersion: " + test);

        sharedPreferencesSettings.edit().putString("DeviceID",deviceId).apply();
        sharedPreferencesSettings.edit().putString("AndroidDeviceID",androidID).apply();

        //check installation id , if it's null set register form visibility and authorization "gone" and set first Start true for next synchronization
        if (licenseID == null) {
            authForm.setVisibility(View.GONE);
            sharedPreferencesSettings.edit().putBoolean("FirstStart",true).apply();
        }
        else {
            registerForm.setVisibility(View.GONE);
            authForm.setVisibility(View.VISIBLE);

            String code = sharedPreferencesSettings.getString("LicenseActivationCode","");
            //check URI and installation id
            getURI(licenseID, code,false);
        }

        inputEditTextLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutLogin.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputEditTextPasswordLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutPasswordLogin.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputEditTextCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutIdno.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void AskForPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int readpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int READ_PHONEpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (READ_PHONEpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void registerApplicationToBroker(SendRegisterApplication registerApplication, String activationCode) {
        Call<RegisterApplication> registerApplicationCall = brokerServiceAPI.registerApplicationCall(registerApplication);
        progressDialog.setMessage(getString(R.string.dialog_msg_register_device));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(-1, getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                registerApplicationCall.cancel();
                if(registerApplicationCall.isCanceled())
                    dialog.dismiss();
            }
        });
        progressDialog.show();

        registerApplicationCall.enqueue(new Callback<RegisterApplication>() {
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
                                .putString("LicenseActivationCode",activationCode)
                                .apply();

                        //after register app ,get URI for accounting system on broker server
                        progressDialog.dismiss();

                        registerForm.setVisibility(View.GONE);
                        authForm.setVisibility(View.VISIBLE);

                        if(appDataRegisterApplication.getURI() != null && !appDataRegisterApplication.getURI().equals("") && appDataRegisterApplication.getURI().length() > 5){
                            long nowDate = new Date().getTime();
                            String serverStringDate = appDataRegisterApplication.getServerDateTime();
                            serverStringDate = serverStringDate.replace("/Date(","");
                            serverStringDate = serverStringDate.replace("+0200)/","");
                            serverStringDate = serverStringDate.replace("+0300)/","");

                            long serverDate = Long.parseLong(serverStringDate);

                            sharedPreferencesSettings.edit()
                                    .putString("URI", appDataRegisterApplication.getURI())
                                    .putLong("DateReceiveURI", nowDate)
                                    .putLong("ServerDate", serverDate)
                                    .apply();
                        }
                        else{

                            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("URL not set!")
                                    .setMessage("The application is not fully configured.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        finish();
                                    })
                                    .setNegativeButton("Retry",((dialogInterface, i) -> {
                                        getURI(appDataRegisterApplication.getLicenseID(), activationCode, true);
                                    }))
                                    .show();

                        }
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, getString(R.string.failure_connect) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getURI(String licenseID, String codeActivation, boolean fromRegistration) {
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

        if (fromRegistration) {
            progressDialog.setMessage(getString(R.string.dialog_msg_obtain_uri));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setButton(-1, getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getURICall.cancel();
                    if (getURICall.isCanceled())
                        dialog.dismiss();
                }
            });
            progressDialog.show();
        }

        getURICall.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();
                if (result == null){
                    progressDialog.dismiss();
                    Toast.makeText(context, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                    //check installation id if valid from broker service
                    checkApplicationToUse();
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

                            //check installation id if valid from broker service
                            checkApplicationToUse();
                        }else{
                            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("URL not set!")
                                    .setMessage("The application is not fully configured.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        finish();
                                    })
                                    .setNegativeButton("Retry",((dialogInterface, i) -> {
                                        getURI(licenseID, codeActivation, fromRegistration);
                                    }))
                                    .show();
                        }
                    }
                    else if(result.getErrorCode() == 133){
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
                                    authForm.setVisibility(View.GONE);
                                    registerForm.setVisibility(View.VISIBLE);
                                })
                                .setNegativeButton("Cancel",((dialogInterface, i) -> {
                                    finish();
                                }))
                                .show();
                    }
                    else {
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        //check installation id if valid from broker service
                        checkApplicationToUse();
                    }
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();

                //check installation id if valid from broker service
                checkApplicationToUse();
            }
        });
    }

    private void checkApplicationToUse() {
        boolean restriction = false;

        // get all date for check it
        long dateReceiveURI = sharedPreferencesSettings.getLong("DateReceiveURI",0);
        long oneDay = 86400000;
        long dateLimitCanUseApp = dateReceiveURI + (oneDay * 60);
        long brokerServerDate = sharedPreferencesSettings.getLong("BrokerServerDate",0);
        long currentDate = new Date().getTime();

        //check if user can use application
        restriction = currentDate < dateLimitCanUseApp && currentDate > brokerServerDate;

        //check if user can use application
        if (restriction){
            //TODO add restriction
        }
    }

    private void authorizationToOrderService(String uri, String userName, String userPass) {
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);

        Log.e("TAG", "authorizationToOrderService: " + orderServiceAPI.toString() );

        String deviceID = sharedPreferencesSettings.getString("DeviceID","");
        Call<AuthorizeUser> authorizeUserCall = orderServiceAPI.authorizeUser(deviceID, userName, userPass);

        progressDialog.setMessage(getString(R.string.dialog_msg_authorize_user));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(-1, getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                authorizeUserCall.cancel();
                if(authorizeUserCall.isCanceled())
                    dialog.dismiss();
            }
        });
        progressDialog.show();

        Log.e("TAG", "authorizeUserCall:  "  + authorizeUserCall.request());

        authorizeUserCall.enqueue(new Callback<AuthorizeUser>() {
            @Override
            public void onResponse(Call<AuthorizeUser> call, Response<AuthorizeUser> response) {

                Log.e("TAG", "onResponse:  "  + call.request());

                AuthorizeUser user = response.body();
                if(user != null){
                    if(user.getErrorCode() == 0){
                        TokenResult token = user.getToken();

                        String tokenId = token.getUid();
                        String dateValid = token.getValidTo();
                        if (dateValid != null) {
                            if (dateValid != null)
                                dateValid = dateValid.replace("/Date(", "");
                            if (dateValid != null)
                                dateValid = dateValid.replace("+0200)/", "");
                            if (dateValid != null)
                                dateValid = dateValid.replace("+0300)/", "");
                            if (dateValid != null)
                                dateValid = dateValid.replace(")/", "");

                            long timeValid = Long.parseLong(dateValid);

                            String userFullName = user.getUser().getName() != null ? user.getUser().getName() : "" + user.getUser().getSurname() != null ? user.getUser().getSurname(): "";
                            sharedPreferencesSettings.edit()
                                    .putLong("tokenValid",timeValid)
                                    .putString("token",tokenId)
                                    .putString("UserName",userName)
                                    .putString("UserPass",userPass)
                                    .putString("UserAuthFullName",userFullName)
                                    .apply();
                            progressDialog.dismiss();
                            finish();
                            startActivity(new Intent(context, MainActivity.class));
                        }
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error authorize user: " + user.getErrorCode() + " Message: " + user.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(context, "User list is empty", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthorizeUser> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure authorize user: " + t.getMessage() + " : " + call.request(), Toast.LENGTH_SHORT).show();
                Log.e("TAG", "onFailure:  "  + call.request());
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            hideSystemUI();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }

        return super.dispatchTouchEvent(event);
    }
}