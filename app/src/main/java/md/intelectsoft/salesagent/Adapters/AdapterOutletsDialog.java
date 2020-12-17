package md.intelectsoft.salesagent.Adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.Outlets;


/**
 * Created by Igor on 23.12.2019
 */

public class AdapterOutletsDialog extends RealmBaseAdapter<Outlets> implements ListAdapter {
    Context context;

    private static class ViewHolder {
        TextView address, comment;
    }


    public AdapterOutletsDialog(@Nullable OrderedRealmCollection<Outlets> data, Context context) {
        super(data);
        this.context = context;
    }

    @Override
    public void updateData(@Nullable OrderedRealmCollection<Outlets> data) {
        super.updateData(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_outlets_list, parent, false);
            viewHolder = new ViewHolder();
            //find views for id
            viewHolder.address = convertView.findViewById(R.id.textItemOutletAddress);
            viewHolder.comment = convertView.findViewById(R.id.textItemOutletComment);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final Outlets item = adapterData.get(position);

            viewHolder.address.setText(item.getAddress());
            viewHolder.comment.setText(item.getComment());
        }
        return convertView;
    }

}
