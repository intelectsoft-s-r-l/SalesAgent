package md.intelectsoft.salesagent.OrderServiceUtils.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import md.intelectsoft.salesagent.RealmUtils.Assortment;

/**
 * Created by Igor on 30.01.2020
 */

public class AssortmentList {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Token")
    @Expose
    private TokenResult token;
    @SerializedName("Assortiment")
    @Expose
    private RealmList<Assortment> assortment = null;

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

    public RealmList<Assortment> getAssortment() {
        return assortment;
    }

    public void setAssortment(RealmList<Assortment> assortment) {
        this.assortment = assortment;
    }
}
