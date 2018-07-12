package assra.bahria.fyp.Patient.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PatientLoginStatus {

    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("user_id")
    @Expose
    private int userID;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUserId() {
        return userID;
    }

    public void setUserId(int userID) {
        this.userID = userID;
    }
}
