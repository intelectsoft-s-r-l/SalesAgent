package md.intelectsoft.salesagent.RealmUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Outlets extends RealmObject {
    @SerializedName("Address")
    @Expose
    private String address;
    @SerializedName("Comment")
    @Expose
    private String comment;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
