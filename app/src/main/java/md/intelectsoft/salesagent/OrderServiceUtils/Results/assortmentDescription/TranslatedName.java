package md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentDescription;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TranslatedName {

    @SerializedName("EN")
    @Expose
    private String eN;
    @SerializedName("RO")
    @Expose
    private String rO;
    @SerializedName("RU")
    @Expose
    private String rU;

    public String getEN() {
        return eN;
    }

    public void setEN(String eN) {
        this.eN = eN;
    }

    public String getRO() {
        return rO;
    }

    public void setRO(String rO) {
        this.rO = rO;
    }

    public String getRU() {
        return rU;
    }

    public void setRU(String rU) {
        this.rU = rU;
    }

}
