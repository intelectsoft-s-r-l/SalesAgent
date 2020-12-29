package md.intelectsoft.salesagent;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.salesagent.Adapters.AdapterListRequest;
import md.intelectsoft.salesagent.AppUtils.BaseEnum;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.GetPrintRequest;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.RequestList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.SaveRequestResult;
import md.intelectsoft.salesagent.OrderServiceUtils.body.saveRequest.RequestLineBody;
import md.intelectsoft.salesagent.OrderServiceUtils.body.saveRequest.SaveRequestBody;
import md.intelectsoft.salesagent.RealmUtils.Request;
import md.intelectsoft.salesagent.RealmUtils.RequestLine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

@SuppressLint("NonConstantResourceId")
public class OrdersListActivity extends AppCompatActivity {
    @BindView(R.id.list_request) ListView ordersList;
    @BindView(R.id.orderListAppName) TextView headerActivityName;
    @BindView(R.id.buttonSortOrders) ImageButton filterOrdersButton;

    SimpleDateFormat sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
    SharedPreferences sharedPreferencesSettings;
    AdapterListRequest adapterListRequest;
    OrderServiceAPI orderServiceAPI;
    ProgressDialog progressDialog;
    Request clickedRequest;
    ActionMode actionMode;
    Context context;
    String token;
    Realm mRealm;

    @OnClick(R.id.textBackMain) void back(){
        finish();
    }

