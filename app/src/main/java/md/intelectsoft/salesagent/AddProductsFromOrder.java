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
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.salesagent.Adapters.AdapterProductsListToOrder;
import md.intelectsoft.salesagent.AppUtils.LocaleHelper;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.PriceList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentDescription.AssortmentDescription;
import md.intelectsoft.salesagent.RealmUtils.Assortment;
import md.intelectsoft.salesagent.RealmUtils.Price;
import md.intelectsoft.salesagent.RealmUtils.Request;
import md.intelectsoft.salesagent.RealmUtils.RequestLine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.nikartm.support.ImageBadgeView;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;
import static md.intelectsoft.salesagent.OrderDetailActivity.addChanges;

@SuppressLint("NonConstantResourceId")
public class AddProductsFromOrder extends AppCompatActivity {
    @BindView(R.id.productsListAppNameAssortmentOrder) TextView headerActivityName;
    @BindView(R.id.gridAssortmentOrder) GridView gridView;
    @BindView(R.id.layoutDOMAssortment)LinearLayout layoutDOM;
    @BindView(R.id.imageGoHomeListAssortment) ImageView goHomeList;
    @BindView(R.id.searchAssortment) SearchView searchProducts;
    static ImageBadgeView orderAddedProducts;

    @BindView(R.id.imageChangeGridColumns2) ImageView changeColumns;

    AdapterProductsListToOrder adapterProductsList;
    SharedPreferences sharedPreferencesSettings;
    ProgressDialog progressDialog;
    TimerTask timerTaskSearchText;
    DisplayMetrics displayMetrics;
    OrderServiceAPI orderServiceAPI;
    String token, clientPriceUid;
    static String requestId;
    Timer timerSearch;
    Context context;
    static Realm mRealm;

    static boolean isViewWithCatalog;
    String clickedId = "00000000-0000-0000-0000-000000000000";
    int currentColumns;
    String searchedText = null;

