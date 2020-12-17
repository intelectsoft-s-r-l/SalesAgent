package md.intelectsoft.salesagent.OrderServiceUtils.Results;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 30.01.2020
 */

public class TokenResult {
    @SerializedName("DeviceUid")
    @Expose
    private String deviceUid;
    @SerializedName("Uid")
    @Expose
    private String uid;
    @SerializedName("ValidTo")
    @Expose
    private String validTo;

    public String getDeviceUid() {
        return deviceUid;
    }

    public void setDeviceUid(String deviceUid) {
        this.deviceUid = deviceUid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }
}