    @OnClick(R.id.buttonSortOrders) void onSortOrders() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_orders, null);

        Dialog sortOrdersDialog = new Dialog(context,R.style.CustomDialog);
        sortOrdersDialog.setContentView(dialogView);

        Button applyFilter = dialogView.findViewById(R.id.applyFilterSortOrders);
        TextView sortByDate = dialogView.findViewById(R.id.textFilterOrderDate);
        TextView sortByCode = dialogView.findViewById(R.id.textFilterOrderCode);
        TextView clearFilters = dialogView.findViewById(R.id.textClearFilter);
        ConstraintLayout layoutDate = dialogView.findViewById(R.id.layoutSortDate);
        ConstraintLayout layoutCode = dialogView.findViewById(R.id.layoutSortCode);
        ImageView imageDate = dialogView.findViewById(R.id.imageExpandByDate);
        ImageView imageCode = dialogView.findViewById(R.id.imageExpandByCode);
        ChipGroup sortByStateGroup = dialogView.findViewById(R.id.chipGroup);
        CheckBox saveFilter = dialogView.findViewById(R.id.checkBoxSaveFilter);
        Chip chipDraft = dialogView.findViewById(R.id.chipDraft);
        Chip chipQueue = dialogView.findViewById(R.id.chipQueue);
        Chip chipWork = dialogView.findViewById(R.id.chipWork);
        Chip chipPrepared = dialogView.findViewById(R.id.chipPrepared);


        final int[] sortDate = {0};
        final int[] sortCode = {0};

        // 1 - ascending , 0 - descending

        boolean isSaved = sharedPreferencesSettings.getBoolean("saveFilter", false);

        saveFilter.setChecked(isSaved);

        if(isSaved){
            sortDate[0] = sharedPreferencesSettings.getInt("sortDate", 0);
            sortCode[0] = sharedPreferencesSettings.getInt("sortCode", 0);

            if(sharedPreferencesSettings.getBoolean("draftOrders", false))
                chipDraft.setChecked(true);
            if(sharedPreferencesSettings.getBoolean("queueOrders", false))
                chipQueue.setChecked(true);
            if(sharedPreferencesSettings.getBoolean("workOrders", false))
                chipWork.setChecked(true);
            if(sharedPreferencesSettings.getBoolean("preparedOrders", false))
                chipPrepared.setChecked(true);

            if(sortDate[0] == 0)
                sortByDate.setText("Descending");
            else{
                ObjectAnimator rotate = ObjectAnimator.ofFloat(imageDate, "rotation", 0f, 180f);
                rotate.setDuration(300);
                rotate.start();

                sortByDate.setText("Ascending");
            }
            if(sortCode[0] == 0)
                sortByCode.setText("Descending");
            else{
                ObjectAnimator rotate = ObjectAnimator.ofFloat(imageCode, "rotation", 0f, 180f);
                rotate.setDuration(300);
                rotate.start();

                sortByCode.setText("Ascending");
            }
        }

        layoutDate.setOnClickListener(v -> {
            if(sortDate[0] == 0){
                ObjectAnimator rotate = ObjectAnimator.ofFloat(imageDate, "rotation", 0f, 180f);
                rotate.setDuration(300);
                rotate.start();

                sortByDate.setText("Ascending");
                sortDate[0] = 1;
            }
            else{
                ObjectAnimator rotate = ObjectAnimator.ofFloat(imageDate, "rotation", 180f, 0f);
                rotate.setDuration(300);
                rotate.start();

                sortByDate.setText("Descending");
                sortDate[0] = 0;
            }
        });

        layoutCode.setOnClickListener(v -> {
            if(sortCode[0] == 0){
                ObjectAnimator rotate = ObjectAnimator.ofFloat(imageCode, "rotation", 0f, 180f);
                rotate.setDuration(300);
                rotate.start();

                sortByCode.setText("Ascending");
                sortCode[0] = 1;
            }
            else{
                ObjectAnimator rotate = ObjectAnimator.ofFloat(imageCode, "rotation", 180f, 0f);
                rotate.setDuration(300);
                rotate.start();

                sortByCode.setText("Descending");
                sortCode[0] = 0;
            }
        });

        clearFilters.setOnClickListener(v -> {
            chipDraft.setChecked(false);
            chipQueue.setChecked(false);
            chipWork.setChecked(false);
            chipPrepared.setChecked(false);

            if(sortDate[0] != 0){
                ObjectAnimator rotate = ObjectAnimator.ofFloat(imageDate, "rotation", 180f, 0f);
                rotate.setDuration(300);
                rotate.start();

                sortByDate.setText("Descending");
                sortDate[0] = 0;
            }
            if(sortCode[0] != 0){
                ObjectAnimator rotate = ObjectAnimator.ofFloat(imageCode, "rotation", 180f, 0f);
                rotate.setDuration(300);
                rotate.start();

                sortByCode.setText("Descending");
                sortCode[0] = 0;
            }
            if(saveFilter.isChecked()){
                saveFilter.setChecked(false);
                sharedPreferencesSettings.edit()
                        .putBoolean("saveFilter", false)
                        .apply();
            }
        });

        applyFilter.setOnClickListener(v -> {
            if(saveFilter.isChecked()){
                sharedPreferencesSettings.edit()
                        .putBoolean("saveFilter", true)
                        .putBoolean("draftOrders", chipDraft.isChecked())
                        .putBoolean("queueOrders", chipQueue.isChecked())
                        .putBoolean("workOrders", chipWork.isChecked())
                        .putBoolean("preparedOrders", chipPrepared.isChecked())
                        .putInt("sortDate", sortDate[0])
                        .putInt("sortCode", sortCode[0])
                        .apply();
            }
            else
                sharedPreferencesSettings.edit()
                        .putBoolean("saveFilter", false)
                        .apply();

            List<Integer> chipsChecked = sortByStateGroup.getCheckedChipIds();

            showOrdersSorted(chipsChecked, sortCode[0] != 0, sortDate[0] != 0);
            sortOrdersDialog.dismiss();
        });


        sortOrdersDialog.show();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(sortOrdersDialog.getWindow().getAttributes());
        layoutParams.width = 480;
        layoutParams.height = 630;  //LinearLayout.LayoutParams.WRAP_CONTENT
        sortOrdersDialog.getWindow().setAttributes(layoutParams);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        context = this;
        mRealm = Realm.getDefaultInstance();
        sdfChisinau.setTimeZone(tzInChisinau);
        progressDialog = new ProgressDialog(context);
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);

        int stateAction = getIntent().getIntExtra("TypeOrders",0);

        switch (stateAction){
            case 97: {
                setNameTextColor(getString(R.string.drafts_orders));
            }break;
            case 98: {
                setNameTextColor(getString(R.string.in_queue_orders));
            }break;
            case 99: {
                setNameTextColor(getString(R.string.in_work_orders));
            }break;
            case 100: {
                setNameTextColor(getString(R.string.order_list_header_activity));
            }break;
        }

        showOrdersConformState(stateAction);

        if(stateAction != 100)
            filterOrdersButton.setVisibility(View.GONE);

        ordersList.setOnItemClickListener((parent, view, position, id) -> {
            if(actionMode != null)
                actionMode.finish();
            else{
                clickedRequest = adapterListRequest.getItem(position);
                String idRequest = clickedRequest.getInternId();
                String uidRequest = clickedRequest.getUid();

                Intent detail = new Intent(context, OrderDetailActivity.class);
                detail.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                detail.putExtra("uid", uidRequest);
                detail.putExtra("id",idRequest);

                Log.e("TAG", "onCreate: " + " id : " + idRequest + " uid: " + uidRequest  + " position: " + position + "\n Client name: " + clickedRequest.getClientName());

                startActivity(detail);
            }
        });

        ordersList.setOnItemLongClickListener((parent, view, position, id) -> {
            clickedRequest = adapterListRequest.getItem(position);
            actionMode = startActionMode(callback);
            view.setSelected(true);

            actionMode.setTitle(clickedRequest.getCode() + " " + clickedRequest.getClientName());
            return true;
        });
    }

    public ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.selected_request_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem menuItemDelete = menu.findItem(R.id.item_delete);
            MenuItem menuItemCancel = menu.findItem(R.id.item_cancel);
            MenuItem menuItemSend = menu.findItem(R.id.item_confirm);
            MenuItem menuItemShare = menu.findItem(R.id.item_share);

            menuItemSend.setVisible(clickedRequest.getSyncState() == BaseEnum.NeSincronizat || clickedRequest.getState() == 0);

            menuItemDelete.setVisible(clickedRequest.getSyncState() == BaseEnum.NeSincronizat);

            menuItemCancel.setVisible(clickedRequest.getState() == 1 && clickedRequest.getSyncState() == BaseEnum.Syncronizat);

            menuItemShare.setVisible(clickedRequest.getState() != BaseEnum.Draft && clickedRequest.getSyncState() == BaseEnum.Syncronizat);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_delete:{
                    if(clickedRequest.getState() == 0 && clickedRequest.getSyncState() == BaseEnum.NeSincronizat){
                        mRealm.executeTransaction(realm -> {
                            RealmList<RequestLine> line = clickedRequest.getLines();
                            line.deleteAllFromRealm();
                            clickedRequest.deleteFromRealm();

                            mode.finish();
                        });
                    }

                    return true;
                }
                case R.id.item_confirm:{
                        if(clickedRequest.getState() == 0){
                            SaveRequestBody order = new SaveRequestBody();
                            List<RequestLineBody> orderLines = new ArrayList<>();

                            order.setClientUid(clickedRequest.getClientUid());
                            order.setClientName(clickedRequest.getClientName());
                            order.setComment(clickedRequest.getComment());
                            order.setDeliveryAddress(clickedRequest.getDeliveryAddress());
                            order.setState(BaseEnum.InQueue);
                            order.setSum(clickedRequest.getSum());

                            if(clickedRequest.getUid() != null)
                                order.setUid(clickedRequest.getUid());

                            RealmList<RequestLine> reqLines = clickedRequest.getLines();
                            if(reqLines != null && reqLines.size() > 0){
                                for(RequestLine requestLine : reqLines){
                                    RequestLineBody lines = new RequestLineBody();
                                    lines.setAssortimentUid(requestLine.getAssortimentUid());
                                    lines.setCount(requestLine.getCount());

                                    orderLines.add(lines);
                                }
                            }

                            order.setLines(orderLines);

                            Call<SaveRequestResult> resultRequestListCall = orderServiceAPI.saveRequest(order, token);

                            progressDialog.setMessage("Send order...");
                            progressDialog.setIndeterminate(true);
                            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> {
                                resultRequestListCall.cancel();
                                if(!resultRequestListCall.isCanceled())
                                    progressDialog.dismiss();
                            });
                            progressDialog.show();

                            resultRequestListCall.enqueue(new Callback<SaveRequestResult>() {
                                @Override
                                public void onResponse(Call<SaveRequestResult> call, Response<SaveRequestResult> response) {
                                    SaveRequestResult saveRequestResult = response.body();

                                    progressDialog.dismiss();
                                    if(saveRequestResult != null){
                                        if(saveRequestResult.getErrorCode() == 0){
                                            mRealm.beginTransaction();
                                            clickedRequest.setCode(saveRequestResult.getCode());
                                            clickedRequest.setUid(saveRequestResult.getUid());
                                            clickedRequest.setState(saveRequestResult.getState());
                                            clickedRequest.setSyncState(BaseEnum.Syncronizat);
                                            mRealm.commitTransaction();

                                            mode.finish();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<SaveRequestResult> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Error canceled order: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    return true;
                }
                case R.id.item_cancel: {

                    Call<RequestList> resultRequestListCall = orderServiceAPI.deleteRequest(token, clickedRequest.getUid());

                    progressDialog.setMessage("Canceled order...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> {
                        resultRequestListCall.cancel();
                        if(!resultRequestListCall.isCanceled())
                            progressDialog.dismiss();
                    });
                    progressDialog.show();

                    resultRequestListCall.enqueue(new Callback<RequestList>() {
                        @Override
                        public void onResponse(Call<RequestList> call, Response<RequestList> response) {
                            RequestList resultRequestList = response.body();
                            progressDialog.dismiss();

                            if (resultRequestList != null && resultRequestList.getErrorCode() == 0) {
                                mRealm.executeTransaction(realm -> {
                                    clickedRequest.setState(BaseEnum.Draft);
                                    clickedRequest.setSyncState(BaseEnum.NeSincronizat);
                                });
                                mode.finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<RequestList> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Error canceled order: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                case R.id.item_share: {
                    progressDialog.setMessage("Load image order...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);

                    Call<GetPrintRequest> call = orderServiceAPI.getPrintRequest(token, clickedRequest.getUid());
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> {
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
                            if(printRequest != null) {
                                if (printRequest.getErrorCode() == 0) {
                                    String imageFile = printRequest.getImageFile();

                                    byte[] decodedString = Base64.decode(imageFile, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    // save bitmap to cache directory
                                    try {

                                        File cachePath = new File(context.getCacheDir(), "images");
                                        cachePath.mkdirs(); // don't forget to make the directory
                                        FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                                        decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                        stream.close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    File imagePath = new File(context.getCacheDir(), "images");
                                    File newFile = new File(imagePath, "image.png");
                                    Uri contentUri = FileProvider.getUriForFile(context, "md.intelectsoft.salesagent.fileprovider", newFile);

                                    if (contentUri != null) {
                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                                        shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

                                        startActivity(Intent.createChooser(shareIntent, "Share document!"));
                                    }
                                }else{
                                    Toast.makeText(OrdersListActivity.this, "Error download image!Code: " + printRequest.getErrorCode(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(OrdersListActivity.this, "Error download image!Response is null :(", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GetPrintRequest> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(OrdersListActivity.this, "Error download image!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            adapterListRequest.notifyDataSetInvalidated();
        }
    };

    private void showOrdersSorted(List<Integer> chipsChecked, boolean dateAscending, boolean codeAscending) {
        List<Integer> listState = chipsChecked;
        Sort sortDate = Sort.DESCENDING;
        if (dateAscending)
            sortDate = Sort.ASCENDING;

        Sort sortCode = Sort.DESCENDING;
        if (codeAscending)
            sortCode = Sort.ASCENDING;

        RealmResults<Request> result = null;
        RealmQuery<Request> queried = mRealm.where(Request.class);
        mRealm.beginTransaction();

        if(listState.size() > 0)
            for (int i = 0; i < listState.size(); i++) {
                if (i == 0)
                    switch (listState.get(0)) {
                        case R.id.chipDraft: queried.equalTo("state", BaseEnum.Draft); break;
                        case R.id.chipQueue: queried.equalTo("state", BaseEnum.InQueue);break;
                        case R.id.chipWork: queried.equalTo("state", BaseEnum.InWork);break;
                        case R.id.chipPrepared: queried.equalTo("state", BaseEnum.Prepared);break;
                    }
                else
                    switch (listState.get(i)) {
                        case R.id.chipDraft: queried.or().equalTo("state", BaseEnum.Draft);break;
                        case R.id.chipQueue: queried.or().equalTo("state", BaseEnum.InQueue);break;
                        case R.id.chipWork: queried.or().equalTo("state", BaseEnum.InWork);break;
                        case R.id.chipPrepared: queried.or().equalTo("state", BaseEnum.Prepared); break;

                }
            }

        result = queried.findAllAsync();

        result = result.sort("code", sortCode);
        result = result.sort("dateToLong", sortDate);

        mRealm.commitTransaction();

        adapterListRequest = new AdapterListRequest(result,context);
        ordersList.setAdapter(adapterListRequest);

        assert result != null;
        result.addChangeListener(realmChangeListener);
    }

    private void showOrdersConformState(int stateAction) {
        RealmResults<Request> result = null;
        int states = 100;
        mRealm.beginTransaction();
        switch (stateAction){
            case 97: {
                states = BaseEnum.Draft;
            }break;
            case 98: {
                states = BaseEnum.InQueue;
            }break;
            case 99: {
                states = BaseEnum.InWork;
            }break;
            case 100: {
                states = 100;
            }break;
        }
        if(stateAction != 100)
            result = mRealm.where(Request.class).equalTo("state",states).findAll();
        else
            result = mRealm.where(Request.class).findAll();
        mRealm.commitTransaction();

        adapterListRequest = new AdapterListRequest(result,context);
        ordersList.setAdapter(adapterListRequest);

        Log.e("TAG", "showOrdersConformState: " + adapterListRequest.getCount());

//        assert result != null;
//        result.addChangeListener(realmChangeListener);

    }

    private RealmChangeListener<RealmResults<Request>> realmChangeListener = new RealmChangeListener<RealmResults<Request>>() {
        @Override
        public void onChange(RealmResults<Request> requests) {
            adapterListRequest.updateData(requests);
        }
    };

    private void setNameTextColor(String text){
        SpannableString s = new SpannableString("Sales Agent - " + text);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
    }
}