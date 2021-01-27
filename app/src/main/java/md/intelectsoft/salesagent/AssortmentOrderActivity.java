package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.salesagent.Adapters.AdapterProductsList;
import md.intelectsoft.salesagent.AppUtils.BaseEnum;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.PriceList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.SaveRequestResult;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentDescription.AssortmentDescription;
import md.intelectsoft.salesagent.OrderServiceUtils.body.saveRequest.RequestLineBody;
import md.intelectsoft.salesagent.OrderServiceUtils.body.saveRequest.SaveRequestBody;
import md.intelectsoft.salesagent.RealmUtils.Assortment;
import md.intelectsoft.salesagent.RealmUtils.Price;
import md.intelectsoft.salesagent.RealmUtils.Request;
import md.intelectsoft.salesagent.RealmUtils.RequestLine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.nikartm.support.ImageBadgeView;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

@SuppressLint("NonConstantResourceId")
public class AssortmentOrderActivity extends AppCompatActivity {
    @BindView(R.id.productsListAppNameAssortmentOrder) TextView headerActivityName;
    @BindView(R.id.gridAssortmentOrder) GridView gridView;
    @BindView(R.id.layoutDOMAssortment) LinearLayout layoutDOM;
    @BindView(R.id.imageGoHomeListAssortment) ImageView goHomeList;
    @BindView(R.id.searchAssortment) SearchView searchProducts;
    static ImageBadgeView shopCart;

    AdapterProductsList adapterProductsList;
    SharedPreferences sharedPreferencesSettings;
    ProgressDialog progressDialog;
    TimerTask timerTaskSearchText;
    DisplayMetrics displayMetrics;
    OrderServiceAPI orderServiceAPI;
    String token, clientPriceListUid;
    static String requestId, clientUid, clientName, outletsAddress;
    Timer timerSearch;
    Context context;
    Request requestOrder;
    static Realm mRealm;

