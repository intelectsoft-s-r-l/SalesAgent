package md.intelectsoft.salesagent.OrderServiceUtils.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import md.intelectsoft.salesagent.RealmUtils.Price;

public class PriceList extends RealmObject {

    @SerializedName("PriceListUid")
    @Expose
    private String priceListUid;
    @SerializedName("Prices")
    @Expose
    private RealmList<Price> prices = null;

    public String getPriceListUid() {
        return priceListUid;
    }

    public void setPriceListUid(String priceListUid) {
        this.priceListUid = priceListUid;
    }

    public RealmList<Price> getPrices() {
        return prices;
    }

    public void setPrices(RealmList<Price> prices) {
        this.prices = prices;
    }

}
