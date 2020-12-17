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
import md.intelectsoft.salesagent.RealmUtils.Outlets;

/**
 * Created by Igor on 10.02.2020
 */

public class AdapterOutletListGrid extends ArrayAdapter<Outlets> {
    Context context;

    public AdapterOutletListGrid(@NonNull Context context, int resource, @NonNull List<Outlets> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    private static class ViewHolder {
        TextView address, comment;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            convertView = inflater.inflate(R.layout.item_list_outlets,parent,false);

            viewHolder.address = convertView.findViewById(R.id.textOutletAddress);
            viewHolder.comment = convertView.findViewById(R.id.textOutletComment);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Outlets item = getItem(position);
        if (item != null) {
            viewHolder.address.setText(String.valueOf(item.getAddress()));
            viewHolder.comment.setText(item.getComment());
        }

        return convertView;
    }
}
