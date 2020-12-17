package md.intelectsoft.salesagent.OrderServiceUtils.body;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 30.01.2020
 */

public class ClientPricesBody {
    @SerializedName("ClientUid")
    @Expose
    private String clientUid;
    @SerializedName("Pricelines")
    @Expose
    private List<String> pricelines = null;
    @SerializedName("TokenUid")
    @Expose
    private String tokenUid;

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }

    public List<String> getPricelines() {
        return pricelines;
    }

    public void setPricelines(List<String> pricelines) {
        this.pricelines = pricelines;
    }

    public String getTokenUid() {
        return tokenUid;
    }

    public void setTokenUid(String tokenUid) {
        this.tokenUid = tokenUid;
    }
}
