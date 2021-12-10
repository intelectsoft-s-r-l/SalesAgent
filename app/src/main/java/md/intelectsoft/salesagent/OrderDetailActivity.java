package md.intelectsoft.salesagent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.salesagent.Adapters.AdapterLinesRequestDetail;
import md.intelectsoft.salesagent.AppUtils.BaseEnum;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.RemoteException;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientPrices;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.DiscountPricesClient;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.GetPrintRequest;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.RequestList;
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
public class OrderDetailActivity extends AppCompatActivity {
    @BindView(R.id.textOrderDetailClientNameCode) TextView orderClientNameAndCode;
    @BindView(R.id.textOrderDetailState) TextView orderTextState;
    static TextView orderTotalCost;
    @BindView(R.id.textOrderDetailStateColor) TextView orderTextStateColor;
    @BindView(R.id.textOrderDetailComment) TextView orderTextComment;
    @BindView(R.id.addProductsToOrder) Button orderAddProducts;
    @BindView(R.id.addCommentToOrder) Button orderAddComment;
    @BindView(R.id.orderShareDocument) Button orderShare;
    @BindView(R.id.orderDetailSaveAndSend) Button orderSaveAndSendOrExit;
    static Button orderSaveChanges;
    @BindView(R.id.textOrderDetailAddress) TextView orderAddress;
    @BindView(R.id.rrv_recycler_view_order_lines) ListView linesList;
    @BindView(R.id.imageCheckDiscountFromClientInEditingOrder) TextView checkDiscount;

    static Realm mRealm;
    static Context context;
    String token, orderComment, id, uid, orderAdditionalComment;
    static Request requestDetail;
    AdapterLinesRequestDetail linesRequestDetail;
    SharedPreferences sharedPreferencesSettings;
    OrderServiceAPI orderServiceAPI;
    ProgressDialog progressDialog;
    static int changes;

    @OnClick(R.id.imageCheckDiscountFromClientInEditingOrder) void checkDiscount(){
        ClientPricesBody reviewPrices = new ClientPricesBody();
        List<String> priceList = new ArrayList<>();
        for (int i = 0; i < linesRequestDetail.getCount(); i++) {
            String priceLine = (String) linesRequestDetail.getItem(i).getPriceLineUid();
            priceList.add(priceLine);
        }
        reviewPrices.setPricelines(priceList);
        reviewPrices.setClientUid(requestDetail.getClientUid());
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
                                            requestDetail.setSum(requestDetail.getSum() - requestLine.getSum());
                                            requestLine.setSum(lineCount * discount.getPrice());
                                            requestLine.setPriceDiscount(discount.getPrice());
                                            requestDetail.setSum(requestDetail.getSum() + requestLine.getSum());
                                            mRealm.commitTransaction();
                                        }

                                        double sum = requestDetail.getSum() != null ? requestDetail.getSum() : 0 ;
                                        orderTotalCost.setText(String.format("%.2f", sum).replace(",",".") + " MDL");
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

    @OnClick(R.id.textBackToListOrders) void back(){
        if(changes > 0){
            Toast.makeText(context, getString(R.string.first_save_changes), Toast.LENGTH_SHORT).show();
        }
        else{
            finish();
        }
    }

