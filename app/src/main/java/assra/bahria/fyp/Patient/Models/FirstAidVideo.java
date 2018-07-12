package assra.bahria.fyp.Patient.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import assra.bahria.fyp.Common.Utils.YoutubeIDExtractor;


import com.google.gson.annotations.Expose;
        import com.google.gson.annotations.SerializedName;

public class FirstAidVideo {

    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("desc")
    @Expose
    private String desc;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getYoutubeThumbnailUrl() {
        return "https://img.youtube.com/vi/"+ YoutubeIDExtractor.getVideoId(getLink()) +"/0.jpg";
    }

}