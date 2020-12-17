package md.intelectsoft.salesagent.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.Request;

public class AdapterClientHistoryOrder extends RecyclerView.Adapter<AdapterClientHistoryOrder.OrderViewHolder>{
    List<Request> requestList = new ArrayList<>();
    SimpleDateFormat sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");

    public AdapterClientHistoryOrder(List<Request> requestList) {
        this.requestList = requestList;
        sdfChisinau.setTimeZone(tzInChisinau);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_client_history_orders, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterClientHistoryOrder.OrderViewHolder holder, int position) {
        Request request = requestList.get(position);

        if(request.getCode() == null || request.getCode().equals(""))
            holder.code.setText("-");
        else
            holder.code.setText(request.getCode());
        holder.sum.setText(request.getSum() + " MDL");
        holder.date.setText(sdfChisinau.format(request.getDateToLong()));
        holder.details.setText(request.getDeliveryAddress());

        if(request.getState() != null){
            int currentState = request.getState();
            switch (currentState) {
                case 0:
                    holder.state.setText("Draft");
                    break;
                case 1:
                    holder.state.setText("In queue");
                    break;
                case 2:
                    holder.state.setText("In work");
                    break;
                case 3:
                    holder.state.setText("Prepared");
                    break;
                case 4:
                    holder.state.setText("Anulat de beneficiar");
                    break;
                case 5:
                    holder.state.setText("Anulat de furnizor");
                    break;
                case 6:
                    holder.state.setText("Final");
                    break;
                default:
                    holder.state.setText("Unknown");
            }
        }
        else{
            holder.state.setText("Unknown");
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView code;
        private TextView date;
        private TextView state;
        private TextView sum;
        private TextView details;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.textCodeHistoryOrder);
            date = itemView.findViewById(R.id.textDateHistoryOrder);
            state = itemView.findViewById(R.id.textStateHistoryOrder);
            sum = itemView.findViewById(R.id.textSumHistoryOrder);
            details = itemView.findViewById(R.id.textDetailHistoryOrder);
        }
    }
}