    @OnClick(R.id.addCommentToOrder) void addCommentToOrder(){
        String lang = LocaleHelper.getLanguage(this);
        setAppLocale(lang);
//        final EditText input = new EditText(context);
//        input.setHint(getString(R.string.add_comment_hint_comment_dialog));
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//
//        new MaterialAlertDialogBuilder(this)
//                .setTitle(getString(R.string.comments_to_order_title_dialog))
//                .setView(input)
//                .setPositiveButton(getString(R.string.add_product_to_card), (dialogInterface, i) -> {
//                    if(!input.getText().toString().equals(orderComment)){
//                        changes+=1;
//                        orderSaveChanges.setText(getString(R.string.save_changes_orders) + "(" + changes + ")");
//                    }
//
//                    orderComment = input.getText().toString();
//
//                    mRealm.executeTransaction(realm -> {
//                        requestDetail.setComment(orderComment);
//                    });
//
//                    orderTextComment.setText(orderComment);
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    if (imm != null) {
//                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
//                    }
//                    dialogInterface.dismiss();
//                })
//                .setNegativeButton(getString(R.string.cancel_dialog), (dialog, which) -> {
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    if (imm != null) {
//                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
//                    }
//                    dialog.dismiss();
//                })
//                .show();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_comments_to_order, null);

        Dialog orderCommentDialog = new Dialog(context,R.style.CustomDialog);
        orderCommentDialog.setContentView(dialogView);
        orderCommentDialog.setCancelable(false);

        EditText commentText = dialogView.findViewById(R.id.editTextCommentIput);
        EditText commentName = dialogView.findViewById(R.id.editTextCommentName);
        EditText commentSurname = dialogView.findViewById(R.id.editTextCommentSurname);
        EditText commentPhone = dialogView.findViewById(R.id.editTextCommentPhone);
        EditText commentAddress = dialogView.findViewById(R.id.editTextCommentAddress);
        CheckBox saveInfo = dialogView.findViewById(R.id.keepDataCheckBox);
        Button addComment = dialogView.findViewById(R.id.addCommentTo);
        Button cancel = dialogView.findViewById(R.id.closeDialogComment);

        Client client = mRealm.where(Client.class).equalTo("uid", requestDetail.getClientUid()).findFirst();
        boolean isChecked = false;
        if(client != null)
            isChecked = client.isSavedDataComment();

        saveInfo.setChecked(isChecked);
        if(isChecked){
            if(client.getNamePerson() != null && !client.getNamePerson().equals(""))
                commentName.setText(client.getNamePerson());
            if(client.getSurName() != null && !client.getSurName().equals(""))
                commentSurname.setText(client.getSurName());
            if(client.getPhone() != null && !client.getPhone().equals(""))
                commentPhone.setText(client.getPhone());
            if(client.getAddress() != null && !client.getAddress().equals(""))
                commentAddress.setText(client.getAddress());
            if(orderAdditionalComment != null && !orderAdditionalComment.equals(""))
                commentText.setText(orderAdditionalComment);
        }

        addComment.setOnClickListener(v -> {
            String text = "";
            if(!commentName.getText().toString().equals(""))
                text = text + " " + commentName.getText().toString();
            if(!commentSurname.getText().toString().equals(""))
                text = text + " " + commentSurname.getText().toString();
            if(!commentPhone.getText().toString().equals(""))
                text = text + ", tel. " + commentPhone.getText().toString();
            if(!commentAddress.getText().toString().equals(""))
                text = text + ", Adresa:  " + commentAddress.getText().toString();
            if(!commentText.getText().toString().equals("")){
                orderAdditionalComment = commentText.getText().toString();
                text = text + "/ " + commentText.getText().toString();
            }


            final String toOrderSave = text;
            mRealm.executeTransaction(realm -> {
                Client client2 = realm.where(Client.class).equalTo("uid", requestDetail.getClientUid()).findFirst();
                if(client2 != null){
                    client2.setSavedDataComment(saveInfo.isChecked());
                    if(saveInfo.isChecked()){
                        if(!commentName.getText().toString().equals(""))
                            client2.setNamePerson(commentName.getText().toString());
                        if(!commentSurname.getText().toString().equals(""))
                            client2.setSurName(commentSurname.getText().toString());
                        if(!commentAddress.getText().toString().equals(""))
                            client2.setAddress(commentAddress.getText().toString());
                        if(!commentPhone.getText().toString().equals(""))
                            client2.setPhone(commentPhone.getText().toString());
                        if(!commentText.getText().toString().equals(""))
                            client2.setAdditionalInfo(commentText.getText().toString());
                    }
                }
                requestDetail.setComment(toOrderSave);
            });
            orderTextComment.setText(text);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(commentName.getWindowToken(), 0);
            }
            orderCommentDialog.dismiss();
        });

        cancel.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(commentText.getWindowToken(), 0);
            }
            orderCommentDialog.dismiss();
        });

        orderCommentDialog.show();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(orderCommentDialog.getWindow().getAttributes());
        layoutParams.width = 550;
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;  //LinearLayout.LayoutParams.WRAP_CONTENT
        orderCommentDialog.getWindow().setAttributes(layoutParams);
    }

    @OnClick(R.id.orderShareDocument) void getPrintRequest(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            shareDocument();
        }
        else
            ActivityCompat.requestPermissions(OrderDetailActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 2231);
    }

    @OnClick(R.id.addProductsToOrder) void addProductsToOrder(){
        Intent startActivityAddProduct = new Intent(context, AddProductsFromOrder.class);
        startActivityAddProduct.putExtra("internId", requestDetail.getInternId());
        startActivityForResult(startActivityAddProduct, 6547);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_order_detail);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        context = this;
        mRealm = Realm.getDefaultInstance();
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);
        progressDialog = new ProgressDialog(context);
        orderTotalCost = findViewById(R.id.orderDetailTotalCost);
        orderSaveChanges = findViewById(R.id.orderDetailSaveChanges);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        id = intent.getStringExtra("id");
        if(requestDetail == null)
            Log.e("TAG", "onCreate: request is null" );
        else {
            Log.e("TAG", "onCreate: request is not null" );
        }
        requestDetail = mRealm.where(Request.class).equalTo("internId", id).findFirst();
        if(requestDetail != null){
            changes = 0;

            Log.e("TAG", "Client Name: " + requestDetail.getClientName()
            + "\n intern uid: " + requestDetail.getUid()
            + "\n intern Id: " + requestDetail.getInternId()
                    + "\n intern Id intent: " + id
                    + "\n intern Uid intent: " + uid);

            int state = requestDetail.getState();
            String textState = "";
            boolean isBlocked = false;

            if(state != 0) {
                requestIsBlocked();
                isBlocked = true;
            }
            if(state == 0) {
                orderTextStateColor.setVisibility(View.GONE);
                orderShare.setVisibility(View.GONE);
                checkDiscount.setVisibility(View.VISIBLE);

                textState = getString(R.string.order_state_draft);
                orderSaveAndSendOrExit.setOnClickListener(v -> {
                    SaveRequestBody order = new SaveRequestBody();
                    List<RequestLineBody> orderLines = new ArrayList<>();

                    order.setClientUid(requestDetail.getClientUid());
                    order.setClientName(requestDetail.getClientName());
                    order.setComment(requestDetail.getComment());
                    order.setDeliveryAddress(requestDetail.getDeliveryAddress());
                    order.setState(BaseEnum.InQueue);
                    order.setSum(requestDetail.getSum());

                    if(requestDetail.getUid() != null)
                        order.setUid(requestDetail.getUid());

                    RealmList<RequestLine> reqLines = requestDetail.getLines();
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
                });
                orderSaveChanges.setOnClickListener(v -> {
                    finish();
                });
            }
            else if(state == 1){
                textState = getString(R.string.order_state_in_queue);
                orderTextStateColor.setVisibility(View.VISIBLE);
                checkDiscount.setVisibility(View.GONE);
                orderTextStateColor.setText(getString(R.string.order_is_in_process_can_edit_order_after_you_cancel_this_order));
                orderTextStateColor.setBackgroundColor(getColor(R.color.orange));
                orderTextStateColor.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, ContextCompat.getDrawable(context,R.drawable.ic_cancel_order_black_24dp), null);
                orderTextStateColor.setOnClickListener(v -> {
                    progressDialog.setMessage(getString(R.string.dialog_msg_title_cancel_order));
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    //cancel order
                    Call<RequestList> call = orderServiceAPI.deleteRequest(token, uid);

                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> {
                        call.cancel();
                        if(!call.isCanceled())
                            progressDialog.dismiss();
                    });
                    progressDialog.show();

                    call.enqueue(new Callback<RequestList>() {
                        @Override
                        public void onResponse(Call<RequestList> call, Response<RequestList> response) {
                            RequestList requestList = response.body();
                            if(requestList != null && requestList.getErrorCode() == 0){
                                orderTextState.setText(getString(R.string.order_state_draft_state));

                                mRealm.beginTransaction();
                                requestDetail.setState(BaseEnum.Draft);
                                requestDetail.setSyncState(BaseEnum.NeSincronizat);
                                mRealm.commitTransaction();

                                if (linesRequestDetail != null)
                                    linesRequestDetail.setBlocked(false);

                                orderShare.setVisibility(View.GONE);
                                orderTextStateColor.setVisibility(View.GONE);
                                orderSaveChanges.setVisibility(View.VISIBLE);
                                checkDiscount.setVisibility(View.VISIBLE);
                                orderSaveChanges.setOnClickListener(v1 -> {
                                    finish();
                                });
                                orderSaveAndSendOrExit.setText(getString(R.string.save_and_send_order));
                                orderSaveAndSendOrExit.setOnClickListener(v -> {
                                    SaveRequestBody order = new SaveRequestBody();
                                    List<RequestLineBody> orderLines = new ArrayList<>();

                                    order.setClientUid(requestDetail.getClientUid());
                                    order.setClientName(requestDetail.getClientName());
                                    order.setComment(requestDetail.getComment());
                                    order.setDeliveryAddress(requestDetail.getDeliveryAddress());
                                    order.setState(BaseEnum.InQueue);
                                    order.setSum(requestDetail.getSum());

                                    if(requestDetail.getUid() != null)
                                        order.setUid(requestDetail.getUid());

                                    RealmList<RequestLine> reqLines = requestDetail.getLines();
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
                                });
                                orderAddComment.setVisibility(View.VISIBLE);
                                orderAddProducts.setVisibility(View.VISIBLE);
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<RequestList> call, Throwable t) {
                            progressDialog.dismiss();
                        }
                    });
                });

            }
            else if(state == 2){
                textState = getString(R.string.order_state_in_work);
                orderTextStateColor.setVisibility(View.VISIBLE);
                orderTextStateColor.setText(getString(R.string.order_is_in_work_can_only_view_detail));
                orderTextStateColor.setBackgroundColor(getColor(R.color.red));
            }
            else if(state == 3){
                textState = getString(R.string.order_state_prepared);
                orderTextStateColor.setVisibility(View.VISIBLE);
                orderTextStateColor.setText(getString(R.string.order_is_prepared_can_only_view_detail));
                orderTextStateColor.setBackgroundColor(getColor(R.color.teal_200));
            }
            else if(state == 6){
                textState = getString(R.string.order_state_final);
                orderTextStateColor.setVisibility(View.VISIBLE);
                orderTextStateColor.setText(getString(R.string.order_is_final_can_only_view_detail));
                orderTextStateColor.setBackgroundColor(getColor(R.color.teal_200));
            }

            String requestCode = requestDetail.getCode() != null ? " - " + requestDetail.getCode() : "";

            orderClientNameAndCode.setText(requestDetail.getClientName() + requestCode);
            orderTextState.setText(getString(R.string.order_state_text) + textState);
            orderTotalCost.setText( requestDetail.getSum() + " MDL");
            orderComment = requestDetail.getComment();
            orderTextComment.setText(orderComment);
            orderAddress.setText(requestDetail.getDeliveryAddress());

            RealmList<RequestLine> lines = requestDetail.getLines();
            linesRequestDetail = new AdapterLinesRequestDetail(lines, this,getLayoutInflater());
            linesRequestDetail.setBlocked(isBlocked);
            linesList.setAdapter(linesRequestDetail);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 6547){
            if(resultCode == RESULT_OK){
                requestDetail = mRealm.where(Request.class).equalTo("internId", id).findFirst();
                if(requestDetail != null){
                    RealmList<RequestLine> lines = requestDetail.getLines();
                    linesRequestDetail = new AdapterLinesRequestDetail(lines, this,getLayoutInflater());
                    linesRequestDetail.setBlocked(false);
                    linesList.setAdapter(linesRequestDetail);

                    orderTotalCost.setText(String.format("%.2f",requestDetail.getSum()).replace(",",".") + " MDL");
                    orderSaveChanges.setText(getString(R.string.save_changes_orders) + "(" + changes + ")");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        requestDetail = null;
        mRealm.close();
        mRealm = null;
    }

    private void saveOrder(SaveRequestBody order){
        Call<SaveRequestResult> saveOrder = orderServiceAPI.saveRequest(order, token);

        progressDialog.setMessage(getString(R.string.dialog_msdg_title_save_order));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel_dialog),(dialog, which) -> {
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
                        requestDetail.setCode(saveRequestResult.getCode());
                        requestDetail.setUid(saveRequestResult.getUid());
                        requestDetail.setState(saveRequestResult.getState());
                        requestDetail.setSyncState(BaseEnum.Syncronizat);
                        mRealm.commitTransaction();

                        progressDialog.dismiss();
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
                        .setTitle(getString(R.string.dialog_msg_error_save_order))
                        .setMessage(t.getMessage())
                        .setPositiveButton(getString(R.string.dialog_button_ok), (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setNeutralButton(getString(R.string.dialog_button_retry), (dialog, which) -> {
                            dialog.dismiss();
                            saveOrder(order);
                        })
                        .show();

            }
        });
    }

    private void shareDocument (){
        progressDialog.setMessage(getString(R.string.dialog_msg_load_image));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        Call<GetPrintRequest> call = orderServiceAPI.getPrintRequest(token, requestDetail.getUid());

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
                        File newFile = new File(cachePath, "/" + requestDetail.getClientName().trim() + "-" + requestDetail.getCode() + ".png");
                        FileOutputStream stream = new FileOutputStream(newFile); // overwrites this image every time
                        decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    File imagePath = new File(Environment.getExternalStorageDirectory() + "/SalesAgent");
                    File newFile = new File(imagePath, "/" + requestDetail.getClientName().trim() + "-" + requestDetail.getCode() + ".png");
                    Uri contentUri = FileProvider.getUriForFile(OrderDetailActivity.this, "md.intelectsoft.salesagent.fileprovider", newFile);

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
                Toast.makeText(OrderDetailActivity.this, "Error download image!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static  void changeCountIntoLine (String assortmentUid, double count, boolean replace){
        RealmList<RequestLine> lines = requestDetail.getLines();
        for(RequestLine line : lines){
            if(line.getAssortimentUid().equals(assortmentUid)){
                double linePrice= line.getPrice();
                double lineCount = line.getCount();
                if(replace){
                    mRealm.beginTransaction();
                    requestDetail.setSum(requestDetail.getSum() - line.getSum());
                    line.setSum(count * linePrice);
                    line.setCount(count);
                    requestDetail.setSum(requestDetail.getSum() + line.getSum());
                    mRealm.commitTransaction();
                }
                else{
                    if(lineCount + count > 0) {
                        lineCount = lineCount + count;

                        mRealm.beginTransaction();
                        requestDetail.setSum(requestDetail.getSum() - line.getSum());
                        line.setSum(lineCount * linePrice);
                        line.setCount(lineCount);
                        requestDetail.setSum(requestDetail.getSum() + line.getSum());
                        mRealm.commitTransaction();
                    }
                }
                changes+=1;
                orderSaveChanges.setText(context.getString(R.string.save_changes_orders) + "(" + changes + ")");

                break;
            }
        }
        double sum = requestDetail.getSum() != null ? requestDetail.getSum() : 0 ;
        orderTotalCost.setText(String.format("%.2f", sum).replace(",",".") + " MDL");
    }

    public static void removeLine(String assortmentUid){
        RealmList<RequestLine> lines = requestDetail.getLines();
        for(RequestLine line : lines){
            if(line.getAssortimentUid().equals(assortmentUid)){
                double lineSum = line.getSum();

                mRealm.beginTransaction();
                line.deleteFromRealm();
                requestDetail.setSum(requestDetail.getSum() - lineSum);
                mRealm.commitTransaction();

                changes+=1;
                orderSaveChanges.setText(context.getString(R.string.save_changes_orders) + "(" + changes + ")");
                break;
            }
        }

        double sum = requestDetail.getSum() != null ? requestDetail.getSum() : 0 ;
        orderTotalCost.setText(String.format("%.2f", sum).replace(",",".") + " MDL");
    }

    public void requestIsBlocked(){
        orderSaveChanges.setVisibility(View.GONE);
        orderSaveAndSendOrExit.setText(getString(R.string.exit_button));
        orderSaveAndSendOrExit.setOnClickListener(v -> finish());
        orderAddComment.setVisibility(View.GONE);
        orderAddProducts.setVisibility(View.GONE);
        checkDiscount.setVisibility(View.GONE);
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

    public static void addChanges(){
        changes +=1;
    }
}