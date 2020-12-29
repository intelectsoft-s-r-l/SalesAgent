package md.intelectsoft.salesagent.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import md.intelectsoft.salesagent.R;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

public class FragmentInformation extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);

        TextView appVersion = view.findViewById(R.id.textAppVersion);
        TextView licenseCode = view.findViewById(R.id.textLicenseCode);

        //Add license info in settings

        String license = getContext().getSharedPreferences(sharedPreferenceSettings, Context.MODE_PRIVATE).getString("LicenseCode","");
        licenseCode.setText(license);

        appVersion.setText(getString(R.string.app_name) + " v: " + getAppVersion(getContext()));

        return view;
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
}
