package ru.dsoft38.sms_sender;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class ShedulerService extends Service {

    final String LOG_TAG = "Sheduler Service";
    // Список телефонных номеров и текст сообщения
    private String[] numList = null;
    private String smsText = null;
    private List<List<String>> numberList = null;

    final static private int iSMSCountPerHour = 30;

    // Интент для сервисов отправки СМС
    Intent sms = null;
    private int iSMSServiceCount = 0;

    // Для хранения всех установленных плагинов
    private List<ApplicationInfo> applist = null;

    // Для передачи данных обратно приложению
    Intent intentApp;

    BroadcastReceiver service;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        //Регистрация приемника
        IntentFilter filter = new IntentFilter();
        filter.addAction("SMSSenderSMSCount");
        filter.addAction("SMSSenderServiceStatus");

        // Для передачи данных обратно приложению
        intentApp = new Intent("SMSSender");

        service = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (intent.getAction().equals("SMSSenderServiceStatus")){

                    if(intent.getStringExtra("servicestatus").equals("stop")) {

                        // Если это последний сервис, разблокируем кнопки иначе стартуес следующее задание
                        if(iSMSServiceCount > applist.size()) {


                            iSMSServiceCount = 0;
                        } else {
                            ApplicationInfo app = applist.get(iSMSServiceCount);
                            ComponentName component = new ComponentName(app.packageName, app.packageName + ".SendSMSService");///////

                            sms = new Intent(app.packageName);
                            sms.setComponent(component);

                            sms.putExtra("numberList", numberList.get(0).toArray(new String[numberList.size()]));
                            sms.putExtra("smsText", smsText);

                            // Запуск сервиса отправки СМС
                            if (null != sms)
                                startService(sms);

                            iSMSServiceCount++;
                        }

                    }
                }
            }
        };
        registerReceiver(service, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        // Получаем переданные номера телефонов и текст СМС
        numList     = (String[])intent.getExtras().get("numberList");
        smsText     = (String)intent.getExtras().get("smsText");
        applist     = (List<ApplicationInfo>)intent.getExtras().get("appList");

        // Передаем данные во встроенный сервис отправки СМС
        sms = new Intent(this, SendSMSService.class);

        sms.putExtra("numberList", numberList.get(0).toArray(new String[ numberList.get(0).size()]));
        sms.putExtra("smsText", smsText);

        // Запуск сервиса отправки СМС
        if (null != sms)
            startService(sms);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        // Уведомляем главное окно о том что сервиз остановлен (закрылся)
        sendDataToApp("SMSSenderServiceStatus", "servicestatus", "stop");
    }

    // Отправка широковещательного сообщения
    private void sendDataToApp(String action, String name,String value){
        intentApp.setAction(action);
        intentApp.removeExtra(name);
        intentApp.putExtra(name, value);
        sendBroadcast(intentApp);
    }
}
