package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.salesagent.Adapters.AdapterLinesRequestShopCart;
import md.intelectsoft.salesagent.AppUtils.BaseEnum;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.RemoteException;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientPrices;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientResponseInfo;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.DiscountPricesClient;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.SaveRequestResult;
import md.intelectsoft.salesagent.OrderServiceUtils.body.ClientPricesBody;
import md.intelectsoft.salesagent.OrderServiceUtils.body.saveRequest.RequestLineBody;
import md.intelectsoft.salesagent.OrderServiceUtils.body.saveRequest.SaveRequestBody;
import md.intelectsoft.salesagent.RealmUtils.Client;
import md.intelectsoft.salesagent.RealmUtils.Request;
import md.intelectsoft.salesagent.RealmUtils.RequestLine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;
@SuppressLint("NonConstantResourceId")
public class ShopCartActivity extends AppCompatActivity {
    @BindView(R.id.textOrderShopCartAddress) TextView addressShopCart;
    @BindView(R.id.textShopCartName) TextView shopCartName;
    static TextView shopCartSum;
    @BindView(R.id.textShopCartComment) TextView shopCartComment;
    @BindView(R.id.rrv_recycler_view_shop_cart_lines) ListView linesList;
    @BindView(R.id.textClientBalanceShopCart) TextView shopCartBalanceClient;

    AdapterLinesRequestShopCart linesRequestDetail;
    SharedPreferences sharedPreferencesSettings;
    OrderServiceAPI orderServiceAPI;
    ProgressDialog progressDialog;
    Context context;
    String token, orderComment, clientUid;
    static Realm mRealm;

    static Request request;

    @OnClick(R.id.imageCheckDiscountFromClient) void onCheckDiscount(){
        ClientPricesBody reviewPrices = new ClientPricesBody();
        List<String> priceList = new ArrayList<>();
        for (int i = 0; i < linesRequestDetail.getCount(); i++) {
            String priceLine = linesRequestDetail.getItem(i).getPriceLineUid();
            priceList.add(priceLine);
        }
        reviewPrices.setPricelines(priceList);
        reviewPrices.setClientUid(clientUid);
        reviewPrices.setTokenUid(token);

        Call<ClientPrices> clientPricesCall = orderServiceAPI.getClientPriceDiscount(reviewPrices);

        progressDialog.setMessage("Check discount...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> {
            if(clientPricesCall.isCanceled())
                dialog.dismiss();
            clientPricesCall.cancel();
        });
        progressDialog.show();

