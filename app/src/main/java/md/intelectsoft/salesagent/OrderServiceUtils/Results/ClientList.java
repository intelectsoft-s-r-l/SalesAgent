package md.intelectsoft.salesagent.OrderServiceUtils.Results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import md.intelectsoft.salesagent.RealmUtils.Client;

/**
 * Created by Igor on 30.01.2020
 */

public class ClientList {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Token")
    @Expose
    private TokenResult token;
    @SerializedName("Contragents")
    @Expose
    private RealmList<Client> clients = null;

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

    public RealmList<Client> getClients() {
        return clients;
    }

    public void setClients(RealmList<Client> clients) {
        this.clients = clients;
    }
}
