package md.intelectsoft.salesagent.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.RequestLine;

import static md.intelectsoft.salesagent.ShopCartActivity.changeCountIntoLine;
import static md.intelectsoft.salesagent.ShopCartActivity.removeLine;


/**
 * Created by Igor on 23.12.2019
 */

public class AdapterLinesRequestShopCart extends RealmBaseAdapter<RequestLine> implements ListAdapter {
    Context context;
    SimpleDateFormat sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
    LayoutInflater inflater;

    private static class ViewHolder {
        TextView nameProduct, codeProduct, countProduct, priceProduct, sumProduct, discountProductDiller , discountFromClient;
        ImageView deleteProduct, addCountProduct, deleteCountProduct;
    }


    public AdapterLinesRequestShopCart(@Nullable OrderedRealmCollection<RequestLine> data, Context context, LayoutInflater inflater) {
        super(data);
        this.context = context;
        sdfChisinau.setTimeZone(tzInChisinau);
        this.inflater = inflater;
    }

    @Override
    public void updateData(@Nullable OrderedRealmCollection<RequestLine> data) {
        super.updateData(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_order_detail_lines, parent, false);
            viewHolder = new ViewHolder();
            //find views for id

            viewHolder.nameProduct = convertView.findViewById(R.id.textNameProduct);
            viewHolder.codeProduct = convertView.findViewById(R.id.textCodeProduct);
            viewHolder.countProduct = convertView.findViewById(R.id.textCountProduct);
            viewHolder.priceProduct = convertView.findViewById(R.id.textPriceProduct);
            viewHolder.discountProductDiller = convertView.findViewById(R.id.textDiscountProduct);
            viewHolder.discountFromClient = convertView.findViewById(R.id.textDiscountProductFromDiscountClient);
            viewHolder.sumProduct = convertView.findViewById(R.id.textTotalProduct);
            viewHolder.deleteProduct = convertView.findViewById(R.id.imageDeleteProduct);
            viewHolder.addCountProduct = convertView.findViewById(R.id.imageAddCountProduct);
            viewHolder.deleteCountProduct = convertView.findViewById(R.id.imageDeleteCountProduct);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final RequestLine item = adapterData.get(position);

            viewHolder.nameProduct.setText(item.getAssortimentName());
            viewHolder.codeProduct.setText("#" + item.getAssortimentCode());
            viewHolder.countProduct.setText(String.format("%.2f", item.getCount()).replace(",","."));
            viewHolder.priceProduct.setText(String.format("%.2f", item.getPrice()).replace(",","."));
            viewHolder.sumProduct.setText(String.format("%.2f", item.getSum()).replace(",","."));
            if(item.getPriceDialler() > 0)
                viewHolder.discountProductDiller.setText(String.format("%.2f", item.getPriceDialler()).replace(",","."));
            else
                viewHolder.discountProductDiller.setVisibility(View.GONE);

            if(item.getPriceDiscount() > 0)
                viewHolder.discountFromClient.setText(String.format("%.2f", (item.getPrice() - item.getPriceDiscount()) * item.getCount()).replace(",","."));
            else
                viewHolder.discountFromClient.setText("0.0");

            viewHolder.addCountProduct.setOnClickListener(v -> {
                changeCountIntoLine(item.getAssortimentUid(), 1,false);
            });
            viewHolder.deleteCountProduct.setOnClickListener(v -> {
                changeCountIntoLine(item.getAssortimentUid(), -1,false);
            });
            viewHolder.deleteProduct.setOnClickListener(v -> {
                removeLine(item.getAssortimentUid());
            });

            viewHolder.countProduct.setOnClickListener(v -> {
                View dialogView = inflater.inflate(R.layout.dialog_change_count, null);

                Dialog outletsDialog = new Dialog(context,R.style.CustomDialog);
                outletsDialog.setContentView(dialogView);

                TextView name = dialogView.findViewById(R.id.textDialogChangeCountName);
                TextView currCount = dialogView.findViewById(R.id.dialogChangeCountCurrent);
                TextView newCount = dialogView.findViewById(R.id.dialogChangeCountNew);
                TextView unitCount = dialogView.findViewById(R.id.dialogChangeCountUnit);
                TextView total = dialogView.findViewById(R.id.textTotalForCount);
                TextView change = dialogView.findViewById(R.id.textDialogChangeCountDone);
                TextView cancel = dialogView.findViewById(R.id.textDialogChangeCountCancel);

                Button button0 = dialogView.findViewById(R.id.change0);
                Button button1 = dialogView.findViewById(R.id.change1);
                Button button2 = dialogView.findViewById(R.id.change2);
                Button button3 = dialogView.findViewById(R.id.change3);
                Button button4 = dialogView.findViewById(R.id.change4);
                Button button5 = dialogView.findViewById(R.id.change5);
                Button button6 = dialogView.findViewById(R.id.change6);
                Button button7 = dialogView.findViewById(R.id.change7);
                Button button8 = dialogView.findViewById(R.id.change8);
                Button button9 = dialogView.findViewById(R.id.change9);
                Button buttonPoint = dialogView.findViewById(R.id.changePoint);
                Button buttonCE = dialogView.findViewById(R.id.changeCE);

                name.append(item.getAssortimentName());
                currCount.setText(String.format("%.2f", item.getCount()).replace(",",".") + " " + (item.getUnitName() == null ? "" : item.getUnitName()));
                unitCount.setText(item.getUnitName());

                newCount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(s.equals("")){
                            total.setText("0.00 MDL");
                        }
                        else {
                            double counst = 0;
                            try{
                                counst =  Double.parseDouble(newCount.getText().toString());
                            }
                            catch (Exception e){
                                Log.i("TAG", "onTextChanged: " + e.getMessage());
                            }
                            total.setText(String.format("%.2f", counst * item.getPrice()) + " MDL (Pret standart)" );
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                buttonCE.setOnClickListener(v1 -> {
                    newCount.setText("");
                });
                buttonPoint.setOnClickListener(v1 -> {
                    if(!newCount.getText().toString().contains("."))
                        newCount.append(".");
                });
                button0.setOnClickListener(v1 -> {
                    if (!newCount.getText().toString().equals("0"))
                        if (newCount.getText().toString().contains(".")) {
                            String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                            if (test.length() < 2) {
                                newCount.append("0");
                            }
                        }
                        else newCount.append("0");
                });
                button1.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("1");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("1");
                        }
                    }
                    else newCount.append("1");
                });
                button2.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("2");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("2");
                        }
                    }
                    else newCount.append("2");
                });
                button3.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("3");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("3");
                        }
                    }
                    else newCount.append("3");
                });
                button4.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("4");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("4");
                        }
                    }
                    else newCount.append("4");
                });
                button5.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("5");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("5");
                        }
                    }
                    else newCount.append("5");
                });
                button6.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("6");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("6");
                        }
                    }
                    else newCount.append("6");
                });
                button7.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("7");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("7");
                        }
                    }
                    else newCount.append("7");
                });
                button8.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("8");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("8");
                        }
                    }
                    else newCount.append("8");
                });
                button9.setOnClickListener(v1 -> {
                    if (newCount.getText().toString().equals("0")) newCount.setText("9");
                    else if(newCount.getText().toString().contains(".")){
                        String test = newCount.getText().toString().substring(newCount.getText().toString().indexOf("."), newCount.getText().toString().length());
                        if (test.length() < 3){
                            newCount.append("9");
                        }
                    }
                    else newCount.append("9");
                });

                change.setOnClickListener(v1 -> {
                    double count = Double.parseDouble(newCount.getText().toString());
                    if(count > 0){
                        changeCountIntoLine(item.getAssortimentUid(), count,true);
                        outletsDialog.dismiss();
                    }
                    else
                        Toast.makeText(context, "Input cout or cancelled change it!", Toast.LENGTH_SHORT).show();
                });
                cancel.setOnClickListener(v1 -> {
                    outletsDialog.dismiss();
                });

                outletsDialog.show();

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(outletsDialog.getWindow().getAttributes());
                layoutParams.width = 500;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                outletsDialog.getWindow().setAttributes(layoutParams);
            });

        }
        return convertView;
    }

}
