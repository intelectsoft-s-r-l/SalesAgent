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

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Collections;
import java.util.List;

import md.intelectsoft.salesagent.R;
import md.intelectsoft.salesagent.RealmUtils.Assortment;

import static md.intelectsoft.salesagent.AssortmentOrderActivity.addProductToCart;

public class AdapterProductsList extends BaseAdapter {

    Context context;
    private List<Assortment> assortmentList = Collections.emptyList();
    boolean preview;

    public AdapterProductsList(Context context, List<Assortment> assortmentList, boolean preview) {
        this.assortmentList = assortmentList;
        this.context = context;
        this.preview = preview;
    }

    @Override
    public int getCount() {
        return assortmentList.size();
    }

    @Override
    public Assortment getItem(int position) {
        return assortmentList.get(position);
    }

    public void setData(List<Assortment> details){
        if (details == null) {
            details = Collections.emptyList();
        }
        this.assortmentList = details;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        private final TextView name;
        private final TextView code;
        private final ImageView image;
        private final ImageView liked;
        private final TextView price;
        private final TextView priceDiscount;
        private final TextView addToCart;
        private final ConstraintLayout layoutPrices;

        public ViewHolder(View view) {
            name = view.findViewById(R.id.textProductName);
            code = view.findViewById(R.id.textProductCode);
            price = view.findViewById(R.id.textProductPrice);
            priceDiscount = view.findViewById(R.id.textProductPriceDiscount);
            addToCart = view.findViewById(R.id.textAddProductToCart);
            liked = view.findViewById(R.id.imageLikedProduct);
            image = view.findViewById(R.id.imageProduct);
            layoutPrices = view.findViewById(R.id.constraintLayout3);
        }

        public void bind(Assortment item) {
            name.setText(item.getName());
            code.setText("#" + item.getCode());
            if(item.getFolder()){
                addToCart.setVisibility(View.GONE);
                layoutPrices.setVisibility(View.GONE);
                image.setImageDrawable(context.getDrawable(R.drawable.folder_product_image));
            }
            else{
                layoutPrices.setVisibility(View.VISIBLE);

                price.setText(item.getPrice() + " MDL");

                if(item.getImage() != null){
                    if(item.getImage().length > 0) {
                        Bitmap productImg = BitmapFactory.decodeByteArray(item.getImage(), 0, item.getImage().length);
                        image.setImageBitmap(productImg);
                    }
                }
                else{
                    image.setImageDrawable(context.getDrawable(R.drawable.product_image));
                }

                if(preview)
                    addToCart.setVisibility(View.GONE);
                else{
                    addToCart.setVisibility(View.VISIBLE);
                    addToCart.setOnClickListener(v -> {
                        addProductToCart(item, 1);
                    });
                }

                if(item.getPriceDiscount() == 0)
                    priceDiscount.setVisibility(View.GONE);
                else{
                    priceDiscount.setVisibility(View.VISIBLE);
                    priceDiscount.setText(item.getPriceDiscount() + " MDL");
                }
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
            currentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_products_list, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)currentView.getTag();
        }

        Assortment contestant = assortmentList.get(position);

        viewHolder.bind(contestant);

        return currentView;
    }
}
