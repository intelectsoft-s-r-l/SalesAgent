package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmList;
import md.intelectsoft.salesagent.Adapters.AdapterClientSortedOrderList;
import md.intelectsoft.salesagent.AppUtils.BaseEnum;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.RequestList;
import md.intelectsoft.salesagent.RealmUtils.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;
import static md.intelectsoft.salesagent.ClientDetailActivity.getRequestsClient;

@SuppressLint("NonConstantResourceId")
public class OrdersClientListActivity extends AppCompatActivity {

    @BindView(R.id.list_request_client) ListView listClientOrders;
    @BindView(R.id.orderListClientOrdersList) TextView textActivityName;
    @BindView(R.id.textPeriodHistoryClientName) TextView textClientName;
    @BindView(R.id.textPeriodHistoryClientFrom) TextView periodFrom;
    @BindView(R.id.textPeriodHistoryClientTo) TextView periodTo;
    @BindView(R.id.orderClientHistorySearch) Button searchOrders;
    @BindView(R.id.textView51) TextView searchTitle;
    @BindView(R.id.textView53) TextView periodTitle;
    @BindView(R.id.textView54) TextView periodInters;

    AdapterClientSortedOrderList adapterClientSortedOrderList;
    SharedPreferences sharedPreferencesSettings;
    OrderServiceAPI orderServiceAPI;
    ProgressDialog progressDialog;
    RealmList<Request> requests;
    Context context;
    String token, clientUid, clientName;

    SimpleDateFormat dfDate_day= new SimpleDateFormat("yyyy-MM-dd");

    @OnClick(R.id.textBackToClient) void onBckClick(){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_orders_client_list);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        context = this;
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        progressDialog = new ProgressDialog(context);

        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);

        Intent fromClient = getIntent();
        clientUid = fromClient.getStringExtra("uid");
        clientName = fromClient.getStringExtra("name");
        int type = fromClient.getIntExtra("TypeOrders",100);
        boolean isHistory = fromClient.getBooleanExtra("history", false);

        setNameTextColor();

        if(!isHistory){
            searchOrders.setVisibility(View.GONE);
            textClientName.setVisibility(View.GONE);
            periodFrom.setVisibility(View.GONE);
            periodTo.setVisibility(View.GONE);
            searchTitle.setVisibility(View.GONE);
            periodTitle.setVisibility(View.GONE);
            periodInters.setVisibility(View.GONE);

            List<Request> sortedList = new ArrayList<>();
            requests = getRequestsClient();

            if(type == 100){ // all
                sortedList.addAll(requests);
            }
            else if(type == 99) { // in work
                for(Request request : requests){
                    if (request.getState() == BaseEnum.InWork)
                        sortedList.add(request);
                }
            }
            else if(type == 98){ //in queue
                for(Request request : requests){
                    if (request.getState() == BaseEnum.InQueue)
                        sortedList.add(request);
                }
            }
            else if(type == 97){ // drafts
                for(Request request : requests){
                    if (request.getState() == BaseEnum.Draft)
                        sortedList.add(request);
                }
            }
            adapterClientSortedOrderList = new AdapterClientSortedOrderList(sortedList);
            listClientOrders.setAdapter(adapterClientSortedOrderList);
        }
        else{
            getOrdersSearch("2020-01-01", "2021-12-31");

            Calendar calendarFrom = Calendar.getInstance();
            calendarFrom.set(2020, 0,1);

            Calendar calendarTo = Calendar.getInstance();
            calendarTo.set(2021, 11,31);

            periodFrom.setText("2020-01-01");
            periodTo.setText("2021-12-31");


            DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
                // TODO Auto-generated method stub
                calendarFrom.set(Calendar.YEAR, year);
                calendarFrom.set(Calendar.MONTH, monthOfYear);
                calendarFrom.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String dt1 = dfDate_day.format(calendarFrom.getTime());

                periodFrom.setText(dt1);
            };

            DatePickerDialog.OnDateSetListener dateTo  = (view, year, monthOfYear, dayOfMonth) -> {
                // TODO Auto-generated method stub
                calendarTo.set(Calendar.YEAR, year);
                calendarTo.set(Calendar.MONTH, monthOfYear);
                calendarTo.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String dt1 = dfDate_day.format(calendarTo.getTime());

                periodTo.setText(dt1);
            };

            periodFrom.setOnClickListener(v -> {
                new DatePickerDialog(context, date, calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH), calendarFrom.get(Calendar.DAY_OF_MONTH)).show();
            });
            periodTo.setOnClickListener(v -> {
                new DatePickerDialog(context, dateTo, calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH), calendarTo.get(Calendar.DAY_OF_MONTH)).show();
            });
            textClientName.setText(clientName);

            searchOrders.setOnClickListener(v -> {
                getOrdersSearch(periodFrom.getText().toString(), periodTo.getText().toString());
            });
        }

        listClientOrders.setOnItemClickListener((parent, view, position, id) -> {
            AgentApplication.getInstance().setRequestToView(adapterClientSortedOrderList.getItem(position));

            startActivity(new Intent(context, OrderHistoryClientActivity.class));
        });
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

    private void getOrdersSearch(String from, String to) {
        Call<RequestList> requestListCall = orderServiceAPI.getRequestList(token,from, to, clientUid);
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
                        RealmList<Request> requestsClient = requestList.getDocuments();

                        if(requestsClient != null && requestsClient.size() > 0) {
                            for (Request request : requestsClient) {
                                String internUUID = UUID.randomUUID().toString();
                                request.setInternId(internUUID);
                                String dateDocument = request.getDateValid();
                                if (dateDocument != null) {
                                    if (dateDocument != null)
                                        dateDocument = dateDocument.replace("/Date(", "");
                                    if (dateDocument != null)
                                        dateDocument = dateDocument.substring(0, dateDocument.length() - 7);
                                }
                                long date = Long.parseLong(dateDocument);
                                request.setDateToLong(date);
                            }

                            progressDialog.dismiss();

                            adapterClientSortedOrderList = new AdapterClientSortedOrderList(requestsClient);
                            listClientOrders.setAdapter(adapterClientSortedOrderList);
                            progressDialog.dismiss();
                        }
                        else{
                            progressDialog.dismiss();
//                                historyRecyclerView.setVisibility(View.INVISIBLE);
//                                imageEmptyOrders.setVisibility(View.VISIBLE);
//                                textEmptyOrders.setVisibility(View.VISIBLE);
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
    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + clientName);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        textActivityName.setText(s);
    }

}