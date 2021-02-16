package md.intelectsoft.salesagent.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

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

    private static class ViewHolder {
        TextView nameProduct, codeProduct, countProduct, priceProduct, sumProduct, discountProductDiller , discountFromClient;
        ImageView deleteProduct, addCountProduct, deleteCountProduct;
    }


    public AdapterLinesRequestShopCart(@Nullable OrderedRealmCollection<RequestLine> data, Context context) {
        super(data);
        this.context = context;
        sdfChisinau.setTimeZone(tzInChisinau);
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
                changeCountIntoLine(item.getAssortimentUid(), 1);
            });
            viewHolder.deleteCountProduct.setOnClickListener(v -> {
                changeCountIntoLine(item.getAssortimentUid(), -1);
            });
            viewHolder.deleteProduct.setOnClickListener(v -> {
                removeLine(item.getAssortimentUid());
            });

        }
        return convertView;
    }

}
