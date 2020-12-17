package md.intelectsoft.salesagent.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Base64;
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
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.NewsList;
import md.intelectsoft.salesagent.R;


/**
 * Created by Igor on 23.12.2019
 */

public class AdapterListNews extends RealmBaseAdapter<NewsList> implements ListAdapter {
    Context context;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");

    private static class ViewHolder {
        TextView header, date, content;
        ImageView photo;
    }


    public AdapterListNews(@Nullable OrderedRealmCollection<NewsList> data, Context context) {
        super(data);
        this.context = context;
        simpleDateFormat.setTimeZone(tzInChisinau);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_news_list, parent, false);
            viewHolder = new ViewHolder();
            //find views for id
            viewHolder.header = convertView.findViewById(R.id.textHeaderNews);
            viewHolder.photo = convertView.findViewById(R.id.imageNewsPhoto);
            viewHolder.date = convertView.findViewById(R.id.textDateNews);
            viewHolder.content = convertView.findViewById(R.id.textContentNews);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final NewsList item = adapterData.get(position);

            viewHolder.header.setText(Html.fromHtml(item.getHeader()));
            viewHolder.date.setText(simpleDateFormat.format(item.getDateLong()));

            viewHolder.content.setText(Html.fromHtml(item.getContent()));

            byte[] decodedString = Base64.decode(item.getPhoto(), Base64.DEFAULT);
            Bitmap photoNews = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            viewHolder.photo.setImageBitmap(photoNews);

        }
        return convertView;
    }
}
