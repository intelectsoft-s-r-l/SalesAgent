package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import md.intelectsoft.salesagent.Adapters.AdapterListNews;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.NewsList;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

@SuppressLint("NonConstantResourceId")
public class NewsActivity extends AppCompatActivity {
    @BindView(R.id.list_news) ListView newsList;
    @BindView(R.id.newsListAppName) TextView headerActivityName;

    SimpleDateFormat sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
    SharedPreferences sharedPreferencesSettings;
    AdapterListNews adapterListNews;
    Context context;
    Realm mRealm;

    @OnClick(R.id.textBackMainFromNews) void back(){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        setNameTextColor();
        context = this;
        mRealm = Realm.getDefaultInstance();
        sdfChisinau.setTimeZone(tzInChisinau);
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);

        newsList.setOnItemClickListener((parent, view, position, id) -> {
            String idNews = adapterListNews.getItem(position).getInternID();

            Intent detail = new Intent(context, NewsDetailActivity.class);
            detail.putExtra("id",idNews);
            startActivity(detail);
        });

        showNews();
    }

    private void showNews() {
        mRealm.beginTransaction();
        RealmResults<NewsList> result = mRealm.where(NewsList.class).findAllAsync();

        mRealm.commitTransaction();

        adapterListNews = new AdapterListNews(result,context);
        newsList.setAdapter(adapterListNews);

        assert result != null;
        result.addChangeListener(realmChangeListener);
    }

    private RealmChangeListener<RealmResults<NewsList>> realmChangeListener = new RealmChangeListener<RealmResults<NewsList>>() {
        @Override
        public void onChange(RealmResults<NewsList> news) {
            adapterListNews.updateData(news);
        }
    };

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + getString(R.string.news_header_title));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
    }
}