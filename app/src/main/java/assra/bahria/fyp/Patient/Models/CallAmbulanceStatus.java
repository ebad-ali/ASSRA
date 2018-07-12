package assra.bahria.fyp.Patient.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallAmbulanceStatus  {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("cnic")
    @Expose
    private String cnic;


    @SerializedName("liscense_num")
    @Expose
    private String licenseNum;


    @SerializedName("contact")
    @Expose
    private String contact;

    @SerializedName("lat")
    @Expose
    private String lat;

    @SerializedName("lng")
    @Expose
    private String lng;


    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("status")
    @Expose
    private int status;

    @SerializedName("status_id")
    @Expose
    private int statusID;

    public int getStatusID() {
        return statusID;
    }

    public void setStatusID(int statusID) {
        this.statusID = statusID;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getCnic() {
        return cnic;
    }

    public String getLicenseNum() {
        return licenseNum;
    }

    public String getContact() {
        return contact;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getToken() {
        return token;
    }

    public int getStatus() {
        return status;
    }


}

