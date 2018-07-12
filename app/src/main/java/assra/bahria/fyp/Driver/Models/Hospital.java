package assra.bahria.fyp.Driver.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Hospital {

    @SerializedName("h_id")
    @Expose
    private String hId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("distance")
    @Expose
    private double distance;
    @SerializedName("lat")
    @Expose
    private String lat;
    @SerializedName("lng")
    @Expose
    private String lng;

    public String getHId() {
        return hId;
    }

    public void setHId(String hId) {
        this.hId = hId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

}