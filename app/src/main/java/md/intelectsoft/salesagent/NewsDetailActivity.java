package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.NewsList;

@SuppressLint("NonConstantResourceId")
public class NewsDetailActivity extends AppCompatActivity {
    @BindView(R.id.newsDetailAppName) TextView headerNewsName;
    @BindView(R.id.newsListAppNewsDetailHeader) TextView headerActivityName;
    @BindView(R.id.textContentDetailNews) TextView content;
    @BindView(R.id.textDateDetailNews) TextView date;
    @BindView(R.id.imageDetailNews) ImageView photo;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
    Realm mRealm;

    @OnClick(R.id.textBackMainFromNewsDetail) void onBack() {finish();}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_news_detail);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);


        mRealm = Realm.getDefaultInstance();
        simpleDateFormat.setTimeZone(tzInChisinau);
        String id = getIntent().getStringExtra("id");

        setNameTextColor();

        NewsList item = mRealm.where(NewsList.class).equalTo("internID", id).findFirst();

        if(item != null){
            headerNewsName.setText(Html.fromHtml(item.getHeader()));
            date.setText(simpleDateFormat.format(item.getDateLong()));


            Log.e("TAG", "onCreate: " +  item.getHeader());
            content.setText(Html.fromHtml(item.getContent()));

            byte[] decodedString = Base64.decode(item.getPhoto(), Base64.DEFAULT);
            Bitmap photoNews = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            photo.setImageBitmap(photoNews);
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

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + getString(R.string.detail_news_header_activity));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
    }
}