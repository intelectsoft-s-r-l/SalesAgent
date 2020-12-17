package md.intelectsoft.salesagent;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_splash);

        ActionBar var10000 = this.getSupportActionBar();
        if (var10000 != null) {
            var10000.hide();
        }

        boolean keepSigned = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE).getBoolean("KeepMeSigned", false);
        String token = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE).getString("token","");

        new Timer().schedule(new TimerTask() {
            public void run() {
                if(keepSigned && !token.equals(""))
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                else
                    startActivity(new Intent(SplashActivity.this, StartActivity.class));
                finish();
            }
        }, 1000);

    }
}