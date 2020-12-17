package md.intelectsoft.salesagent;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import io.realm.Sort;
import md.intelectsoft.salesagent.RealmUtils.Request;

public class ItemKindListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private boolean mTwoPane;
    SimpleDateFormat dfDate_day = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Realm mRealm;

    TextView btnBack, textActivityName, textSumAllOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemkind_list);

        mRealm = Realm.getDefaultInstance();

        View recyclerView = findViewById(R.id.itemkind_list);
        btnBack = findViewById(R.id.textBackToMainFromDailyReport);
        textActivityName = findViewById(R.id.mainAppNameDailyReport);
        textSumAllOrders = findViewById(R.id.textSumAllOrderDailyReport);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        setNameTextColor();

        Calendar calendarFrom = Calendar.getInstance();
        int year = calendarFrom.get(Calendar.YEAR);
        int month = calendarFrom.get(Calendar.MONTH) + 1;
        int date = calendarFrom.get(Calendar.DAY_OF_MONTH);

        long startDay = parseStringToDate(year + "-" + month + "-" + date + " 00:01:00");
        long endDay = parseStringToDate(year + "-" + month + "-" + date + " 23:59:00");


        RealmResults<Request> requestCreatedTodayList = mRealm.where(Request.class).between("dateToLong", startDay, endDay).sort("dateToLong", Sort.DESCENDING).findAll();
        if (!requestCreatedTodayList.isEmpty()) {
            assert recyclerView != null;
            double sum = 0;
            for (Request request : requestCreatedTodayList) {
                if(request.getSum() != null)
                    sum += request.getSum();
            }
            textSumAllOrders.setText(sum + " MDL");
            setupRecyclerView((ListView) recyclerView, requestCreatedTodayList);
        }
        else
            textSumAllOrders.setText("0.0 MDL");
    }

    private long parseStringToDate(String dt) {
        Date date = new Date();
        try {
            date = dfDate_day.parse(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    private void setupRecyclerView(@NonNull ListView recyclerView, RealmResults<Request> content) {
        SimpleItemRecyclerViewAdapter adapter = new SimpleItemRecyclerViewAdapter(this, content);
        recyclerView.setAdapter(adapter);

        recyclerView.setOnItemClickListener((parent, view, position, id) -> {
            view.setSelected(true);
            ItemKindDetailFragment fragment = new ItemKindDetailFragment(adapter.getItem(position).getLines());
            this.getSupportFragmentManager().beginTransaction().replace(R.id.itemkind_detail_container, fragment).commit();

        });
    }

    private void setNameTextColor(){
        SpannableString s = new SpannableString("Sales Agent - Daily report");
        s.setSpan(new ForegroundColorSpan(getColor(R.color.orange)), 0, 1, 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 1, s.length(), 0);

        textActivityName.setText(s);
    }

    public static class SimpleItemRecyclerViewAdapter extends RealmBaseAdapter<Request> implements ListAdapter {

        private ItemKindListActivity mParentActivity;
//        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                DummyContent.DummyItem item = (DummyContent.DummyItem) view.getTag();

//            }
//        };

        private static class ViewHolder {
            TextView clientRequest, sumRequest, stateRequest, commentRequest;
        }

        public SimpleItemRecyclerViewAdapter(ItemKindListActivity activity, @Nullable OrderedRealmCollection<Request> data) {
            super(data);
            this.mParentActivity = activity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemkind_list_content, parent, false);
                viewHolder = new ViewHolder();
                //find views for id
                viewHolder.clientRequest = convertView.findViewById(R.id.textClientName);
                viewHolder.sumRequest = convertView.findViewById(R.id.textOrderItemSum);
                viewHolder.stateRequest = convertView.findViewById(R.id.textOrderItemState);
                viewHolder.commentRequest = convertView.findViewById(R.id.textPointOfSalesInformation);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (adapterData != null) {
                final Request item = adapterData.get(position);

                viewHolder.clientRequest.setText(item.getClientName());
                String comments = item.getComment();

                if (comments == null || comments.equals("FROM TERMINAL"))
                    viewHolder.commentRequest.setText("");
                else
                    viewHolder.commentRequest.setText(item.getComment());

//                viewHolder.dateRequest.setText(sdfChisinau.format(item.getDateToLong()));

                double sum = 0;
                if (item.getSum() != null)
                    sum = item.getSum();

                viewHolder.sumRequest.setText(String.format("%.2f", sum).replace(",", ".") + " MDL");

//                if (item.getSyncState() == BaseEnum.Syncronizat)
//                    viewHolder.imageStateSync.setColorFilter(ContextCompat.getColor(context, R.color.teal_200), android.graphics.PorterDuff.Mode.MULTIPLY);
//                else
//                    viewHolder.imageStateSync.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.MULTIPLY);

                if (item.getState() != null) {
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
                } else {
                    viewHolder.stateRequest.setText("Unknown");
                }
            }
            return convertView;
        }
    }
}