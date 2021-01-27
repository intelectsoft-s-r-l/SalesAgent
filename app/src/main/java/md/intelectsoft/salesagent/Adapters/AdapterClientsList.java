package md.intelectsoft.salesagent.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.Client;

public class AdapterClientsList extends BaseAdapter {

    Context context;
    private List<Client> clientList = Collections.emptyList();

    public AdapterClientsList(Context context, List<Client> assortmentList) {
        this.clientList = assortmentList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return clientList.size();
    }

    @Override
    public Client getItem(int position) {
        return clientList.get(position);
    }

    public void setData(List<Client> details){
        if (details == null) {
            details = Collections.emptyList();
        }
        this.clientList = details;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        private final TextView name;
        private final TextView idnp;
        private final ImageView image;
        private final TextView pointOfSales;
        private final TextView shortName;

        public ViewHolder(View view) {
            name = view.findViewById(R.id.textListClientName);
            pointOfSales = view.findViewById(R.id.textListClientPointOfSales);
            idnp = view.findViewById(R.id.textListClientIDNP);
            image = view.findViewById(R.id.imageClient);
            shortName = view.findViewById(R.id.textViewClientShortName);
        }

        public void bind(Client item) {
            name.setText(item.getName());
            idnp.setText(item.getIDNP());
            pointOfSales.setText(String.valueOf(item.getOutlets().size()));

            if(item.getImage() != null) {
                if(item.getImage().length > 0){
                    shortName.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    Bitmap bmpImage = BitmapFactory.decodeByteArray(item.getImage(), 0, item.getImage().length);
                    image.setImageBitmap(Bitmap.createScaledBitmap(bmpImage, 211, 211, false));
                }
                else{
                    shortName.setVisibility(View.VISIBLE);
                    image.setVisibility(View.INVISIBLE);
                    shortName.setText(item.getName().substring(0,2));
                }
            }
            else{
                shortName.setVisibility(View.VISIBLE);
                image.setVisibility(View.INVISIBLE);
                shortName.setText(item.getName().substring(0,2));
            }



//            vote.setText(String.valueOf(contestant.getVotes()));
//            Picasso.get().load(contestant.getImage()).into(image);
        }
    }

    @Override
    public View getView(int position, View currentView, ViewGroup parent) {
        // GridView requires ViewHolder pattern to ensure optimal performance
        ViewHolder viewHolder;

        if (currentView == null) {
            currentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_clients_list, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)currentView.getTag();
        }

        Client contestant = clientList.get(position);

        viewHolder.bind(contestant);

        return currentView;
    }
}
