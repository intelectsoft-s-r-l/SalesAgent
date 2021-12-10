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

    //for me
    private boolean savedDataComment;
    private String namePerson;
    private String surName;
    private String phone;
    private String address;
    private String additionalInfo;

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

    public boolean isSavedDataComment() {
        return savedDataComment;
    }

    public void setSavedDataComment(boolean savedDataComment) {
        this.savedDataComment = savedDataComment;
    }

    public String getNamePerson() {
        return namePerson;
    }

    public void setNamePerson(String namePerson) {
        this.namePerson = namePerson;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
