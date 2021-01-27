package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.salesagent.Adapters.AdapterClientHistoryOrder;
import md.intelectsoft.salesagent.Adapters.AdapterOutletListGrid;
import md.intelectsoft.salesagent.AppUtils.BaseEnum;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.AppUtils.VerticalSpaceItemDecoration;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientResponseInfo;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.RequestList;
import md.intelectsoft.salesagent.RealmUtils.Client;
import md.intelectsoft.salesagent.RealmUtils.Outlets;
import md.intelectsoft.salesagent.RealmUtils.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

@SuppressLint("NonConstantResourceId")
public class ClientDetailActivity extends AppCompatActivity {
    @BindView(R.id.clientDetailAppName) TextView headerActivityName;
    @BindView(R.id.textBalanceClient) TextView textClientBalance;
    @BindView(R.id.textClientFullName) TextView textClientName;

    @BindView(R.id.countAllOrdersClients) TextView countAllOrders;
    @BindView(R.id.countConfirmedOrdersClient) TextView countInWorkOrders;
    @BindView(R.id.countInQueueOrdersClient) TextView countInQueueOrders;
    @BindView(R.id.countDraftOrdersClient) TextView countDraftOrders;

    @BindView(R.id.textShowAllInClient) TextView textShowAll;
    @BindView(R.id.textShowAllInClientConfirmedClient) TextView textShowAllInWork;
    @BindView(R.id.textShowAllInClientInQueueClient) TextView textShowAllInQueue;
    @BindView(R.id.textShowAllInClientDraftClient) TextView textShowAllDraft;

    @BindView(R.id.textInfoClientCode) TextView codeClient;
    @BindView(R.id.textInfoClientIdnp) TextView idnpClient;
    @BindView(R.id.textInfoClientTVA) TextView tvaClient;

    @BindView(R.id.imageViewOutletsEmpty) ImageView imageEmptyOutlets;
    @BindView(R.id.textOutletsClientEmpty) TextView textEmptyOutlets;
    @BindView(R.id.imageEmptyOrders) ImageView imageEmptyOrders;
    @BindView(R.id.textEmptyOrders) TextView textEmptyOrders;

    @BindView(R.id.historyRecyclerView) RecyclerView historyRecyclerView;
    @BindView(R.id.listClientOutlets) ListView outletListView;

    @BindView(R.id.textViewClientShortNameDetail) TextView textClientDetailShortName;
    @BindView(R.id.imageClientDetail) ImageView clientDetailImage;

    SharedPreferences sharedPreferencesSettings;
    OrderServiceAPI orderServiceAPI;
    ProgressDialog progressDialog;
    DisplayMetrics displayMetrics;
    Context context;
    Realm mRealm;
    String outletAddress;
    String token, clientUid, clientName;
    Client client;

    static RealmList<Request> requestsClient;
    int countDrafts = 0, countInQueue = 0, countInWork = 0, allOrders = 0;

    @OnClick(R.id.textBackToListFromClientDetail) void back(){
        finish();
    }

    @OnClick(R.id.buttonNewOrderClient) void newOrderFromClient(){
        if(client != null ){
            Intent clientDetail = new Intent(context, AssortmentOrderActivity.class);
            clientDetail.putExtra("uid", client.getUid());
            clientDetail.putExtra("name", client.getName());
            clientDetail.putExtra("priceUid", client.getPriceListUid());
            clientDetail.putExtra("newRequest", true);

            if(client.getOutlets().size() > 0 && outletAddress == null){
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_outlets_list, null);

                Dialog outletsDialog = new Dialog(context,R.style.CustomDialog);
                outletsDialog.setContentView(dialogView);

                TextView textCancel = dialogView.findViewById(R.id.textDialogOutletCancel);
                TextView next = dialogView.findViewById(R.id.textDialogOutletNext);
                ListView listOutlets = dialogView.findViewById(R.id.listDialogOutlets);
                TextView titleDialog = dialogView.findViewById(R.id.textDialogOutletClientName);

                titleDialog.setText(getString(R.string.select_outlet_for_client) + client.getName());
                textCancel.setOnClickListener(v -> outletsDialog.dismiss());

                AdapterOutletListGrid adapterOutletsDialog = new AdapterOutletListGrid(context, R.layout.item_list_outlets, client.getOutlets());
                listOutlets.setAdapter(adapterOutletsDialog);

                listOutlets.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    outletAddress = adapterOutletsDialog.getItem(position1).getAddress();
                    listOutlets.setItemChecked(position1, true);
                });

