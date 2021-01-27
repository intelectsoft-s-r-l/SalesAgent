package md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentImages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetAssortmentImages {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Images")
    @Expose
    private List<ImageAssortment> images = null;

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

    public List<ImageAssortment> getImages() {
        return images;
    }

    public void setImages(List<ImageAssortment> images) {
        this.images = images;
    }
}