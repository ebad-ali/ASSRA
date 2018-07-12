package assra.bahria.fyp.Driver.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HospitalNotifStatus {
    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("hosp_lat")
    @Expose
    private String hospLat;
    @SerializedName("hosp_lng")
    @Expose
    private String hospLng;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getHospLat() {
        return hospLat;
    }

    public void setHospLat(String hospLat) {
        this.hospLat = hospLat;
    }

    public String getHospLng() {
        return hospLng;
    }

    public void setHospLng(String hospLng) {
        this.hospLng = hospLng;
    }
}
