package md.intelectsoft.salesagent.OrderServiceUtils.Results;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 30.01.2020
 */

public class DiscountPricesClient {
    @SerializedName("AssortimentUid")
    @Expose
    private String assortimentUid;
    @SerializedName("Price")
    @Expose
    private Double price;
    @SerializedName("PriceLineUid")
    @Expose
    private String priceLineUid;

    public String getAssortimentUid() {
        return assortimentUid;
    }

    public void setAssortimentUid(String assortimentUid) {
        this.assortimentUid = assortimentUid;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPriceLineUid() {
        return priceLineUid;
    }

    public void setPriceLineUid(String priceLineUid) {
        this.priceLineUid = priceLineUid;
    }
}