    @OnClick(R.id.textBackMainFromAssortmentOrder) void onBckClick() {
        int childCount = layoutDOM.getChildCount();
        if(childCount == 1){
            finish();
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
//            orderDialog.show();
//
//            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//            layoutParams.copyFrom(orderDialog.getWindow().getAttributes());
//            layoutParams.width = 750;
//            layoutParams.height = 200;  //LinearLayout.LayoutParams.WRAP_CONTENT
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

    @OnClick(R.id.imageChangeGridColumns2) void onChangeColumns() {
        currentColumns = gridView.getNumColumns();
        if(currentColumns > 1) {
            sharedPreferencesSettings.edit().putBoolean("ViewWithCatalog", false).apply();
            changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_black_24dp));
            isViewWithCatalog = false;
            gridView.setNumColumns(1);
        }else {
            sharedPreferencesSettings.edit().putBoolean("ViewWithCatalog", true).apply();
            changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_black_24dp));
            isViewWithCatalog = true;
            gridView.setNumColumns(6);
        }
        adapterProductsList.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);
        setContentView(R.layout.activity_add_products_from_order);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        context = this;
        mRealm = Realm.getDefaultInstance();
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        progressDialog = new ProgressDialog(context);
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);
        orderAddedProducts = findViewById(R.id.imageProductAddedToOrder);
        setNameTextColor();
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        if(savedInstanceState != null){
            String[] ids = savedInstanceState.getStringArray("ids");
            String[] names = savedInstanceState.getStringArray("names");

            if(ids != null && names != null && ids.length > 1) {
                for (int i = 1; i < ids.length; i++){
                    Assortment clicked = new Assortment();
                    clicked.setUid(ids[i]);
                    clicked.setName(names[i]);

                    TextView folder = new TextView(context);
                    folder.setText(" / " + names[i]);
                    folder.setTag(clicked);
                    folder.setTextSize(20);
                    folder.setGravity(Gravity.CENTER);
                    folder.setOnClickListener(buttons_);
                    folder.setTextColor(getColor(R.color.gray_text));

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
;
                    layoutDOM.addView(folder, lp);
                }

                showProducts(ids[ids.length - 1]);
            }
            else{
                showProducts("00000000-0000-0000-0000-000000000000");
            }

            searchedText = savedInstanceState.getString("textSearched");
            if(searchedText != null){
                searchProducts.setQuery(searchedText, true);
                if (timerSearch != null)
                    timerSearch.cancel();
                timerSearch = new Timer();

                startTimerSearchText(searchedText);
                timerSearch.schedule(timerTaskSearchText, 1200);
            }

            int badges = savedInstanceState.getInt("badges");
            orderAddedProducts.setBadgeValue(badges);

            requestId = savedInstanceState.getString("requestId");

        }
        else{
            Intent fromOrder = getIntent();
            requestId = fromOrder.getStringExtra("internId");
            showProducts("00000000-0000-0000-0000-000000000000");
        }


        goHomeList.setOnClickListener(v -> {
            ViewGroup parent  = (ViewGroup) v.getParent();
            int count = parent.getChildCount();

            for (int i = count - 1; i > 0; i--){
                parent.removeViewAt(i);
            }
            showProducts("00000000-0000-0000-0000-000000000000");
        });

        if(sharedPreferencesSettings.getBoolean("ViewWithCatalog", false)){
            changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_black_24dp));
            isViewWithCatalog = true;
            gridView.setNumColumns(6);
        }
        else{
            changeColumns.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_black_24dp));
            isViewWithCatalog = false;
            gridView.setNumColumns(1);
        }

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Assortment clicked = adapterProductsList.getItem(position);
            if( !clicked.getFolder()){
                //request info about product and set dialog with information
                showDialogProductInfo(null,clicked);


            }
            else{
                //show assortment into folder
                clickedId = clicked.getUid();
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

                searchedText = searchText;
                startTimerSearchText(searchText);
                timerSearch.schedule(timerTaskSearchText, 1200);

                return true;
            }
        });
        searchProducts.setOnCloseListener(() -> {
            showProducts(clickedId);
            return false;
        });


        orderAddedProducts.setOnClickListener(v -> {
            int curBadge = orderAddedProducts.getBadgeValue();
            if(curBadge > 0)
                setResult(RESULT_OK);
            else
                setResult(RESULT_CANCELED);

            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        int sizeTextViews = layoutDOM.getChildCount();

        if(sizeTextViews > 1){
            String[] ids = new String[sizeTextViews];
            String[] names= new String[sizeTextViews];

            ids[0] = "00000000-0000-0000-0000-000000000000";
            names[0] = "";

            for(int i = 1; i < sizeTextViews; i++){
                View item = layoutDOM.getChildAt(i);
                Assortment assortmentEntry = (Assortment)item.getTag();
                ids[i] = assortmentEntry.getUid();
                names[i] = assortmentEntry.getName();

                Log.e("TAG", "onSaveInstanceState: " + assortmentEntry.getUid() + assortmentEntry.getName() );
            }
            bundle.putStringArray("ids", ids);
            bundle.putStringArray("names", names);
        }

        bundle.putString("textSearched", searchedText);
        bundle.putString("requestId",requestId);
        bundle.putInt("badges",orderAddedProducts.getBadgeValue());
    }

    public static boolean isIsViewWithCatalog() {
        return isViewWithCatalog;
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

    public static void addProductToOrder(Assortment product, double count){
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
                        line.setPriceDiscount(product.getPrice() - product.getPriceDiscount());
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
                line.setUnitName(product.getUnitName());

                if(product.getPriceDiscount() > 0){
                    line.setPriceDiscount(product.getPrice() - product.getPriceDiscount());
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

                    int curBadge = orderAddedProducts.getBadgeValue();
                    orderAddedProducts.setBadgeValue(curBadge + 1);
                    addChanges();
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
            line.setUnitName(product.getUnitName());

            if(product.getPriceDiscount() > 0){
                line.setPriceDiscount(product.getPrice() - product.getPriceDiscount());
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

                int curBadge = orderAddedProducts.getBadgeValue();
                orderAddedProducts.setBadgeValue(curBadge + 1);
                addChanges();
            }
        }
    }

    private void showDialogProductInfo(AssortmentDescription description, Assortment clicked){

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_info_add_count, null);

        Dialog productInfoDialog = new Dialog(context,R.style.CustomDialog);
        productInfoDialog.setContentView(dialogView);

        TextView productName = dialogView.findViewById(R.id.textDialogInfoProductName);
        TextView productDiscount = dialogView.findViewById(R.id.textDialogInfoProductDiscount);
        TextView productCode = dialogView.findViewById(R.id.textDialogInfoProductCode);
        TextView productBarCode = dialogView.findViewById(R.id.textDialogInfoProductBarcode);
        TextView productPrice = dialogView.findViewById(R.id.textDialogInfoProductPrice);
        TextView productRemain = dialogView.findViewById(R.id.textDialogInfoProductRemain);
        WebView productDescription = dialogView.findViewById(R.id.webDialogInfoProductDescription);
        Button addToCart = dialogView.findViewById(R.id.addItemToCartDialogProduct);
        EditText textCount = dialogView.findViewById(R.id.editTextCountDialogProduct);
        ImageView addCount = dialogView.findViewById(R.id.imageAddCountDialogProduct);
        ImageView deleteCount = dialogView.findViewById(R.id.imageDeleteCountDialogProduct);
        ImageView imageProduct = dialogView.findViewById(R.id.imageDialogInfoProduct);
        ImageButton getInfo = dialogView.findViewById(R.id.imageButtonRemoteInfo);

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
        productBarCode.setText(clicked.getBarCode());

        Bitmap bmpImage = BitmapFactory.decodeByteArray(image, 0, image.length);
        imageProduct.setImageBitmap(Bitmap.createScaledBitmap(bmpImage, 211, 211, false));

        productDiscount.setVisibility(View.GONE);
        productPrice.setText("MDL " + clicked.getPrice() + " / " + clicked.getUnitName());
        productCode.setText("#" + clicked.getCode());

        if (clicked.getPriceDiscount() > 0){
            productDiscount.setVisibility(View.GONE);
            productPrice.setText("MDL " + clicked.getPrice());
        }

        getInfo.setOnClickListener(v -> {
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

                    if(description != null && description.getErrorCode() == 0){
                        String content = "";
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.product_image);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        byte[] image = stream.toByteArray();

                        if(description.getImage1() != null)
                            image = description.getImage1();
                        else if(description.getImage2() != null)
                            image = description.getImage2();
                        else if(description.getImage3() != null)
                            image = description.getImage3();
                        else if(description.getImage4() != null)
                            image = description.getImage4();

                        content = description.getTranslatedDescription().getRO();

                        productDescription.loadDataWithBaseURL(null, content,"text/html","UTF-8", null);

                        Bitmap bmpImage = BitmapFactory.decodeByteArray(image, 0, image.length);
                        imageProduct.setImageBitmap(Bitmap.createScaledBitmap(bmpImage, 211, 211, false));
                    }
                    else{
                        Toast.makeText(AddProductsFromOrder.this, "No other info!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AssortmentDescription> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductsFromOrder.this, "No other info!", Toast.LENGTH_SHORT).show();
                }
            });
        });

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
            addProductToOrder(clicked,countDouble);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(textCount.getWindowToken(), 0);
            }
            productInfoDialog.dismiss();
        });

        productInfoDialog.show();
        productInfoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(textCount.getWindowToken(), 0);
                }
            }
        });

