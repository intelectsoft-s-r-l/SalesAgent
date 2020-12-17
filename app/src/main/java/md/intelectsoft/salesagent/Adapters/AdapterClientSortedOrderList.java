package md.intelectsoft.salesagent.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.Request;

public class AdapterClientSortedOrderList extends BaseAdapter {
    List<Request> requestList = new ArrayList<>();
    SimpleDateFormat sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");

    public AdapterClientSortedOrderList(List<Request> requestList) {
        this.requestList = requestList;
        sdfChisinau.setTimeZone(tzInChisinau);
    }

    public class OrderViewHolder {
        TextView clientRequest, codeRequest, dateRequest, sumRequest, stateRequest, commentRequest, discountSumRequest;
        ImageView imageStateSync;
    }

    @Override
    public int getCount() {
        return requestList.size();
    }

    @Override
    public Request getItem(int position) {
        return requestList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrderViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_orders_list, parent, false);

            viewHolder = new OrderViewHolder();

            viewHolder.clientRequest = convertView.findViewById(R.id.textClientName);
            viewHolder.codeRequest = convertView.findViewById(R.id.textOrderItemCode);
            viewHolder.dateRequest = convertView.findViewById(R.id.textOrderItemDate);
            viewHolder.sumRequest = convertView.findViewById(R.id.textOrderItemSum);
            viewHolder.stateRequest = convertView.findViewById(R.id.textOrderItemState);
            viewHolder.commentRequest = convertView.findViewById(R.id.textPointOfSalesInformation);
            viewHolder.imageStateSync = convertView.findViewById(R.id.imageStateSyncOrderList);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (OrderViewHolder) convertView.getTag();
        }

        Request item = requestList.get(position);

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
        viewHolder.imageStateSync.setVisibility(View.GONE);

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

        return convertView;
    }
}
