package assra.bahria.fyp.Common.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Status {

    @SerializedName("status")
    @Expose
    private String status;

    /**
     * No args constructor for use in serialization
     */
    public Status() {
    }

    /**
     * @param status
     */
    public Status(String status) {
        super();
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}