package md.intelectsoft.salesagent.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import static md.intelectsoft.salesagent.AddProductsFromOrder.addProductToOrder;
import static md.intelectsoft.salesagent.AddProductsFromOrder.isIsViewWithCatalog;

public class AdapterProductsListToOrder extends BaseAdapter {

    Context context;
    private List<Assortment> assortmentList = Collections.emptyList();
    boolean preview;

    public AdapterProductsListToOrder(Context context, List<Assortment> assortmentList, boolean preview) {
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

        //list view catalog
        private TextView productBarcode;
        private TextView productRemain;

        public ViewHolder(View view, boolean isGrid) {
            if(isGrid){
                name = view.findViewById(R.id.textProductName);
                code = view.findViewById(R.id.textProductCode);
                price = view.findViewById(R.id.textProductPrice);
                priceDiscount = view.findViewById(R.id.textProductPriceDiscount);
                addToCart = view.findViewById(R.id.textAddProductToCart);
                liked = view.findViewById(R.id.imageLikedProduct);
                image = view.findViewById(R.id.imageProduct);
                layoutPrices = view.findViewById(R.id.constraintLayout3);
            }
            else{
                name = view.findViewById(R.id.textProductName);
                code = view.findViewById(R.id.textProductCode);
                price = view.findViewById(R.id.textProductPrice);
                priceDiscount = view.findViewById(R.id.textProductPriceDiscount);
                addToCart = view.findViewById(R.id.textAddProductToCart);
                liked = view.findViewById(R.id.imageLikedProduct);
                image = view.findViewById(R.id.imageProduct);
                layoutPrices = view.findViewById(R.id.constraintLayout3);
                productBarcode = view.findViewById(R.id.textProductBarcode);
                productRemain = view.findViewById(R.id.textProductRemain);
            }
        }

        public void bind(Assortment item, boolean isGrid) {
            if(isGrid){
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
                            addProductToOrder(item, 1);
                        });
                    }

                    if(item.getPriceDiscount() == 0)
                        priceDiscount.setVisibility(View.GONE);
                    else{
                        priceDiscount.setVisibility(View.VISIBLE);
                        priceDiscount.setText(item.getPriceDiscount() + " MDL");
                    }
                }
            }
            else{
                name.setText(item.getName());
                code.setText("#" + item.getCode());
                productBarcode.setText(item.getBarCode());
                if(item.getFolder()){
                    addToCart.setVisibility(View.GONE);
                    layoutPrices.setVisibility(View.GONE);
                    image.setImageDrawable(context.getDrawable(R.drawable.folder_product_image));
                    productBarcode.setVisibility(View.GONE);
                    productRemain.setVisibility(View.GONE);
                }
                else{
                    layoutPrices.setVisibility(View.VISIBLE);
                    productBarcode.setVisibility(View.VISIBLE);
                    productRemain.setVisibility(View.VISIBLE);

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
                            addProductToOrder(item, 1);
                        });
                    }

                    if(item.getRemain() != 0){
                        productRemain.setText(String.format("%.2f", item.getRemain()) + " /" + item.getUnitName());
                        productRemain.setTextColor(context.getColor(R.color.teal_700));
                    }
                    else{
                        productRemain.setText("0 /" + item.getUnitName());
                        productRemain.setTextColor(Color.RED);
                    }

                    if(item.getPriceDiscount() == 0)
                        priceDiscount.setVisibility(View.GONE);
                    else{
                        priceDiscount.setVisibility(View.VISIBLE);
                        priceDiscount.setText(item.getPriceDiscount() + " MDL");
                    }
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

        if(isIsViewWithCatalog()) {
            // show products in grid view
            currentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_products_list, parent, false);
            viewHolder = new ViewHolder(currentView, true);

            Assortment contestant = assortmentList.get(position);
            viewHolder.bind(contestant, true);
        }
        else{
            currentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_products_list, parent, false);
            viewHolder = new ViewHolder(currentView, false);

            Assortment contestant = assortmentList.get(position);
            viewHolder.bind(contestant, false);
        }

        return currentView;
    }
}
