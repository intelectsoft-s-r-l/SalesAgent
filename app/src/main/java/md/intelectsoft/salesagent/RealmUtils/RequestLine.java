package md.intelectsoft.salesagent.RealmUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Igor on 30.01.2020
 */

public class RequestLine extends RealmObject {
    @SerializedName("AssortimentBarcode")
    @Expose
    private String assortimentBarcode;
    @SerializedName("AssortimentCode")
    @Expose
    private String assortimentCode;
    @SerializedName("AssortimentName")
    @Expose
    private String assortimentName;
    @SerializedName("AssortimentUid")
    @Expose
    private String assortimentUid;
    @SerializedName("Count")
    @Expose
    private Double count;
    @SerializedName("LineNumber")
    @Expose
    private Integer lineNumber;
    @SerializedName("Price")
    @Expose
    private Double price;
    @SerializedName("ProcessedCount")
    @Expose
    private Double processedCount;
    @SerializedName("Sum")
    @Expose
    private Double sum;
    @SerializedName("Uid")
    @Expose
    private String uid;
    @SerializedName("UnitName")
    @Expose
    private String unitName;
    @SerializedName("UnitUid")
    @Expose
    private String unitUid;

    private double priceDiscount = 0;
    private String priceLineUid;
    private double priceDialler = 0;

    public double getPriceDiscount() {
        return priceDiscount;
    }

    public void setPriceDiscount(double priceDiscount) {
        this.priceDiscount = priceDiscount;
    }

    public String getAssortimentBarcode() {
        return assortimentBarcode;
    }

    public void setAssortimentBarcode(String assortimentBarcode) {
        this.assortimentBarcode = assortimentBarcode;
    }

    public String getAssortimentCode() {
        return assortimentCode;
    }

    public void setAssortimentCode(String assortimentCode) {
        this.assortimentCode = assortimentCode;
    }

    public String getAssortimentName() {
        return assortimentName;
    }

    public void setAssortimentName(String assortimentName) {
        this.assortimentName = assortimentName;
    }

    public String getAssortimentUid() {
        return assortimentUid;
    }

    public void setAssortimentUid(String assortimentUid) {
        this.assortimentUid = assortimentUid;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(Double processedCount) {
        this.processedCount = processedCount;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitUid() {
        return unitUid;
    }

    public void setUnitUid(String unitUid) {
        this.unitUid = unitUid;
    }

    public String getPriceLineUid() {
        return priceLineUid;
    }

    public void setPriceLineUid(String priceLineUid) {
        this.priceLineUid = priceLineUid;
    }

    public double getPriceDialler() {
        return priceDialler;
    }

    public void setPriceDialler(double priceDialler) {
        this.priceDialler = priceDialler;
    }
}
