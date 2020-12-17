package md.intelectsoft.salesagent.RealmUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Igor on 30.01.2020
 */

public class Request extends RealmObject {
    @SerializedName("ClientName")
    @Expose
    private String clientName;
    @SerializedName("ClientUid")
    @Expose
    private String clientUid;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Comment")
    @Expose
    private String comment;
    @SerializedName("DateProcessed")
    @Expose
    private String dateProcessed;
    @SerializedName("DateValid")
    @Expose
    private String dateValid;
    @SerializedName("DeliveryAddress")
    @Expose
    private String deliveryAddress;
    @SerializedName("Lines")
    @Expose
    private RealmList<RequestLine> lines = null;
    @SerializedName("State")
    @Expose
    private Integer state;
    @SerializedName("StockName")
    @Expose
    private String stockName;
    @SerializedName("StockUid")
    @Expose
    private String stockUid;
    @SerializedName("Sum")
    @Expose
    private Double sum;
    @SerializedName("Uid")
    @Expose
    private String uid;

    private int syncState;

    private String internId;
    //for me
    private long dateToLong;

    private boolean toDelete;

    public long getDateToLong() {
        return dateToLong;
    }

    public void setDateToLong(long dateToLong) {
        this.dateToLong = dateToLong;
    }

    public int getSyncState() {
        return syncState;
    }

    public void setSyncState(int syncState) {
        this.syncState = syncState;
    }

    public String getInternId() {
        return internId;
    }

    public void setInternId(String internId) {
        this.internId = internId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDateProcessed() {
        return dateProcessed;
    }

    public void setDateProcessed(String dateProcessed) {
        this.dateProcessed = dateProcessed;
    }

    public String getDateValid() {
        return dateValid;
    }

    public void setDateValid(String dateValid) {
        this.dateValid = dateValid;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public RealmList<RequestLine> getLines() {
        return lines;
    }

    public void setLines(RealmList<RequestLine> lines) {
        this.lines = lines;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getStockUid() {
        return stockUid;
    }

    public void setStockUid(String stockUid) {
        this.stockUid = stockUid;
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

    public boolean isToDelete() {
        return toDelete;
    }

    public void setToDelete(boolean toDelete) {
        this.toDelete = toDelete;
    }
}
