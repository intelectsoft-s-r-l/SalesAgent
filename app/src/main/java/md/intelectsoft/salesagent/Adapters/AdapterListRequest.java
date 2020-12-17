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
import md.intelectsoft.salesagent.AppUtils.BaseEnum;
import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.Request;


/**
 * Created by Igor on 23.12.2019
 */

public class AdapterListRequest extends RealmBaseAdapter<Request> implements ListAdapter {
    Context context;
    SimpleDateFormat sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");

    private static class ViewHolder {
        TextView clientRequest, codeRequest, dateRequest, sumRequest, stateRequest, commentRequest, discountSumRequest;
        ImageView imageStateSync;
    }


    public AdapterListRequest(@Nullable OrderedRealmCollection<Request> data, Context context) {
        super(data);
        this.context = context;
        sdfChisinau.setTimeZone(tzInChisinau);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_orders_list, parent, false);
            viewHolder = new ViewHolder();
            //find views for id
            viewHolder.clientRequest = convertView.findViewById(R.id.textClientName);
            viewHolder.codeRequest = convertView.findViewById(R.id.textOrderItemCode);
            viewHolder.dateRequest = convertView.findViewById(R.id.textOrderItemDate);
            viewHolder.sumRequest = convertView.findViewById(R.id.textOrderItemSum);
            viewHolder.stateRequest = convertView.findViewById(R.id.textOrderItemState);
            viewHolder.commentRequest = convertView.findViewById(R.id.textPointOfSalesInformation);
            viewHolder.imageStateSync = convertView.findViewById(R.id.imageStateSyncOrderList);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final Request item = adapterData.get(position);

            viewHolder.clientRequest.setText(item.getClientName());
            viewHolder.codeRequest.setText(item.getCode());
            String comments = item.getComment();

            if(comments == null || comments.equals("FROM TERMINAL"))
                viewHolder.commentRequest.setText("");
            else
                viewHolder.commentRequest.setText(item.getComment());

            viewHolder.dateRequest.setText(sdfChisinau.format(item.getDateToLong()));

            double sum = 0;
            if(item.getSum() != null)
                sum = item.getSum();

            viewHolder.sumRequest.setText(String.format("%.2f", sum).replace(",", ".") + " MDL");

            if(item.getSyncState() == BaseEnum.Syncronizat)
                viewHolder.imageStateSync.setImageDrawable(context.getDrawable(R.drawable.ic_next_round_green));
            else
                viewHolder.imageStateSync.setImageDrawable(context.getDrawable(R.drawable.ic_next_round_red));

            if(item.getState() != null){
                int currentState = item.getState();
                switch (currentState) {
                    case 0:
                        viewHolder.stateRequest.setText("Draft");
                        break;
                    case 1:
                        viewHolder.stateRequest.setText("In queue");
                        break;
                    case 2:
                        viewHolder.stateRequest.setText("In work");
                        break;
                    case 3:
                        viewHolder.stateRequest.setText("Prepared");
                        break;
                    case 4:
                        viewHolder.stateRequest.setText("Anulat de beneficiar");
                        break;
                    case 5:
                        viewHolder.stateRequest.setText("Anulat de furnizor");
                        break;
                    case 6:
                        viewHolder.stateRequest.setText("Final");
                        break;
                    default:
                        viewHolder.stateRequest.setText("Unknown");
                }
            }
            else{
                viewHolder.stateRequest.setText("Unknown");
            }
        }
        return convertView;
    }
}
