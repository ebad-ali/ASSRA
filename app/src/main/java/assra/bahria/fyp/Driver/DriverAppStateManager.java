package assra.bahria.fyp.Driver;

public class DriverAppStateManager {
    private static final DriverAppStateManager ourInstance = new DriverAppStateManager();
    private int userID;
    private Boolean isLoggedIn = false;
    private int statusID;

    public static DriverAppStateManager getInstance() {
        return ourInstance;
    }

    private DriverAppStateManager() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }


    public Boolean getLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public int getStatusID() {
        return statusID;
    }

    public void setStatusID(int statusID) {
        this.statusID = statusID;
    }
}