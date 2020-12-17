package md.intelectsoft.salesagent.RealmUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Igor on 30.01.2020
 */

public class Assortment extends RealmObject {
    @SerializedName("BarCode")
    @Expose
    private String barCode;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("FullName")
    @Expose
    private Boolean fullName;
    @SerializedName("IsFolder")
    @Expose
    private Boolean isFolder;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("ParentUid")
    @Expose
    private String parentUid;
    @SerializedName("Uid")
    @Expose
    private String uid;
    @SerializedName("Marking")
    @Expose
    private String marking;
    @SerializedName("Price")
    @Expose
    private Double price;
    @SerializedName("PricelineUid")
    @Expose
    private String pricelineUid;
    @SerializedName("Remain")
    @Expose
    private Double remain;
    @SerializedName("UnitName")
    @Expose
    private String unitName;

    private boolean isFavorit;

    private long favoritOrder;

    private double qauntity;

    private double priceDiscount;

    public double getQauntity() {
        return qauntity;
    }

    public void setQauntity(double qauntity) {
        this.qauntity = qauntity;
    }

    public boolean isFavorit() {
        return isFavorit;
    }

    public void setFavorit(boolean favorit) {
        isFavorit = favorit;
    }

    public long getFavoritOrder() {
        return favoritOrder;
    }

    public void setFavoritOrder(long favoritOrder) {
        this.favoritOrder = favoritOrder;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getFullName() {
        return fullName;
    }

    public void setFullName(Boolean fullName) {
        this.fullName = fullName;
    }

    public Boolean getFolder() {
        return isFolder;
    }

    public void setFolder(Boolean folder) {
        isFolder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentUid() {
        return parentUid;
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMarking() {
        return marking;
    }

    public void setMarking(String marking) {
        this.marking = marking;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPricelineUid() {
        return pricelineUid;
    }

    public void setPricelineUid(String pricelineUid) {
        this.pricelineUid = pricelineUid;
    }

    public Double getRemain() {
        return remain;
    }

    public void setRemain(Double remain) {
        this.remain = remain;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public double getPriceDiscount() {
        return priceDiscount;
    }

    public void setPriceDiscount(double priceDiscount) {
        this.priceDiscount = priceDiscount;
    }
}
