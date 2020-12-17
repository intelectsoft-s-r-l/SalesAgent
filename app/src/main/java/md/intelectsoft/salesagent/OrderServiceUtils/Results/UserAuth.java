package md.intelectsoft.salesagent.OrderServiceUtils.Results;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 30.01.2020
 */

public class UserAuth {
    @SerializedName("BarCode")
    @Expose
    private String barCode;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Description")
    @Expose
    private String description;
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
    @SerializedName("IDNP")
    @Expose
    private String iDNP;
    @SerializedName("Patronymic")
    @Expose
    private String patronymic;
    @SerializedName("Surname")
    @Expose
    private String surname;

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

    public Boolean getIsFolder() {
        return isFolder;
    }

    public void setIsFolder(Boolean isFolder) {
        this.isFolder = isFolder;
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

    public String getIDNP() {
        return iDNP;
    }

    public void setIDNP(String iDNP) {
        this.iDNP = iDNP;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