        clientPricesCall.enqueue(new Callback<ClientPrices>() {
            @Override
            public void onResponse(Call<ClientPrices> call, Response<ClientPrices> response) {
                ClientPrices clientPrices = response.body();

                if(clientPrices != null){
                    if(clientPrices.getErrorCode() == 0 ){
                        if(clientPrices.getPrices().size() > 0){
                            for (int i = 0; i < linesRequestDetail.getCount(); i++) {
                                RequestLine requestLine = linesRequestDetail.getItem(i);
                                for(DiscountPricesClient discount : clientPrices.getPrices()){
                                    if(discount.getPriceLineUid().equals(requestLine.getPriceLineUid())){
                                        double lineCount = requestLine.getCount();
                                        double linePriceDialler = requestLine.getPriceDialler();

                                        if(linePriceDialler == 0){
                                            mRealm.beginTransaction();
                                            request.setSum(request.getSum() - requestLine.getSum());
                                            requestLine.setSum(lineCount * discount.getPrice());
                                            requestLine.setPriceDiscount(discount.getPrice());
                                            request.setSum(request.getSum() + requestLine.getSum());
                                            mRealm.commitTransaction();
                                        }

                                        double sum = request.getSum() != null ? request.getSum() : 0 ;
                                        shopCartSum.setText(String.format("%.2f", sum).replace(",",".") + " MDL");
                                    }
                                }
                            }
                        }
                        else
                            Toast.makeText(context, "No discount!", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(context, "Error check discount: " + RemoteException.getServiceException(clientPrices.getErrorCode()), Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(context, "Error check discount: Response null!", Toast.LENGTH_SHORT).show();

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ClientPrices> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error check discount: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @OnClick(R.id.addCommentToOrderShopCart) void addCommentShopCart() {
        final EditText input = new EditText(context);
        input.setHint(getString(R.string.add_comment_hint_comment_dialog));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        if(orderComment != null && !orderComment.equals(""))
            input.setText(orderComment);


        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.comments_to_order_title_dialog))
                .setView(input)
                .setPositiveButton(getString(R.string.add_product_to_card), (dialogInterface, i) -> {
                    String text = input.getText().toString();
                    if(!text.equals("")){
                        mRealm.executeTransaction(realm -> {
                            request.setComment(text);
                        });

                        shopCartComment.setText(text);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        }
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel_dialog), (dialog, which) -> {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    }
                    dialog.dismiss();
                })
                .show();

//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_comments_to_order, null);
//
//        Dialog orderCommentDialog = new Dialog(context,R.style.CustomDialog);
//        orderCommentDialog.setContentView(dialogView);
//        orderCommentDialog.setCancelable(false);
//
//        EditText commentText = dialogView.findViewById(R.id.editTextCommentIput);
//        Button addComment = dialogView.findViewById(R.id.addCommentTo);
//        Button cancel = dialogView.findViewById(R.id.closeDialogComment);
//        if(orderComment != null && !orderComment.equals(""))
//            commentText.setText(orderComment);
//
//        addComment.setOnClickListener(v -> {
//            String text = commentText.getText().toString();
//            if(!text.equals("")){
//                mRealm.executeTransaction(realm -> {
//                    request.setComment(text);
//                });
//
//                shopCartComment.setText(text);
//
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    imm.hideSoftInputFromWindow(commentText.getWindowToken(), 0);
//                }
//                orderCommentDialog.dismiss();
//            }
//        });
//
//        cancel.setOnClickListener(v -> {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (imm != null) {
//                imm.hideSoftInputFromWindow(commentText.getWindowToken(), 0);
//            }
//            orderCommentDialog.dismiss();
//        });
//
//        orderCommentDialog.show();
//
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
//
//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//        layoutParams.copyFrom(orderCommentDialog.getWindow().getAttributes());
//        layoutParams.width = 550;
//        layoutParams.height = 250;  //LinearLayout.LayoutParams.WRAP_CONTENT
//        orderCommentDialog.getWindow().setAttributes(layoutParams);
    }

    @OnClick(R.id.orderShopCartSaveChanges) void saveLocalOrderDraft() {
        setResult(777);
        finish();
    }

    @OnClick(R.id.orderShopCartSaveAndSend) void sendRemoteOrder() {
        SaveRequestBody order = new SaveRequestBody();
        List<RequestLineBody> orderLines = new ArrayList<>();

        if(request == null)
            finish();

        order.setClientUid(request.getClientUid());
        order.setClientName(request.getClientName());
        order.setComment(request.getComment());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setState(BaseEnum.InQueue);
        order.setSum(request.getSum());

        if(request.getUid() != null)
            order.setUid(request.getUid());

        RealmList<RequestLine> reqLines = request.getLines();
        if(reqLines != null && reqLines.size() > 0){
            for(RequestLine requestLine : reqLines){
                RequestLineBody lines = new RequestLineBody();
                lines.setAssortimentUid(requestLine.getAssortimentUid());
                lines.setCount(requestLine.getCount());

                orderLines.add(lines);
            }
        }

        order.setLines(orderLines);

        saveOrder(order);
    }

    private void saveOrder(SaveRequestBody order){
        Call<SaveRequestResult> saveOrder = orderServiceAPI.saveRequest(order, token);

        progressDialog.setMessage("Save order...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",(dialog, which) -> {
            saveOrder.cancel();
            if(!saveOrder.isCanceled())
                progressDialog.dismiss();
        });

        saveOrder.enqueue(new Callback<SaveRequestResult>() {
            @Override
            public void onResponse(Call<SaveRequestResult> call, Response<SaveRequestResult> response) {
                SaveRequestResult saveRequestResult = response.body();
                Log.d("TAG", "sendRemoteOrder: " + saveRequestResult.getState());
                if(saveRequestResult != null){
                    if(saveRequestResult.getErrorCode() == 0){
                        mRealm.beginTransaction();
                        request.setCode(saveRequestResult.getCode());
                        request.setUid(saveRequestResult.getUid());
                        request.setState(saveRequestResult.getState());
                        request.setSyncState(BaseEnum.Syncronizat);
                        mRealm.commitTransaction();

                        progressDialog.dismiss();
                        setResult(777);
                        finish();
                    }
                    else{
                        progressDialog.dismiss();
                    }
                }
                else{
                    progressDialog.dismiss();

                }
            }

            @Override
            public void onFailure(Call<SaveRequestResult> call, Throwable t) {
                progressDialog.dismiss();
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Error save order!")
                        .setMessage(t.getMessage())
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setNeutralButton("RETRY", (dialog, which) -> {
                            dialog.dismiss();
                            saveOrder(order);
                        })
                        .show();

            }
        });
    }

    @OnClick(R.id.textBackContinueShopping) void continueShopping() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_shop_cart);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        context = this;
        mRealm = Realm.getDefaultInstance();
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);
        progressDialog = new ProgressDialog(context);
        shopCartSum = findViewById(R.id.orderShopCartTotalCost);

        Intent fromAssortment = getIntent();
        clientUid = fromAssortment.getStringExtra("clientUid");
        String requestId = fromAssortment.getStringExtra("id");
        loadInfoRequest(requestId);
        loadInfoBalanceClient(clientUid);

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

    private void loadInfoBalanceClient(String clientId) {
        Call<ClientResponseInfo> clientListCall = orderServiceAPI.getClientInfo(token, clientId);

        clientListCall.enqueue(new Callback<ClientResponseInfo>() {
            @Override
            public void onResponse(Call<ClientResponseInfo> call, Response<ClientResponseInfo> response) {
                ClientResponseInfo clientList = response.body();

                if(clientList != null){
                    if(clientList.getErrorCode() == 0){
                        Client client = clientList.getClients();

                        if(client != null){
                            Client client1 = client;

                            shopCartBalanceClient.setText(getString(R.string.balance_client_in_shop_cart) + client1.getBalance() + " MDL");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ClientResponseInfo> call, Throwable t) {

            }
        });
    }

    private void loadInfoRequest(String id) {
        progressDialog.setMessage("Obtain information about order...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        request = mRealm.where(Request.class).equalTo("internId",id).findFirst();
        if(request == null)
            finish();
        else{
            shopCartName.setText(request.getClientName());
            addressShopCart.setText(request.getDeliveryAddress());
            double sum = request.getSum() != null ? request.getSum() : 0 ;
            shopCartSum.setText(String.format("%.2f", sum).replace(",",".") + " MDL");
            orderComment = request.getComment();
            shopCartComment.setText(orderComment);

            RealmList<RequestLine> lines = request.getLines();
            if(lines != null && lines.size() > 0){
                linesRequestDetail = new AdapterLinesRequestShopCart(lines, this);
                linesList.setAdapter(linesRequestDetail);
            }
        }

        progressDialog.dismiss();
    }

    public static  void changeCountIntoLine (String assortmentUid, double count){
        RealmList<RequestLine> lines = request.getLines();
        for(RequestLine line : lines){
            if(line.getAssortimentUid().equals(assortmentUid)){
                double linePrice = line.getPrice();
                double linePriceDialler = line.getPriceDialler();
                double linePriceDiscount = line.getPriceDiscount();

                if(linePriceDialler > 0)
                    linePrice = linePriceDialler;
                if(linePriceDiscount > 0)
                    linePrice = linePriceDiscount;

                double lineCount = line.getCount();

                if(lineCount + count > 0) {
                    lineCount = lineCount + count;

                    mRealm.beginTransaction();

                    request.setSum(request.getSum() - line.getSum());
                    line.setSum(lineCount * linePrice);
                    line.setCount(lineCount);
                    request.setSum(request.getSum() + line.getSum());
                    mRealm.commitTransaction();
                }
                break;
            }
        }
        double sum = request.getSum() != null ? request.getSum() : 0 ;
        shopCartSum.setText(String.format("%.2f", sum).replace(",",".") + " MDL");
    }

    public static void removeLine(String assortmentUid){
        RealmList<RequestLine> lines = request.getLines();
        for(RequestLine line : lines){
            if(line.getAssortimentUid().equals(assortmentUid)){
                double lineSum = line.getSum();

                mRealm.beginTransaction();
                line.deleteFromRealm();
                request.setSum(request.getSum() - lineSum);
                mRealm.commitTransaction();

                break;
            }
        }

        double sum = request.getSum() != null ? request.getSum() : 0 ;
        shopCartSum.setText(String.format("%.2f", sum).replace(",",".") + " MDL");
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