    @OnClick(R.id.textBackMainFromAssortmentOrderCreate) void onBckClick() {
        int childCount = layoutDOM.getChildCount();
        if(childCount == 1){
            String lang = LocaleHelper.getLanguage(this);

            setAppLocale(lang);
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.dialog_msg_warning))
                    .setMessage(getString(R.string.dialog_msg_log_out_user))
                    .setPositiveButton(getString(R.string.save_local_and_exit_dialog_exit), (dialogInterface, i) -> {
                        setResult(7777);
                        dialogInterface.dismiss();
                        finish();
                    })
                    .setNegativeButton(getString(R.string.exit_button),(dialog, which) -> {
                        Request req = mRealm.where(Request.class).equalTo("internId",requestId).findFirst();
                        assert req != null;
                        RealmList<RequestLine> requestLines = req.getLines();
                        mRealm.beginTransaction();
                        if(requestLines != null && requestLines.size() > 0)
                            requestLines.deleteAllFromRealm();

                        req.deleteFromRealm();
                        mRealm.commitTransaction();

                        dialog.dismiss();
                        finish();
                    })
                    .setNeutralButton(getString(R.string.send_to_server_dialog_exit), (dialog, which) -> {
                        requestOrder = mRealm.where(Request.class).equalTo("internId", requestId).findFirst();
                        if(requestOrder != null){
                            SaveRequestBody order = new SaveRequestBody();
                            List<RequestLineBody> orderLines = new ArrayList<>();

                            order.setClientUid(requestOrder.getClientUid());
                            order.setClientName(requestOrder.getClientName());
                            order.setComment(requestOrder.getComment());
                            order.setDeliveryAddress(requestOrder.getDeliveryAddress());
                            order.setState(BaseEnum.InQueue);
                            order.setSum(requestOrder.getSum());

                            RealmList<RequestLine> reqLines = requestOrder.getLines();
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
                    })
                    .show();


//            View dialogView = getLayoutInflater().inflate(R.layout.dialog_exit_from_assortment, null);
//
//            Dialog orderDialog = new Dialog(context,R.style.CustomDialog);
//            orderDialog.setContentView(dialogView);
//
//            Button saveExit = dialogView.findViewById(R.id.saveAndExitFromNewOrder);
//            Button exit = dialogView.findViewById(R.id.closeDialogExit);
//            Button sendExit = dialogView.findViewById(R.id.saveAndSendExitFromNewOrder);
//
//            saveExit.setOnClickListener(v -> {
//                setResult(7777);
//                orderDialog.dismiss();
//                finish();
//            });
//
//            exit.setOnClickListener(v -> {
//                Request req = mRealm.where(Request.class).equalTo("internId",requestId).findFirst();
//                assert req != null;
//                RealmList<RequestLine> requestLines = req.getLines();
//                mRealm.beginTransaction();
//                if(requestLines != null && requestLines.size() > 0)
//                    requestLines.deleteAllFromRealm();
//
//                req.deleteFromRealm();
//                mRealm.commitTransaction();
//
//                orderDialog.dismiss();
//                finish();
//            });
//
//            sendExit.setOnClickListener(v -> {
//                requestOrder = mRealm.where(Request.class).equalTo("internId", requestId).findFirst();
//                if(requestOrder != null){
//                    SaveRequestBody order = new SaveRequestBody();
//                    List<RequestLineBody> orderLines = new ArrayList<>();
//
//                    order.setClientUid(requestOrder.getClientUid());
//                    order.setClientName(requestOrder.getClientName());
//                    order.setComment(requestOrder.getComment());
//                    order.setDeliveryAddress(requestOrder.getDeliveryAddress());
//                    order.setState(BaseEnum.InQueue);
//                    order.setSum(requestOrder.getSum());
//
//                    RealmList<RequestLine> reqLines = requestOrder.getLines();
//                    if(reqLines != null && reqLines.size() > 0){
//                        for(RequestLine requestLine : reqLines){
//                            RequestLineBody lines = new RequestLineBody();
//                            lines.setAssortimentUid(requestLine.getAssortimentUid());
//                            lines.setCount(requestLine.getCount());
//
//                            orderLines.add(lines);
//                        }
//                    }
//
//                    order.setLines(orderLines);
//
//                    saveOrder(order);
//                }
//            });
//
//            orderDialog.show();
//
//            int displayWidth = displayMetrics.widthPixels;
//            int displayHeight = displayMetrics.heightPixels;
//            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//            layoutParams.copyFrom(orderDialog.getWindow().getAttributes());
//
//            layoutParams.width = (int) (displayWidth * 0.8f);
//            layoutParams.height = (int) (displayHeight * 0.4f);  //LinearLayout.LayoutParams.WRAP_CONTENT
//            orderDialog.getWindow().setAttributes(layoutParams);
        }
        else {
            layoutDOM.removeViewAt(childCount - 1);
            int index = layoutDOM.getChildCount() - 1;

            Assortment assortmentEntry = (Assortment)layoutDOM.getChildAt(index).getTag();
            if(assortmentEntry != null)
                showProducts(assortmentEntry.getUid());
            else
                showProducts("00000000-0000-0000-0000-000000000000");
        }
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
                        requestOrder.setCode(saveRequestResult.getCode());
                        requestOrder.setUid(saveRequestResult.getUid());
                        requestOrder.setState(saveRequestResult.getState());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_assortment_order);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        context = this;
        mRealm = Realm.getDefaultInstance();
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        shopCart = findViewById(R.id.imageShopCartAssortment);
        progressDialog = new ProgressDialog(context);
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);
        setNameTextColor();
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Intent fromClient = getIntent();
        clientUid = fromClient.getStringExtra("uid");
        clientName = fromClient.getStringExtra("name");
        outletsAddress = fromClient.getStringExtra("address");
        clientPriceListUid = fromClient.getStringExtra("priceUid");
        boolean newRequest = fromClient.getBooleanExtra("newRequest",false);

        showProducts("00000000-0000-0000-0000-000000000000");

        if(newRequest) {
            requestId = UUID.randomUUID().toString();
            boolean isCreated = createNewRequest(requestId);
            Log.d("TAG", "onCreate request: " + requestId + " is " + isCreated);
        }

        goHomeList.setOnClickListener(v -> {
            ViewGroup parent  = (ViewGroup) v.getParent();
            int count = parent.getChildCount();

            for (int i = count - 1; i > 0; i--){
                parent.removeViewAt(i);
            }
            showProducts("00000000-0000-0000-0000-000000000000");
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Assortment clicked = adapterProductsList.getItem(position);
            if( !clicked.getFolder()){
                //request info about product and set dialog with information
                Call<AssortmentDescription> call = orderServiceAPI.getAssortmentDescription(token, clicked.getUid());

                progressDialog.setMessage("Obtain information about product...");
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",(dialog, which) -> {
                    call.cancel();
                    if(call.isCanceled())
                        dialog.dismiss();
                });
                progressDialog.show();

                call.enqueue(new Callback<AssortmentDescription>() {
                    @Override
                    public void onResponse(Call<AssortmentDescription> call, Response<AssortmentDescription> response) {
                        AssortmentDescription description = response.body();
                        progressDialog.dismiss();

                        if(description != null && description.getErrorCode() == 0)
                            showDialogProductInfo(description,clicked);
                        else
                            showDialogProductInfo(null,clicked);
                    }

                    @Override
                    public void onFailure(Call<AssortmentDescription> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Load information failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
            else{
                //show assortment into folder
                String clickedId = clicked.getUid();
                String clickedName = clicked.getName();

                TextView folder = new TextView(context);
                folder.setText(" / " + clickedName);
                folder.setTag(clicked);
                folder.setTextSize(20);
                folder.setGravity(Gravity.CENTER);
                folder.setOnClickListener(buttons_);
                folder.setTextColor(getColor(R.color.gray_text));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                lp.setMargins(5,-5,0,-5);
                layoutDOM.addView(folder, lp);

                showProducts(clickedId);
            }

        });

        searchProducts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                if (timerSearch != null)
                    timerSearch.cancel();
                timerSearch = new Timer();

                startTimerSearchText(searchText);
                timerSearch.schedule(timerTaskSearchText, 1200);

                return true;
            }
        });
        searchProducts.setOnCloseListener(() -> {
            showProducts("00000000-0000-0000-0000-000000000000");
            return false;
        });

        shopCart.setOnClickListener(v -> {
            Intent shopCart = new Intent(context, ShopCartActivity.class);
            shopCart.putExtra("id", requestId);
            shopCart.putExtra("clientUid", clientUid);
            startActivityForResult(shopCart,100);
        });
    }

    View.OnClickListener buttons_ = view -> {
        Assortment assortmentEntry = (Assortment)view.getTag();
        ViewGroup parent  = (ViewGroup) view.getParent();
        int count = parent.getChildCount();

        for (int i = count - 1; i > 0; i--){
            TextView vi = (TextView) parent.getChildAt(i);
            Assortment entry = (Assortment) vi.getTag();
            if(!entry.getUid().equals(assortmentEntry.getUid())){
                parent.removeViewAt(i);
            }
            else if(entry.getUid().equals(assortmentEntry.getUid())){
                break;
            }
        }
        showProducts(assortmentEntry.getUid());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == 777) {
                setResult(7777);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
    }

    public static void addProductToCart(Assortment product, double count){
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
            if (!createNewRequest((requestId))) {
                requestId = null;
            }
        }

        Request req = mRealm.where(Request.class).equalTo("internId",requestId).findFirst();
        assert req != null;

        RealmList<RequestLine> requestLines = req.getLines();
        double sumItemsAdded = 0;
        if (requestLines.size() > 0) {
            String uidItem = product.getUid();
            boolean existItem = false;

            for (RequestLine line : requestLines) {
                if (uidItem.equals(line.getAssortimentUid())) {
                    double existQuantity = line.getCount();
                    existQuantity = existQuantity + count;
                    mRealm.beginTransaction();
                    line.setCount(existQuantity);
                    if(product.getPriceDiscount() > 0){
                        line.setPriceDialler(product.getPriceDiscount());
                        line.setSum(product.getPriceDiscount() * existQuantity);
                    }
                    else{
                        line.setSum(product.getPrice() * existQuantity);
                    }
                    mRealm.commitTransaction();
                    existItem = true;
                    sumItemsAdded += (count * product.getPrice());
                }
            }
            if (!existItem) {
                RequestLine line = new RequestLine();
                line.setAssortimentBarcode(product.getBarCode());
                line.setAssortimentCode(product.getCode());
                line.setAssortimentName(product.getName());
                line.setAssortimentUid(product.getUid());
                line.setCount(count);
                line.setPrice(product.getPrice());
                line.setPriceLineUid(product.getPricelineUid());

                if(product.getPriceDiscount() > 0){
                    line.setPriceDialler(product.getPriceDiscount());
                    line.setSum(product.getPriceDiscount() * count);
                }
                else
                    line.setSum(product.getPrice() * count);

                sumItemsAdded += line.getSum();

                Request result = mRealm.where(Request.class).equalTo("internId", requestId).findFirst();
                if (result != null) {
                    mRealm.beginTransaction();
                    result.getLines().add(line);
                    result.setSum(result.getSum() + sumItemsAdded);
                    mRealm.commitTransaction();

                    int curBadge = shopCart.getBadgeValue();
                    shopCart.setBadgeValue(curBadge + 1);
                }
            } else {
                Request result = mRealm.where(Request.class).equalTo("internId", requestId).findFirst();
                if (result != null) {
                    mRealm.beginTransaction();
                    result.setSum(result.getSum() + sumItemsAdded);
                    mRealm.commitTransaction();
                }
            }
        } else {
            RequestLine line = new RequestLine();
            line.setAssortimentBarcode(product.getBarCode());
            line.setAssortimentCode(product.getCode());
            line.setAssortimentName(product.getName());
            line.setAssortimentUid(product.getUid());
            line.setCount(count);
            line.setPrice(product.getPrice());
            line.setPriceLineUid(product.getPricelineUid());
            if(product.getPriceDiscount() > 0){
                line.setPriceDialler(product.getPriceDiscount());
                line.setSum(product.getPriceDiscount() * count);
            }
            else
                line.setSum(product.getPrice() * count);

            sumItemsAdded += line.getSum();

            Request result = mRealm.where(Request.class).equalTo("internId", requestId).findFirst();
            if (result != null) {
                mRealm.beginTransaction();
                result.getLines().add(line);
                result.setSum(sumItemsAdded);
                mRealm.commitTransaction();

                int curBadge = shopCart.getBadgeValue();
                shopCart.setBadgeValue(curBadge + 1);
            }
        }
    }

    public static boolean createNewRequest(String id){
        try {
            Request request = new Request();
            request.setInternId(id);
            request.setClientName(clientName);
            request.setClientUid(clientUid);
            request.setState(BaseEnum.Draft);
            request.setDateToLong(new Date().getTime());
            request.setSyncState(BaseEnum.NeSincronizat);
            if(outletsAddress != null && !outletsAddress.equals(""))
                request.setDeliveryAddress(outletsAddress);

            mRealm.beginTransaction();
            mRealm.insert(request);
            mRealm.commitTransaction();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showDialogProductInfo(AssortmentDescription description, Assortment clicked){

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_info_add_count, null);

        Dialog productInfoDialog = new Dialog(context,R.style.CustomDialog);
        productInfoDialog.setContentView(dialogView);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);

        TextView productName = dialogView.findViewById(R.id.textDialogInfoProductName);
        TextView productDiscount = dialogView.findViewById(R.id.textDialogInfoProductDiscount);
        TextView productCode = dialogView.findViewById(R.id.textDialogInfoProductCode);
        TextView productPrice = dialogView.findViewById(R.id.textDialogInfoProductPrice);
        TextView productRemain = dialogView.findViewById(R.id.textDialogInfoProductRemain);
        WebView productDescription = dialogView.findViewById(R.id.webDialogInfoProductDescription);
        Button addToCart = dialogView.findViewById(R.id.addItemToCartDialogProduct);
        EditText textCount = dialogView.findViewById(R.id.editTextCountDialogProduct);
        ImageView addCount = dialogView.findViewById(R.id.imageAddCountDialogProduct);
        ImageView deleteCount = dialogView.findViewById(R.id.imageDeleteCountDialogProduct);
        ImageView imageProduct = dialogView.findViewById(R.id.imageDialogInfoProduct);

        String name = clicked.getName();
        String content = "";
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.product_image);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] image = stream.toByteArray();

        if(description != null){
            if(description.getTranslatedName().getRO() != null && !description.getTranslatedName().getRO().equals(""))
                name = description.getTranslatedName().getRO();
            else if(description.getTranslatedName().getEN() != null && !description.getTranslatedName().getEN().equals(""))
                name = description.getTranslatedName().getEN();
            else if(description.getTranslatedName().getRU() != null && !description.getTranslatedName().getRU().equals(""))
                name = description.getTranslatedName().getRU();

            if(description.getImage1() != null)
                image = description.getImage1();
            else if(description.getImage2() != null)
                image = description.getImage2();
            else if(description.getImage3() != null)
                image = description.getImage3();
            else if(description.getImage4() != null)
                image = description.getImage4();

            content = description.getTranslatedDescription().getRO();
        }

        productName.setText(name);
        productRemain.setText(clicked.getRemain() + " " + clicked.getUnitName());
        productDescription.loadDataWithBaseURL(null, content,"text/html","UTF-8", null);

        Bitmap bmpImage = BitmapFactory.decodeByteArray(image, 0, image.length);
        imageProduct.setImageBitmap(Bitmap.createScaledBitmap(bmpImage, 211, 211, false));

        productDiscount.setVisibility(View.GONE);
        productPrice.setText("MDL " + clicked.getPrice() + " / " + clicked.getUnitName());
        productCode.setText("#" + clicked.getCode());

        addCount.setOnClickListener(v -> {
            String count = textCount.getText().toString();
            if(count.equals(""))
                count = "0";

            double countDouble = Double.parseDouble(count) + 1;
            textCount.setText(String.format("%.2f", countDouble).replace(",","."));
        });
        deleteCount.setOnClickListener(v -> {
            String count = textCount.getText().toString();
            if(count.equals(""))
                count = "0";
            double countDouble = Double.parseDouble(count);
            if(countDouble - 1 > 0)
                textCount.setText(String.format("%.2f", countDouble - 1).replace(",","."));
        });
        addToCart.setOnClickListener(v -> {
            String count = textCount.getText().toString();
            if(count.equals(""))
                count = "1";

            double countDouble = Double.parseDouble(count);
            addProductToCart(clicked,countDouble);

            productInfoDialog.dismiss();
        });

        productInfoDialog.show();

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(productInfoDialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.45f);
        int dialogWindowHeight = (int) (displayHeight * 0.8f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;  //LinearLayout.LayoutParams.WRAP_CONTENT
        productInfoDialog.getWindow().setAttributes(layoutParams);
    }

    private void searchText(String newText) {
        RealmResults<Assortment> result = mRealm.where(Assortment.class)
                .contains("name", newText, Case.INSENSITIVE).or()
                .contains("code", newText, Case.INSENSITIVE).or()
                .contains("barCode", newText, Case.INSENSITIVE).or()
                .sort("name", Sort.ASCENDING)
                .findAllAsync();
        adapterProductsList = new AdapterProductsList(this, result,false);
        gridView.setAdapter(adapterProductsList);

        result.addChangeListener(realmChangeListener);
    }

    private void startTimerSearchText(final String newText) {
        timerTaskSearchText = new TimerTask() {
            @Override
            public void run() {
                AssortmentOrderActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (newText.length() >= 3) {
                            searchText(newText);
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private RealmChangeListener<RealmResults<Assortment>> realmChangeListener = new RealmChangeListener<RealmResults<Assortment>>() {
        @Override
        public void onChange(RealmResults<Assortment> realmResults) {
            adapterProductsList.setData(realmResults);
        }
    };

    private void showProducts(String parentId) {
        RealmResults<Assortment> result = null;
        PriceList priceList = null;
        mRealm.beginTransaction();
        result = mRealm.where(Assortment.class).equalTo("parentUid", parentId).findAllAsync();
        priceList = mRealm.where(PriceList.class).equalTo("priceListUid", clientPriceListUid).findFirst();
        mRealm.commitTransaction();

        result = result.sort("name", Sort.ASCENDING);
        result = result.sort("isFolder", Sort.DESCENDING);

        List<Assortment> lisOfAssortment = mRealm.copyFromRealm(result);

        if(priceList != null){
            RealmList<Price> listPrices = priceList.getPrices();

            if(listPrices != null && listPrices.size() > 0){
                for(int i = 0; i < lisOfAssortment.size(); i++){
                    Assortment item = lisOfAssortment.get(i);
                    if(!item.getFolder()){
                        Price discount = listPrices.where().equalTo("priceLineUid", item.getPricelineUid()).or().equalTo("assortimentUid", item.getUid()).findFirst();
                        if(discount != null){
                            Log.d("TAG", "showProducts: discount price" + discount.getPrice());
                            item.setPriceDiscount(discount.getPrice());
                        }
                    }
                }
            }
        }

        adapterProductsList = new AdapterProductsList(this, lisOfAssortment,false);
        gridView.setAdapter(adapterProductsList);

//        result.addChangeListener(realmChangeListener);
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

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + getString(R.string.shopping_cart_orders));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
    }
}