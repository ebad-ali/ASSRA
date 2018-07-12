package assra.bahria.fyp.Admin.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdminLoginStatus {

    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("admin_id")
    @Expose
    private int adminId;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

}