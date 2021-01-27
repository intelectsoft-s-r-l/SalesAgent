package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.fragments.FragmentInformation;
import md.intelectsoft.salesagent.fragments.FragmentLanguage;
import md.intelectsoft.salesagent.fragments.FragmentSync;
import md.intelectsoft.salesagent.fragments.FragmentWorkPlace;

@SuppressLint("NonConstantResourceId")
public class SettingsActivity extends AppCompatActivity {
    @BindView(R.id.settingsAppName) TextView settingsNameActivity;
    @BindView(R.id.txt_start_settings_msg) TextView startMsg;
    @BindView(R.id.container_setting) FrameLayout frmContainer;

    @OnClick(R.id.csl_sync) void onSyncLayout() {
        FragmentSync fragmentSyncPage = new FragmentSync();
        replaceFragment(fragmentSyncPage);
    }

    @OnClick(R.id.csl_workplace) void onWorkPlaceLayout() {
        FragmentWorkPlace work = new FragmentWorkPlace();
        replaceFragment(work);
    }

    @OnClick(R.id.csl_information) void onInformationLayout() {
        FragmentInformation fragmentInformation = new FragmentInformation();
        replaceFragment(fragmentInformation);
    }

    @OnClick(R.id.csl_language) void onLanguageLayout() {
        FragmentLanguage fragmentLanguage = new FragmentLanguage();
        replaceFragment(fragmentLanguage);
    }

    @OnClick(R.id.textBackMainFromSettings) void onBack(){
        finish();
    }

    FragmentManager fgManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        setNameTextColor(getString(R.string.settings_name_activity));
        fgManager = getSupportFragmentManager();
    }

    private void setNameTextColor(String text){
        SpannableString s = new SpannableString("Sales Agent - " + text);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);
        settingsNameActivity.setText(s);
    }

    private void replaceFragment(Fragment fragment){
        if(startMsg.getVisibility() == View.GONE){
            fgManager.beginTransaction().replace(R.id.container_setting,fragment).commit();
        }
        else{
            frmContainer.setVisibility(View.VISIBLE);
            startMsg.setVisibility(View.GONE);
            fgManager.beginTransaction().replace(R.id.container_setting,fragment).commit();
        }
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

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
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