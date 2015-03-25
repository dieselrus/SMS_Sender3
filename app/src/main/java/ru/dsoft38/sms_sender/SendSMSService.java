package ru.dsoft38.sms_sender;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by user on 25.03.2015.
 */
public class SendSMSService  extends Service {

    final String LOG_TAG = "Send SMS Service";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void someTask(Intent intent) {
        String [] numList=(String[])intent.getExtras().get("numberList");

        if (numList == null)
            return;

        for(int i = 0; i < numList.length; i++)
        {
            Log.d(LOG_TAG, numList[i]);
        }
    }
}
