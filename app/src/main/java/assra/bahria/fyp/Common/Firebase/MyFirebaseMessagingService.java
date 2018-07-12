package assra.bahria.fyp.Common.Firebase;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.sql.Struct;

import assra.bahria.fyp.Common.Constants;
import assra.bahria.fyp.Driver.DriverAppStateManager;
import assra.bahria.fyp.Driver.Models.DriverNotificationStatus;
import assra.bahria.fyp.Hospital.Activities.HospitalMainActivity;
import assra.bahria.fyp.Hospital.HospitalAppStateManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);





        // For driver notification when patient calls ambulance
        if (DriverAppStateManager.getInstance().getLoggedIn()) {
            Intent intent = new Intent(Constants.fireBaseBroadcasteEvent);
            intent.putExtra("name", remoteMessage.getData().get("name"));
            intent.putExtra("lat", remoteMessage.getData().get("lat"));
            intent.putExtra("lng", remoteMessage.getData().get("lng"));
            intent.putExtra("statusID", remoteMessage.getData().get("status_id"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        // For hospital notification when driver has arrived at patient location
        // and selects a hospital
        else if (HospitalAppStateManager.getInstance().getLoggedIn()) {
            Intent intent = new Intent(Constants.driverArrivedNotificationEvent);
            intent.putExtra("driver_name", remoteMessage.getData().get("driver_name"));
            intent.putExtra("lat", remoteMessage.getData().get("lat"));
            intent.putExtra("lng", remoteMessage.getData().get("lng"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }




    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();

    }


}
