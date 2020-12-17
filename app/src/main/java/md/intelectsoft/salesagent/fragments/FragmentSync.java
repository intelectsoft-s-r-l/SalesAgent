package md.intelectsoft.salesagent.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderRetrofitClient;
import md.intelectsoft.salesagent.OrderServiceUtils.OrderServiceAPI;
import md.intelectsoft.salesagent.OrderServiceUtils.RemoteException;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.AssortmentList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientPriceLists;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.PriceList;
import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.Assortment;
import md.intelectsoft.salesagent.RealmUtils.Client;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static md.intelectsoft.salesagent.AgentApplication.sharedPreferenceSettings;

/**
 * Created by Igor on 02.09.2020
 */

public class FragmentSync extends Fragment {

    ConstraintLayout layoutSync, layoutSyncToStart;
    Switch syncToStart;
    TextView latestSync;
    SharedPreferences sharedPreferencesSettings, sharedPrefFavorite;
    Context context;
    Activity activity;
    ProgressDialog pgH;
    Realm mRealm;
    OrderServiceAPI orderServiceAPI;

    String tokenId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_sync, container, false);

        context = getContext();
        if(context == null)
            context = getActivity();
        activity = getActivity();

        layoutSync = rootViewAdmin.findViewById(R.id.csl_sync_start);
        syncToStart = rootViewAdmin.findViewById(R.id.switch_start_sync);
        latestSync = rootViewAdmin.findViewById(R.id.txt_last_sync);
        layoutSyncToStart = rootViewAdmin.findViewById(R.id.csl_start_sync);
        pgH = new ProgressDialog(context);
        mRealm = Realm.getDefaultInstance();
        orderServiceAPI = OrderRetrofitClient.getApiOrderService("");

        sharedPreferencesSettings = context.getSharedPreferences(sharedPreferenceSettings, MODE_PRIVATE);
        sharedPrefFavorite = context.getSharedPreferences("AssortmentFavorite",MODE_PRIVATE);

        tokenId = sharedPreferencesSettings.getString("token","");

        String latest = sharedPreferencesSettings.getString("lastSync","");
        boolean syncToS = sharedPreferencesSettings.getBoolean("syncToStart",false);
        syncToStart.setChecked(syncToS);

        if(latest.equals(""))
            latestSync.setText("Syncronization is not do");
        else
            latestSync.setText("Ultima sincronizare: " + latest);

        layoutSync.setOnClickListener(view -> {
            pgH.setMessage("Download assortment...");
            pgH.setCancelable(false);
            pgH.setIndeterminate(true);
            pgH.show();

            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.delete(Assortment.class);
                    realm.delete(Client.class);
                }
            });

            getAssortmentList();
        });

        layoutSyncToStart.setOnClickListener(view -> {
            boolean syncToStr = sharedPreferencesSettings.getBoolean("syncToStart",false);
            syncToStart.setChecked(!syncToStr);
        });

        syncToStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPreferencesSettings.edit().putBoolean("syncToStart",b).apply();
            }
        });
        return rootViewAdmin;
    }

    private void getAssortmentList (){
        Call<AssortmentList> call = orderServiceAPI.getAssortmentList(tokenId);

        call.enqueue(new Callback<AssortmentList>() {
            @Override
            public void onResponse(Call<AssortmentList> call, Response<AssortmentList> response) {
                final AssortmentList assortmentList = response.body();
                if(assortmentList != null){
                    if(assortmentList.getErrorCode() == 0){
                        if(assortmentList.getAssortment() != null){

                            mRealm.executeTransaction(realm -> {
                                realm.insert(assortmentList.getAssortment());

                                Map<String, ?> allPreferences = sharedPrefFavorite.getAll();

                                for(Map.Entry<String, ?> entry : allPreferences.entrySet()){
                                    Assortment item = realm.where(Assortment.class).equalTo("uid",entry.getKey()).findFirst();
                                    if(item != null){
                                        item.setFavoritOrder((Long) entry.getValue());
                                        item.setFavorit(true);
                                    }
                                    else{
                                        sharedPrefFavorite.edit().remove(entry.getKey()).apply();
                                    }
                                }
                            });
                            //load price lists
                            getPriceLists();
                            getClientsList();
                        }

                        else{
                            pgH.dismiss();
                            latestSync.setTextColor(Color.rgb(219,45,45));//red
                            latestSync.setText("List of assortment is empty");
                        }
                    }
                    else{
                        if(!activity.isDestroyed()){
                            pgH.dismiss();
                            String msg = RemoteException.getServiceException(assortmentList.getErrorCode());
                            latestSync.setTextColor(Color.rgb(219,45,45));//red
                            latestSync.setText("Error download assortment: " + msg);
                        }
                    }
                }
                else{
                    pgH.dismiss();
                    latestSync.setTextColor(Color.rgb(219,45,45));//red
                    latestSync.setText("Response from server is empty");
                }
            }

            @Override
            public void onFailure(Call<AssortmentList> call, Throwable t) {
                pgH.dismiss();
                latestSync.setTextColor(Color.rgb(219,45,45));//red
                latestSync.setText(t.getMessage());
            }
        });
    }

    private void getClientsList (){
        pgH.setMessage("Download contragents...");
        Call<ClientList> call = orderServiceAPI.getClients(tokenId);

        call.enqueue(new Callback<ClientList>() {
            @Override
            public void onResponse(Call<ClientList> call, Response<ClientList> response) {
                final ClientList resultContragents = response.body();
                if(resultContragents != null){
                    if(resultContragents.getErrorCode() == 0){
                        if(resultContragents.getClients() != null){
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.insert(resultContragents.getClients());
                                }
                            });

                            Date datess = new Date();
                            SimpleDateFormat sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                            TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
                            sdfChisinau.setTimeZone(tzInChisinau);
                            latestSync.setText("Ultima sincronizare: " + sdfChisinau.format(datess)); // Convert to String first
                            sharedPreferencesSettings.edit().putString("lastSync", sdfChisinau.format(datess)).apply();
                            pgH.dismiss();
                        }
                        else{
                            pgH.dismiss();
                            latestSync.setTextColor(Color.rgb(219,45,45));//red
                            latestSync.setText("List of clients is empty");
                        }
                    }
                    else{
                        pgH.dismiss();
                        String msg = RemoteException.getServiceException(resultContragents.getErrorCode());
                        latestSync.setTextColor(Color.rgb(219,45,45));//red
                        latestSync.setText("Error download contragents: " + msg);
                    }
                }
                else{
                    pgH.dismiss();
                    latestSync.setTextColor(Color.rgb(219,45,45));//red
                    latestSync.setText("Response from server is empty");
                }
            }

            @Override
            public void onFailure(Call<ClientList> call, Throwable t) {
                pgH.dismiss();
                latestSync.setTextColor(Color.rgb(219,45,45));//red
                latestSync.setText(t.getMessage());
            }
        });
    }

    private void getPriceLists() {
        Call<ClientPriceLists> clientPriceListsCall = orderServiceAPI.getClientPriceLists(tokenId);

        clientPriceListsCall.enqueue(new Callback<ClientPriceLists>() {
            @Override
            public void onResponse(Call<ClientPriceLists> call, Response<ClientPriceLists> response) {
                ClientPriceLists clientPriceLists = response.body();
                if(clientPriceLists != null){
                    if(clientPriceLists.getErrorCode() == 0){
                        RealmList<PriceList> priceLists = clientPriceLists.getPriceLists();

                        if(priceLists != null && priceLists.size() > 0){
                            mRealm.executeTransaction(realm -> {
                                realm.insert(priceLists);
                            });
                        }
                        else{
                            pgH.dismiss();
                            Toast.makeText(context, "List on the prices is empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        pgH.dismiss();
                        Toast.makeText(context, "Error load orders! Code & Message: " + clientPriceLists.getErrorCode() + " & " + clientPriceLists.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    pgH.dismiss();
                    Toast.makeText(context, "Null response price lists!", Toast.LENGTH_SHORT).show();
                }

                //load client list
                getClientsList();
            }

            @Override
            public void onFailure(Call<ClientPriceLists> call, Throwable t) {

            }
        });
    }

}
