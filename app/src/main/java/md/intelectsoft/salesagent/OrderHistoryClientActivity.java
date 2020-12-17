package md.intelectsoft.salesagent;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import md.intelectsoft.salesagent.Adapters.AdapterLinesRequestHistory;
import md.intelectsoft.salesagent.RealmUtils.Request;

@SuppressLint("NonConstantResourceId")
public class OrderHistoryClientActivity extends AppCompatActivity {
    @BindView(R.id.orderListClientOrderDetailHistory) TextView textActivityName;
    @BindView(R.id.textOrderHistoryState) TextView textRequestState;
    @BindView(R.id.textOrderHistoryComment) TextView textRequestComment;
    @BindView(R.id.textOrderHistoryDeliveryAddress) TextView textDeliveryAddress;
    @BindView(R.id.textOrderHistorySum) TextView textRequestSum;
    @BindView(R.id.rrv_recycler_view_order_history_lines) ListView listLines;

    @OnClick(R.id.textBackToHistoryListOrders) void onBck(){
        finish();
    }

    @OnClick(R.id.orderExitHistoryDetail) void onExit(){
        finish();
    }

    @OnClick(R.id.orderShareRequestHistory) void onShareDocument(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history_client);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        Request requestToView = AgentApplication.getInstance().getRequestToView();

        textActivityName.setText(requestToView.getClientName() + " - " + getString(R.string.order_history_activity_title) + requestToView.getCode());
        textDeliveryAddress.setText(requestToView.getDeliveryAddress());
        textRequestComment.setText(requestToView.getComment());
        String state = "";
        switch (requestToView.getState()){
            case 0 : state = "This order is draft.";
            case 1 : state = "This order is queue.";
            case 2 : state = "This order is in work.";
            case 3 : state = "This order is prepared.";
        }
        textRequestState.setText(state);
        textRequestSum.setText(requestToView.getSum() + " MDL");

        listLines.setAdapter(new AdapterLinesRequestHistory(this, R.layout.item_list_order_history_detail_lines, requestToView.getLines()));
    }
}