//        int displayWidth = displayMetrics.widthPixels;
//        int displayHeight = displayMetrics.heightPixels;
//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//        layoutParams.copyFrom(productInfoDialog.getWindow().getAttributes());
//        int dialogWindowWidth = (int) (displayWidth * 0.45f);
//        int dialogWindowHeight = (int) (displayHeight * 0.8f);
//        layoutParams.width = dialogWindowWidth;
//        layoutParams.height = dialogWindowHeight;  //LinearLayout.LayoutParams.WRAP_CONTENT
//        productInfoDialog.getWindow().setAttributes(layoutParams);
    }

    private void searchText(String newText) {
        RealmResults<Assortment> result;
        if(!clickedId.equals("00000000-0000-0000-0000-000000000000")) {
            result = mRealm.where(Assortment.class)
                    .equalTo("parentUid", clickedId).and()
                    .contains("name", newText, Case.INSENSITIVE).or()
                    .contains("code", newText, Case.INSENSITIVE).or()
                    .contains("barCode", newText, Case.INSENSITIVE).or()
                    .sort("name", Sort.ASCENDING)
                    .findAllAsync();

        }
        else{
            result = mRealm.where(Assortment.class)
                    .contains("name", newText, Case.INSENSITIVE).or()
                    .contains("code", newText, Case.INSENSITIVE).or()
                    .contains("barCode", newText, Case.INSENSITIVE).or()
                    .sort("name", Sort.ASCENDING)
                    .findAllAsync();

        }
        findDiscountToAssortment(result);
    }

    private void startTimerSearchText(final String newText) {
        timerTaskSearchText = new TimerTask() {
            @Override
            public void run() {
                AddProductsFromOrder.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (newText.length() >= 3) {
                            searchText(newText);
                        }
//                        else if (newText.equals("")) {
//                            showProducts("00000000-0000-0000-0000-000000000000");
//                        }
                    }
                });
            }
        };
    }


    private RealmChangeListener<RealmResults<Assortment>> realmChangeListener = new RealmChangeListener<RealmResults<Assortment>>() {
        @Override
        public void onChange(RealmResults<Assortment> realmResults) {
            adapterProductsList.setData(realmResults);
        }
    };

    private void showProducts(String parentId) {
        RealmResults<Assortment> result = null;
        mRealm.beginTransaction();
        result = mRealm.where(Assortment.class).equalTo("parentUid", parentId).findAllAsync();
        mRealm.commitTransaction();

        result = result.sort("name", Sort.ASCENDING);
        result = result.sort("isFolder", Sort.DESCENDING);

        findDiscountToAssortment(result);
    }

    private void findDiscountToAssortment(RealmResults<Assortment> listProducts){
        PriceList priceList = null;
        mRealm.beginTransaction();
        priceList = mRealm.where(PriceList.class).equalTo("priceListUid", clientPriceUid).findFirst();
        mRealm.commitTransaction();

        List<Assortment> lisOfAssortment = mRealm.copyFromRealm(listProducts);

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

        adapterProductsList = new AdapterProductsListToOrder(this, lisOfAssortment,false);
        gridView.setAdapter(adapterProductsList);
    }

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Add products");
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
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