                next.setOnClickListener(v -> {
                    if(outletAddress != null && !outletAddress.equals("")) {
                        clientDetail.putExtra("address", outletAddress);
                        outletsDialog.dismiss();
                        startActivity(clientDetail);
                    }
                    else
                        Toast.makeText(context, getString(R.string.select_outlets_for_client_to_continue), Toast.LENGTH_SHORT).show();
                });

                outletsDialog.show();

                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(outletsDialog.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.45f);
                int dialogWindowHeight = (int) (displayHeight * 0.8f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = dialogWindowHeight;  //LinearLayout.LayoutParams.WRAP_CONTENT
                outletsDialog.getWindow().setAttributes(layoutParams);

            }
            else {
                if(outletAddress != null && !outletAddress.equals(""))
                    clientDetail.putExtra("address", outletAddress);
                startActivity(clientDetail);
            }
        }
    }

    @OnClick(R.id.searchMoreOrdersClientDetail) void onSearchMoreOrders(){
        Intent requestActivity = new Intent(context, OrdersClientListActivity.class);
        requestActivity.putExtra("history", true);
        requestActivity.putExtra("name", clientName);
        requestActivity.putExtra("uid", clientUid);

        startActivityForResult(requestActivity,1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_client_detail);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        historyRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(2));

        context = this;
        mRealm = Realm.getDefaultInstance();
        progressDialog = new ProgressDialog(context);

        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        clientUid = getIntent().getStringExtra("Uid");

        setNameTextColor();
        getInformationAboutClient();


        Client client = mRealm.where(Client.class).equalTo("uid",clientUid).findFirst();

        if(client != null) {
            clientName = client.getName();
            textClientName.setText(client.getName());

            if(client.getImage() != null) {
                if(client.getImage().length > 0){
                    textClientDetailShortName.setVisibility(View.GONE);
                    clientDetailImage.setVisibility(View.VISIBLE);
                    Bitmap bmpImage = BitmapFactory.decodeByteArray(client.getImage(), 0, client.getImage().length);
                    clientDetailImage.setImageBitmap(Bitmap.createScaledBitmap(bmpImage, 211, 211, false));
                }
                else{
                    textClientDetailShortName.setVisibility(View.VISIBLE);
                    clientDetailImage.setVisibility(View.INVISIBLE);
                    textClientDetailShortName.setText(client.getName().substring(0,2));
                }
            }
            else{
                textClientDetailShortName.setVisibility(View.VISIBLE);
                clientDetailImage.setVisibility(View.INVISIBLE);
                textClientDetailShortName.setText(client.getName().substring(0,2));
            }
        }


        textShowAll.setOnClickListener(textShowOrders);
        textShowAllInWork.setOnClickListener(textShowOrders);
        textShowAllInQueue.setOnClickListener(textShowOrders);
        textShowAllDraft.setOnClickListener(textShowOrders);
    }

    View.OnClickListener textShowOrders = v -> {
        int id = v.getId();

        int intentAction = 0;
        switch (id){
            case R.id.textShowAllInClient: intentAction = 100; break;
            case R.id.textShowAllInClientConfirmedClient: intentAction = 99; break;
            case R.id.textShowAllInClientInQueueClient: intentAction = 98; break;
            case R.id.textShowAllInClientDraftClient: intentAction = 97; break;
        }

        Intent requestActivity = new Intent(context, OrdersClientListActivity.class);
        requestActivity.putExtra("TypeOrders", intentAction);
        requestActivity.putExtra("name", clientName);

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

    private void getInformationAboutClient() {
        Call<ClientResponseInfo> clientListCall = orderServiceAPI.getClientInfo(token, clientUid);

        progressDialog.setMessage(getString(R.string.dialog_msg_get_client_info));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(-1, getString(R.string.cancel_dialog), (dialog, which) -> {
            clientListCall.cancel();
            if(clientListCall.isCanceled()){
                progressDialog.dismiss();
                finish();
            }
        });
        progressDialog.show();

        clientListCall.enqueue(new Callback<ClientResponseInfo>() {
            @Override
            public void onResponse(Call<ClientResponseInfo> call, Response<ClientResponseInfo> response) {
                ClientResponseInfo clientList = response.body();

                if(clientList != null){
                    if(clientList.getErrorCode() == 0){
                        Client client1 = clientList.getClients();

                        if(client1 != null ){
                            client = client1;

                            textClientBalance.setText(getString(R.string.balance_client_text) + client.getBalance() + " MDL");
                            RealmList<Outlets> outlets = client.getOutlets();
                            if(outlets.size() > 0){
                                AdapterOutletListGrid adapterOutletsDialog = new AdapterOutletListGrid(context, R.layout.item_list_outlets, outlets);
                                outletListView.setAdapter(adapterOutletsDialog);

                                outletListView.setOnItemClickListener((parent1, view1, position1, id1) -> {
                                    outletAddress = adapterOutletsDialog.getItem(position1).getAddress();
                                    outletListView.setItemChecked(position1, true);
                                });

                            }
                            else{
                                outletListView.setVisibility(View.GONE);
                                imageEmptyOutlets.setVisibility(View.VISIBLE);
                                textEmptyOutlets.setVisibility(View.VISIBLE);
                            }
                            if(client.getImage() != null) {
                                if(client.getImage().length > 0){
                                    textClientDetailShortName.setVisibility(View.GONE);
                                    clientDetailImage.setVisibility(View.VISIBLE);
                                    Bitmap bmpImage = BitmapFactory.decodeByteArray(client.getImage(), 0, client.getImage().length);
                                    clientDetailImage.setImageBitmap(Bitmap.createScaledBitmap(bmpImage, 211, 211, false));

                                    mRealm.executeTransaction(realm -> {
                                        client.setImage(client1.getImage());
                                    });
                                }
                                else{
                                    textClientDetailShortName.setVisibility(View.VISIBLE);
                                    clientDetailImage.setVisibility(View.INVISIBLE);
                                    textClientDetailShortName.setText(client.getName().substring(0,2));
                                }
                            }
                            else{
                                textClientDetailShortName.setVisibility(View.VISIBLE);
                                clientDetailImage.setVisibility(View.INVISIBLE);
                                textClientDetailShortName.setText(client.getName().substring(0,2));
                            }

                            codeClient.setText(client.getCode());
                            idnpClient.setText(client.getIDNP());
                            tvaClient.setText(client.getTVACode());


                            getInformationOrdersOfClient(outlets);
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(context, "List on the clients is empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error load client! Code & Message: " + clientList.getErrorCode() + " & " + clientList.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(context, "Null response client!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ClientResponseInfo> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure load client: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TAG", "onFailure: " + call.request() );
            }
        });

    }

    private void getInformationOrdersOfClient(RealmList<Outlets> outlets) {
        Call<RequestList> requestListCall = orderServiceAPI.getRequestList(token,"2000-01-01", "2200-01-01", clientUid);
        progressDialog.dismiss();
        progressDialog.setMessage(getString(R.string.dialog_msg_get_orders));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(-1, getString(R.string.cancel_dialog), (dialog, which) -> {
            requestListCall.cancel();
            if(requestListCall.isCanceled()){
                progressDialog.dismiss();
                finish();
            }
        });
        progressDialog.show();

        requestListCall.enqueue(new Callback<RequestList>() {
            @Override
            public void onResponse(Call<RequestList> call, Response<RequestList> response) {
                RequestList requestList = response.body();
                if (requestList != null){
                    if(requestList.getErrorCode() == 0){
                        requestsClient = requestList.getDocuments();

                        if(requestsClient != null && requestsClient.size() > 0) {
                            for (Request request : requestsClient) {
                                String internUUID = UUID.randomUUID().toString();
                                request.setInternId(internUUID);
                                String dateDocument = request.getDateValid();
                                if (dateDocument != null) {
                                    dateDocument = dateDocument.replace("/Date(", "");
                                    dateDocument = dateDocument.replace("+0300)/", "");
                                    dateDocument = dateDocument.replace("+0200)/", "");
                                }
                                long date = Long.parseLong(dateDocument);
                                request.setDateToLong(date);

                                if (request.getState() == BaseEnum.Draft)
                                    countDrafts++;
                                else if (request.getState() == BaseEnum.InQueue)
                                    countInQueue++;
                                else if (request.getState() == BaseEnum.InWork)
                                    countInWork++;

                                allOrders++;
                            }

                            progressDialog.dismiss();

                            countAllOrders.setText(String.valueOf(allOrders));
                            countInWorkOrders.setText(String.valueOf(countInWork));
                            countInQueueOrders.setText(String.valueOf(countInQueue));
                            countDraftOrders.setText(String.valueOf(countDrafts));

                            AdapterClientHistoryOrder adapterClientHistoryOrder = new AdapterClientHistoryOrder(requestsClient);
                            historyRecyclerView.setAdapter(adapterClientHistoryOrder);

                            progressDialog.dismiss();
                        }
                        else{
                            progressDialog.dismiss();
                            historyRecyclerView.setVisibility(View.INVISIBLE);
                            imageEmptyOrders.setVisibility(View.VISIBLE);
                            textEmptyOrders.setVisibility(View.VISIBLE);
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
            }

            @Override
            public void onFailure(Call<RequestList> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure load orders: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static RealmList<Request> getRequestsClient(){
        return  requestsClient;
    }

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + getString(R.string.client_detail_title_activity));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
    }
}