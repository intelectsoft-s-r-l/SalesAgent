package md.intelectsoft.salesagent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import md.intelectsoft.salesagent.Adapters.AdapterLinesRequestHistory;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.GetPrintRequest;
import md.intelectsoft.salesagent.RealmUtils.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

@SuppressLint("NonConstantResourceId")
public class OrderHistoryClientActivity extends AppCompatActivity {
    @BindView(R.id.orderListClientOrderDetailHistory) TextView textActivityName;
    @BindView(R.id.textOrderHistoryState) TextView textRequestState;
    @BindView(R.id.textOrderHistoryComment) TextView textRequestComment;
    @BindView(R.id.textOrderHistoryDeliveryAddress) TextView textDeliveryAddress;
    @BindView(R.id.textOrderHistorySum) TextView textRequestSum;
    @BindView(R.id.rrv_recycler_view_order_history_lines) ListView listLines;

    OrderServiceAPI orderServiceAPI;
    ProgressDialog progressDialog;
    Request requestToView;
    String token;

    @OnClick(R.id.textBackToHistoryListOrders) void onBck(){
        finish();
    }

    @OnClick(R.id.orderExitHistoryDetail) void onExit(){
        finish();
    }

    @OnClick(R.id.orderShareRequestHistory) void onShareDocument(){
        if (ActivityCompat.checkSelfPermission(OrderHistoryClientActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(OrderHistoryClientActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            shareDocument();
        }
        else
            ActivityCompat.requestPermissions(OrderHistoryClientActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 2231);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_order_history_client);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
        progressDialog = new ProgressDialog(this);
        SharedPreferences sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);

        requestToView = AgentApplication.getInstance().getRequestToView();

        textActivityName.setText(requestToView.getClientName() + " - " + getString(R.string.order_history_activity_title) + requestToView.getCode());
        textDeliveryAddress.setText(requestToView.getDeliveryAddress());
        textRequestComment.setText(requestToView.getComment());
        String state = "";
        switch (requestToView.getState()){
            case 0 : state = getString(R.string.order_histori_client_state);
            case 1 : state = getString(R.string.order_histori_client_state_queue);
            case 2 : state = getString(R.string.order_histori_client_state_work);
            case 3 : state = getString(R.string.order_histori_client_state_prepared);
        }
        textRequestState.setText(state);
        textRequestSum.setText(requestToView.getSum() + " MDL");

        listLines.setAdapter(new AdapterLinesRequestHistory(this, R.layout.item_list_order_history_detail_lines, requestToView.getLines()));
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 2231){
            if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                shareDocument();
            }
        }
    }

    private void shareDocument (){
        progressDialog.setMessage(getString(R.string.dialog_msg_load_image));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        Call<GetPrintRequest> call = orderServiceAPI.getPrintRequest(token, requestToView.getUid());

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel_dialog), (dialog, which) -> {
            call.cancel();
            if(!call.isCanceled())
                progressDialog.dismiss();
        });
        progressDialog.show();

        call.enqueue(new Callback<GetPrintRequest>() {
            @Override
            public void onResponse(Call<GetPrintRequest> call, Response<GetPrintRequest> response) {
                GetPrintRequest printRequest = response.body();
                progressDialog.dismiss();
                if(printRequest.getErrorCode() == 0){
                    String imageFile = printRequest.getImageFile();

                    byte[] decodedString = Base64.decode(imageFile, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    // save bitmap to cache directory
                    try {

                        File cachePath = new File(Environment.getExternalStorageDirectory() + "/SalesAgent");
                        if(!cachePath.exists()){
                            cachePath.mkdir(); // don't forget to make the directory
                        }
                        File newFile = new File(cachePath, "/" + requestToView.getClientName().trim() + "-" + requestToView.getCode() + ".png");
                        FileOutputStream stream = new FileOutputStream(newFile); // overwrites this image every time
                        decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    File imagePath = new File(Environment.getExternalStorageDirectory() + "/SalesAgent");
                    File newFile = new File(imagePath, "/" + requestToView.getClientName().trim() + "-" + requestToView.getCode() + ".png");
                    Uri contentUri = FileProvider.getUriForFile(OrderHistoryClientActivity.this, "md.intelectsoft.salesagent.fileprovider", newFile);

                    if (contentUri != null) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                        shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

                        startActivity(Intent.createChooser(shareIntent, "Share document!"));
                    }
                }
            }

            @Override
            public void onFailure(Call<GetPrintRequest> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(OrderHistoryClientActivity.this, "Error download image!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}