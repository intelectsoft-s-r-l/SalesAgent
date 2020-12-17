package md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentDescription;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AssortmentDescription {
    @SerializedName("Brand")
    @Expose
    private String brand;
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Image1")
    @Expose
    private byte[] image1 = null;
    @SerializedName("Image2")
    @Expose
    private byte[] image2 = null;
    @SerializedName("Image3")
    @Expose
    private byte[] image3 = null;
    @SerializedName("Image4")
    @Expose
    private byte[] image4 = null;
    @SerializedName("Producer")
    @Expose
    private String producer;
    @SerializedName("TranslatedDescription")
    @Expose
    private TranslatedDescription translatedDescription;
    @SerializedName("TranslatedName")
    @Expose
    private TranslatedName translatedName;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

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

    public byte[] getImage1() {
        return image1;
    }

    public void setImage1(byte[] image1) {
        this.image1 = image1;
    }

    public byte[] getImage2() {
        return image2;
    }

    public void setImage2(byte[] image2) {
        this.image2 = image2;
    }

    public byte[] getImage3() {
        return image3;
    }

    public void setImage3(byte[] image3) {
        this.image3 = image3;
    }

    public byte[] getImage4() {
        return image4;
    }

    public void setImage4(byte[] image4) {
        this.image4 = image4;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public TranslatedDescription getTranslatedDescription() {
        return translatedDescription;
    }

    public void setTranslatedDescription(TranslatedDescription translatedDescription) {
        this.translatedDescription = translatedDescription;
    }

    public TranslatedName getTranslatedName() {
        return translatedName;
    }

    public void setTranslatedName(TranslatedName translatedName) {
        this.translatedName = translatedName;
    }

}
