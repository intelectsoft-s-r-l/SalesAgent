package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import md.intelectsoft.salesagent.Adapters.AdapterClientsList;
import md.intelectsoft.salesagent.Adapters.AdapterOutletsDialog;
import md.intelectsoft.salesagent.RealmUtils.Client;
import md.intelectsoft.salesagent.RealmUtils.Outlets;

@SuppressLint("NonConstantResourceId")
public class ClientOrderActivity extends AppCompatActivity {
    @BindView(R.id.clientsListOrderSelect) TextView headerActivityName;
    @BindView(R.id.gridClients) GridView gridView;
    @BindView(R.id.searchClientOrder) SearchView searchClients;

    @OnClick(R.id.textBackMainFromClientOrder) void cancelOrder(){ finish();}

    TimerTask timerTaskSearchText;
    AdapterClientsList adapterClientsList;
    DisplayMetrics displayMetrics;
    String outletAddress;
    Context context;
    Realm mRealm;
    Timer timerSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_order);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        setNameTextColor();
        context = this;
        mRealm = Realm.getDefaultInstance();
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        showClients("",false);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Client selectedClient = adapterClientsList.getItem(position);

                Intent clientDetail = new Intent(context, AssortmentOrderActivity.class);
                clientDetail.putExtra("uid", selectedClient.getUid());
                clientDetail.putExtra("name", selectedClient.getName());
                clientDetail.putExtra("priceUid", selectedClient.getPriceListUid());
                clientDetail.putExtra("newRequest", true);

                RealmList<Outlets> outlets = selectedClient.getOutlets();

                if(outlets.size() > 0){
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_outlets_list, null);

                    Dialog outletsDialog = new Dialog(context,R.style.CustomDialog);
                    outletsDialog.setContentView(dialogView);

                    TextView textCancel = dialogView.findViewById(R.id.textDialogOutletCancel);
                    TextView next = dialogView.findViewById(R.id.textDialogOutletNext);
                    ListView listOutlets = dialogView.findViewById(R.id.listDialogOutlets);
                    TextView titleDialog = dialogView.findViewById(R.id.textDialogOutletClientName);

                    titleDialog.setText(getString(R.string.select_outlet_for_client) + selectedClient.getName());
                    textCancel.setOnClickListener(v -> outletsDialog.dismiss());

                    AdapterOutletsDialog adapterOutletsDialog = new AdapterOutletsDialog(outlets, context);
                    listOutlets.setAdapter(adapterOutletsDialog);

                    listOutlets.setOnItemClickListener((parent1, view1, position1, id1) -> {
                        outletAddress = adapterOutletsDialog.getItem(position1).getAddress();
                        listOutlets.setItemChecked(position1, true);
                    });

                    next.setOnClickListener(v -> {
                        if(outletAddress != null && !outletAddress.equals(""))
                            clientDetail.putExtra("address", outletAddress);
                        outletsDialog.dismiss();
                        startActivity(clientDetail);
                        finish();
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
                else{
                    startActivity(clientDetail);
                    finish();
                }
            }
        });

        searchClients.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        searchClients.setOnCloseListener(() -> {
            showClients("",false);
            return false;
        });
    }

    private void startTimerSearchText(final String newText) {
        timerTaskSearchText = new TimerTask() {
            @Override
            public void run() {
                ClientOrderActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (newText.length() >= 3) {
                            showClients(newText,true);
                        }
//                        else if (newText.equals("")) {
//                            showProducts("00000000-0000-0000-0000-000000000000");
//                        }
                    }
                });
            }
        };
    }


    private RealmChangeListener<RealmResults<Client>> realmChangeListener = new RealmChangeListener<RealmResults<Client>>() {
        @Override
        public void onChange(RealmResults<Client> realmResults) {
            adapterClientsList.setData(realmResults);
        }
    };

    private void showClients(String text, boolean search) {
        RealmResults<Client> result = null;
        mRealm.beginTransaction();
        if(search)
            result = mRealm.where(Client.class)
                    .contains("name",text, Case.INSENSITIVE)
                    .or()
                    .contains("iDNP", text, Case.INSENSITIVE)
                    .and()
                    .notEqualTo("name","")
                    .and()
                    .notEqualTo("name", " ")
                    .and()
                    .isNotNull("name")
                    .and()
                    .not()
                    .contains("name", "bank", Case.INSENSITIVE)
                    .and()
                    .not()
                    .contains("name", "banca", Case.INSENSITIVE)
                    .sort("name", Sort.ASCENDING).findAllAsync();
        else
            result = mRealm.where(Client.class)
                    .notEqualTo("name","")
                    .and()
                    .notEqualTo("name", " ")
                    .and()
                    .isNotNull("name")
                    .and()
                    .not()
                    .contains("name", "bank", Case.INSENSITIVE)
                    .and()
                    .not()
                    .contains("name", "banca", Case.INSENSITIVE)
                    .sort("name", Sort.ASCENDING).findAllAsync();
        mRealm.commitTransaction();

        adapterClientsList = new AdapterClientsList(this, result);
        gridView.setAdapter(adapterClientsList);

        result.addChangeListener(realmChangeListener);

//        productsRecyclerViewAdapter = new ProductsRecyclerViewAdapter(this, result,false, false);
//        realmRecyclerView.setAdapter(productsRecyclerViewAdapter);
    }


    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - " + getString(R.string.select_client_tittle_activity));
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        headerActivityName.setText(s);
    }
}