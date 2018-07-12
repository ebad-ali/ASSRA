package assra.bahria.fyp.Admin;

public class AdminAppStateManager {

    private static final AdminAppStateManager ourInstance = new AdminAppStateManager();
    private int userID;

    public static AdminAppStateManager getInstance() {
        return ourInstance;
    }

    private AdminAppStateManager() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
