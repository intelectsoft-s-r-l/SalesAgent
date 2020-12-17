package md.intelectsoft.salesagent.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.MainActivity;
import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.SplashActivity;

public class FragmentLanguage extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_language, container, false);

        ConstraintLayout cslLang = view.findViewById(R.id.csl_language_application);
        TextView selectedLang = view.findViewById(R.id.txt_selected_lang);

        String[] languageList = {"English","Русский","Română"};

        String lang = LocaleHelper.getLanguage(getContext());

        if(lang.equals("ru"))
            selectedLang.setText("Выбран язык: Русский");
        else if(lang.equals("ro"))
            selectedLang.setText("Limba selectata: Romina");
        else if(lang.equals("en"))
            selectedLang.setText("Selected language: English");

        cslLang.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle("Atentie")
                    .setItems(languageList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i){
                                case 0:{
                                    selectedLang.setText("Selected language: English");
                                    dialogInterface.dismiss();
                                    LocaleHelper.setLocale(getContext(),"en");
                                }break;
                                case 1:{
                                    selectedLang.setText("Выбран язык: Русский");
                                    dialogInterface.dismiss();
                                    LocaleHelper.setLocale(getContext(),"ru");
                                }break;
                                case 2:{
                                    selectedLang.setText("Limba selectata: Romina");
                                    dialogInterface.dismiss();
                                    LocaleHelper.setLocale(getContext(),"ro");

                                }break;
                            }

                            Activity activity = MainActivity.getActivity();
                            Intent start = new Intent(getContext(), SplashActivity.class);
                            activity.finish();
                            activity.startActivity(start);

                        }
                    })
                    .setCancelable(false)
                    .setPositiveButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        return view;
    }
}
