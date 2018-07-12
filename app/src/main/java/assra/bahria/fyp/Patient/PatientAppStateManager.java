package assra.bahria.fyp.Patient;

public class PatientAppStateManager {

    private static final PatientAppStateManager ourInstance = new PatientAppStateManager();
    private int userID;

    public static PatientAppStateManager getInstance() {
        return ourInstance;
    }

    private PatientAppStateManager() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
