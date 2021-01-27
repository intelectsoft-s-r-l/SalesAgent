package md.intelectsoft.salesagent.RealmUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Igor on 30.01.2020
 */

public class Client extends RealmObject {
    @SerializedName("Balance")
    @Expose
    private String balance;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("IDNP")
    @Expose
    private String iDNP;
    @SerializedName("Image")
    @Expose
    private byte[] image;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Outlets")
    @Expose
    private RealmList<Outlets> outlets;
    @SerializedName("PricelistUid")
    @Expose
    private String priceListUid;
    @SerializedName("TVACode")
    @Expose
    private String tVACode;
    @SerializedName("Uid")
    @Expose
    private String uid;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIDNP() {
        return iDNP;
    }

    public void setIDNP(String iDNP) {
        this.iDNP = iDNP;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Outlets> getOutlets() {
        return outlets;
    }

    public void setOutlets(RealmList<Outlets> outlets) {
        this.outlets = outlets;
    }

    public String getTVACode() {
        return tVACode;
    }

    public void setTVACode(String tVACode) {
        this.tVACode = tVACode;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getPriceListUid() {
        return priceListUid;
    }

    public void setPriceListUid(String priceListUid) {
        this.priceListUid = priceListUid;
    }
}
