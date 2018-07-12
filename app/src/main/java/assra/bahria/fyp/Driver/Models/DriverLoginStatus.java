package assra.bahria.fyp.Driver.Models;

import android.content.Intent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DriverLoginStatus {

    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("driver_id")
    @Expose
    private int driverId;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

}