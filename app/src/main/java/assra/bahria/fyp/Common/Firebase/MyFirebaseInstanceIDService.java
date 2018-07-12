package assra.bahria.fyp.Common.Firebase;

import android.app.Service;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInstanceIDSer";
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

       Log.d( TAG,"LOLOLO" + FirebaseInstanceId.getInstance().getToken() );
    }
}
