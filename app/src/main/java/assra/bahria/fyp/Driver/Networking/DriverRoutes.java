package assra.bahria.fyp.Driver.Networking;

public class DriverRoutes {

    private static final  String baseUrl =  "http://ennovayt.com/ambulance/";
    public  static final  String  signUp =  baseUrl  + "driver_signup.php";
    public  static final  String   login =  baseUrl + "driver_login.php";
    public  static final  String   setDriverLocation =  baseUrl + "set_driver_location.php";
    public  static final  String   shortListHospitals =  baseUrl + "shortlist_hospital.php";
    public  static final  String   hospitalNotif =  baseUrl + "hospital_notif.php";
    public  static final  String   unbookDriver =  baseUrl + "unbook_driver.php";
    public static final String mapDirections = "https://maps.googleapis.com/maps/api/directions/json";

}
