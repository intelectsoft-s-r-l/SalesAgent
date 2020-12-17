package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.salesagent.Adapters.AdapterProductsList;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentDescription.AssortmentDescription;
import md.intelectsoft.salesagent.RealmUtils.Assortment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

@SuppressLint("NonConstantResourceId")
public class ProductsActivity extends AppCompatActivity {
    @BindView(R.id.productsListAppName) TextView headerActivityName;
    @BindView(R.id.gridProducts) GridView gridView;
    @BindView(R.id.layoutDOM) LinearLayout layoutDOM;
    @BindView(R.id.imageGoHomeList) ImageView goHomeList;
    @BindView(R.id.searchProducts) SearchView searchProducts;

    AdapterProductsList adapterProductsList;
    SharedPreferences sharedPreferencesSettings;
    ProgressDialog progressDialog;
    TimerTask timerTaskSearchText;
    DisplayMetrics displayMetrics;
    OrderServiceAPI orderServiceAPI;
    String token;
    Timer timerSearch;
    Context context;
    Realm mRealm;

    @OnClick(R.id.textBackMainFromProducts) void onBckClick() {
        int childCount = layoutDOM.getChildCount();
        if(childCount == 1){
            finish();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        setNameTextColor();
        context = this;
        mRealm = Realm.getDefaultInstance();
        sharedPreferencesSettings = getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        token = sharedPreferencesSettings.getString("token","");
        progressDialog = new ProgressDialog(context);
        orderServiceAPI = OrderRetrofitClient.getApiOrderService(uri);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        showProducts("00000000-0000-0000-0000-000000000000");

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

                progressDialog.setMessage(getString(R.string.obtain_info_about_product_dialog_msg));
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel_dialog),(dialog, which) -> {
                   call.cancel();
                   if(call.isCanceled())
                       dialog.dismiss();
                });
                progressDialog.show();

                call.enqueue(new Callback<AssortmentDescription>() {
                    @Override
                    public void onResponse(Call<AssortmentDescription> call, Response<AssortmentDescription> response) {
                        AssortmentDescription description = response.body();
                        showDialogProductInfo(description,clicked);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<AssortmentDescription> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(ProductsActivity.this, "Load information failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void showDialogProductInfo( AssortmentDescription description, Assortment clicked){

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_info, null);

        Dialog productInfoDialog = new Dialog(context,R.style.CustomDialog);
        productInfoDialog.setContentView(dialogView);

        TextView productName = dialogView.findViewById(R.id.textDialogInfoProductName);
        TextView productDiscount = dialogView.findViewById(R.id.textDialogInfoProductDiscount);
        TextView productCode = dialogView.findViewById(R.id.textDialogInfoProductCode);
        TextView productPrice = dialogView.findViewById(R.id.textDialogInfoProductPrice);
        TextView productRemain = dialogView.findViewById(R.id.textDialogInfoProductRemain);
        TextView productBarcode = dialogView.findViewById(R.id.textDialogInfoProductBarcode);
        WebView productDescription = dialogView.findViewById(R.id.webDialogInfoProductDescription);
        Button closeDialog = dialogView.findViewById(R.id.closeDialogInfoProduct);
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
        productBarcode.setText(clicked.getBarCode());
        productRemain.setText(clicked.getRemain() + " " + clicked.getUnitName());
        productDescription.loadDataWithBaseURL(null, content,"text/html","UTF-8", null);

        Bitmap bmpImage = BitmapFactory.decodeByteArray(image, 0, image.length);
        imageProduct.setImageBitmap(Bitmap.createScaledBitmap(bmpImage, 211, 211, false));

        productDiscount.setVisibility(View.GONE);
        productPrice.setText(clicked.getPrice() + " MDL" +  " / " + clicked.getUnitName());
        productCode.setText("#" + clicked.getCode());

        closeDialog.setOnClickListener(v -> productInfoDialog.dismiss());

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
        adapterProductsList = new AdapterProductsList(this, result,true);
        gridView.setAdapter(adapterProductsList);

        result.addChangeListener(realmChangeListener);
    }

    private void startTimerSearchText(final String newText) {
        timerTaskSearchText = new TimerTask() {
            @Override
            public void run() {
                ProductsActivity.this.runOnUiThread(new Runnable() {
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
        mRealm.beginTransaction();
        result = mRealm.where(Assortment.class).equalTo("parentUid", parentId).findAllAsync();
        mRealm.commitTransaction();

        result = result.sort("name", Sort.ASCENDING);
        result = result.sort("isFolder", Sort.DESCENDING);

        adapterProductsList = new AdapterProductsList(this, result,true);
        gridView.setAdapter(adapterProductsList);

        result.addChangeListener(realmChangeListener);

//        productsRecyclerViewAdapter = new ProductsRecyclerViewAdapter(this, result,false, false);
//        realmRecyclerView.setAdapter(productsRecyclerViewAdapter);
    }

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + getString(R.string.products_header_activity));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
    }
}