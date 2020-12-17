package md.intelectsoft.salesagent.OrderServiceUtils.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;

public class ClientPriceLists {

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("PriceLists")
    @Expose
    private RealmList<PriceList> priceLists = null;
    @SerializedName("Token")
    @Expose
    private TokenResult token;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public RealmList<PriceList> getPriceLists() {
        return priceLists;
    }

    public void setPriceLists(RealmList<PriceList> priceLists) {
        this.priceLists = priceLists;
    }

    public TokenResult getToken() {
        return token;
    }

    public void setToken(TokenResult token) {
        this.token = token;
    }
}
