package assra.bahria.fyp.Hospital;

public class HospitalAppStateManager {

    private static final HospitalAppStateManager ourInstance = new HospitalAppStateManager();
    private int userID;

    private boolean isLoggedIn = false;

    public static HospitalAppStateManager getInstance() {
        return ourInstance;
    }

    private HospitalAppStateManager() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean getLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }
}
