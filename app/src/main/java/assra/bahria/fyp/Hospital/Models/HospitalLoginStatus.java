package assra.bahria.fyp.Hospital.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HospitalLoginStatus {
    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("id")
    @Expose
    private int id;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
