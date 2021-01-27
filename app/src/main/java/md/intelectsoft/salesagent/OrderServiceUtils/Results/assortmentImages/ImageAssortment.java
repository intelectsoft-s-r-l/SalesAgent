package md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentImages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImageAssortment {
    @SerializedName("Image1")
    @Expose
    private byte[] image1 = null;
    @SerializedName("Uid")
    @Expose
    private String uid;

    public byte[] getImage1() {
        return image1;
    }

    public void setImage1(byte[] image1) {
        this.image1 = image1;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
