package md.intelectsoft.salesagent.OrderServiceUtils.Results;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 30.01.2020
 */

public class ClientPrices {

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Token")
    @Expose
    private TokenResult token;
    @SerializedName("Prices")
    @Expose
    private List<DiscountPricesClient> prices = null;

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

    public TokenResult getToken() {
        return token;
    }

    public void setToken(TokenResult token) {
        this.token = token;
    }

    public List<DiscountPricesClient> getPrices() {
        return prices;
    }

    public void setPrices(List<DiscountPricesClient> prices) {
        this.prices = prices;
    }
}
