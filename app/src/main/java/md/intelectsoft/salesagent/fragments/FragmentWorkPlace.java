package md.intelectsoft.salesagent.fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.Assortment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Igor on 02.09.2020
 */

public class FragmentWorkPlace extends Fragment {

    TextView textStateExport, textStateImport;
    SharedPreferences sharedPrefSettings;
    ConstraintLayout layoutExport, layoutImport;
    Context context;
    Realm mRealm;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_workplace, container, false);

        textStateExport = rootViewAdmin.findViewById(R.id.txt_work_state);
        textStateImport = rootViewAdmin.findViewById(R.id.text_count_state_assortment_import);
        layoutExport = rootViewAdmin.findViewById(R.id.csl_export_assortment);
        layoutImport = rootViewAdmin.findViewById(R.id.csl_import_assortment);
        mRealm = Realm.getDefaultInstance();
        context = getContext();
        if(context == null)
            context = getActivity();

        sharedPrefSettings = context.getSharedPreferences("Settings", MODE_PRIVATE);
        progressDialog = new ProgressDialog(context);

        layoutExport.setOnClickListener(view -> {
            progressDialog.setMessage("Exporting assortment...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

            exportFavoriteAssortment();
        });

        layoutImport.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/IntelectSoft/");
            intent.setDataAndType(uri, "text/csv");
            startActivityForResult(Intent.createChooser(intent, "Select file"),152);

//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/IntelectSoft");
//            intent.setDataAndType(uri, "text/*");
//            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivityForResult(intent, 152);
////            startActivityForResult(Intent.createChooser(intent, "Select file"),152);
        });

        return rootViewAdmin;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 152){
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();
                String direct = getPath(uri);
                progressDialog.setMessage("Saving assortment...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
//                readAssortmentFavorite(direct);
            }
        }
    }

    private String readCSV (Uri uri) throws IOException {
        InputStream csvFile = context.getContentResolver().openInputStream(uri);
        InputStreamReader isr = new InputStreamReader(csvFile);
        return new BufferedReader(isr).readLine();
    }

    private void exportFavoriteAssortment() {
        RealmResults<Assortment> result = mRealm.where(Assortment.class).equalTo("isFavorit",true).sort("favoritOrder", Sort.ASCENDING).findAll();
        if(!result.isEmpty()){
            AskForPermissions();
            String fileName = "FavoriteAssortment.csv"; //Name of the file
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extStorageDirectory, "IntelectSoft");// Name of the folder you want to keep your file in the local storage.
            folder.mkdir(); //creating the folder
            File file = new File(folder, fileName);
            try {
                file.createNewFile(); // creating the file inside the folder
            } catch (IOException e1) {
                AskForPermissions();
                e1.printStackTrace();
            }

            FileWriter fw = null;
            try {
                fw = new FileWriter(file);

                for(int i = 0; i < result.size() ; i++){
                    Assortment assortment = result.get(i);

                    fw.append(assortment.getUid());
                    fw.append(';');

                    fw.append(String.valueOf(assortment.getFavoritOrder()));
                    fw.append(';');

                    fw.append(assortment.getName());
                    fw.append(';');

                    fw.append('\n');
                }
                // fw.flush();
                fw.close();
                Toast.makeText(context, "Export successful! ", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                textStateExport.setText(result.size() + " assortment exported! Path saved: " + file.getAbsolutePath());

            } catch (IOException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }

        }
        else{
            progressDialog.dismiss();
            Toast.makeText(context, "No assortment favorite!", Toast.LENGTH_SHORT).show();
        }
    }

//    private void readAssortmentFavorite(String filePath) {
//        try {
////            String uris = Environment.getExternalStorageDirectory() + "/IntelectSoft/FavoriteAssortment.csv";
//
//            File csvfile = new File(filePath);
//
//            CSVReader reader = new CSVReader(new FileReader(csvfile.getAbsolutePath()));
//
//            String[] nextLine;
//            final int[] cnt = {0};
//
//            while ((nextLine = reader.readNext()) != null) {
//
//                String id = nextLine[0].substring( 0, nextLine[0].indexOf(";"));
//                String remainder = nextLine[0].substring(nextLine[0].indexOf(";") + 1, nextLine[0].length());
//                String time = remainder.substring( 0, remainder.indexOf(";"));
//
//                long timeLong = Long.parseLong(time);
//
//                mRealm.executeTransaction(realm -> {
//                    AssortmentList item = realm.where(AssortmentList.class).equalTo("uid",id).findFirst();
//                    if(item != null) {
//                        item.setFavorit(true);
//                        item.setFavoritOrder(timeLong);
//                        cnt[0] +=1;
//                    }
//                });
//            }
//
//            Toast.makeText(context, "Import finis! ", Toast.LENGTH_SHORT).show();
//            progressDialog.dismiss();
//            textStateImport.setText(cnt[0] + " assortments imported!");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            progressDialog.dismiss();
//            Toast.makeText(context, "The specified file was not found", Toast.LENGTH_SHORT).show();
//        }
//    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return "";
    }

    private void AskForPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int readpermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writepermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int READ_PHONEpermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (READ_PHONEpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }
}