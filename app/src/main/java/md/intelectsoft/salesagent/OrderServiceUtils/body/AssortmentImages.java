package md.intelectsoft.salesagent.OrderServiceUtils.body;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AssortmentImages {

    @SerializedName("Assortiment")
    @Expose
    private List<String> assortiment = null;
    @SerializedName("TokenUid")
    @Expose
    private String tokenUid;

    public List<String> getAssortiment() {
        return assortiment;
    }

    public void setAssortiment(List<String> assortiment) {
        this.assortiment = assortiment;
    }

    public String getTokenUid() {
        return tokenUid;
    }

    public void setTokenUid(String tokenUid) {
        this.tokenUid = tokenUid;
    }
}