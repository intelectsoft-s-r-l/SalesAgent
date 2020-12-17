package md.intelectsoft.salesagent.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.RequestLine;


/**
 * Created by Igor on 23.12.2019
 */

public class AdapterLinesItemKind extends RealmBaseAdapter<RequestLine> implements ListAdapter {

    private static class ViewHolder {
        TextView nameProduct, codeProduct, countProduct, priceProduct, sumProduct, discountProduct;
    }


    public AdapterLinesItemKind(@Nullable OrderedRealmCollection<RequestLine> data) {
        super(data);
    }

    @Override
    public void updateData(@Nullable OrderedRealmCollection<RequestLine> data) {
        super.updateData(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_order_daily_report, parent, false);
            viewHolder = new ViewHolder();
            //find views for id

            viewHolder.nameProduct = convertView.findViewById(R.id.textNameProduct);
            viewHolder.codeProduct = convertView.findViewById(R.id.textCodeProduct);
            viewHolder.countProduct = convertView.findViewById(R.id.textCountProduct);
            viewHolder.priceProduct = convertView.findViewById(R.id.textPriceProduct);
            viewHolder.discountProduct = convertView.findViewById(R.id.textDiscountProduct);
            viewHolder.sumProduct = convertView.findViewById(R.id.textTotalProduct);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final RequestLine item = adapterData.get(position);

            viewHolder.nameProduct.setText(item.getAssortimentName());
            viewHolder.codeProduct.setText("#" + item.getAssortimentCode());
            viewHolder.countProduct.setText(String.format("%.2f", item.getCount()).replace(",","."));
            viewHolder.priceProduct.setText(String.format("%.2f", item.getPrice()).replace(",",".") + " MDL");
            viewHolder.sumProduct.setText(String.format("%.2f", item.getSum()).replace(",",".") + " MDL");
            viewHolder.discountProduct.setText(String.format("%.2f", item.getPriceDiscount()).replace(",",".") + " MDL");
        }
        return convertView;
    }

}
