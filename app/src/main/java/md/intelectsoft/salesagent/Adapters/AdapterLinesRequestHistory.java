package md.intelectsoft.salesagent.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.RequestLine;

/**
 * Created by Igor on 10.02.2020
 */

public class AdapterLinesRequestHistory extends ArrayAdapter<RequestLine> {
    Context context;

    public AdapterLinesRequestHistory(@NonNull Context context, int resource, @NonNull List<RequestLine> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    private static class ViewHolder {
        TextView nameProduct, codeProduct, countProduct, priceProduct, sumProduct;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

       ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_order_history_detail_lines, parent, false);
            viewHolder = new ViewHolder();
            //find views for id

            viewHolder.nameProduct = convertView.findViewById(R.id.textNameProduct);
            viewHolder.codeProduct = convertView.findViewById(R.id.textCodeProduct);
            viewHolder.countProduct = convertView.findViewById(R.id.textCountProduct);
            viewHolder.priceProduct = convertView.findViewById(R.id.textPriceProduct);
            viewHolder.sumProduct = convertView.findViewById(R.id.textTotalProduct);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        final RequestLine item = getItem(position);

        viewHolder.nameProduct.setText(item.getAssortimentName());
        viewHolder.codeProduct.setText("#" + item.getAssortimentCode());
        viewHolder.countProduct.setText(String.format("%.2f", item.getCount()).replace(",","."));
        viewHolder.priceProduct.setText(String.format("%.2f", item.getPrice()).replace(",",".") + " MDL");
        viewHolder.sumProduct.setText(String.format("%.2f", item.getSum()).replace(",",".") + " MDL");

        return convertView;
    }
}
