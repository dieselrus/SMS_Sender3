package ru.dsoft38.sms_sender;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.List;

/**
 * Created by user on 25.03.2015.
 */
public class SendSMSService  extends Service {

    // максимальное количество отправляемых сообщений для этого сервиса
    private final String LOG_TAG = "Send SMS Service";
    private int maxSMSIndex = 100;

    // Флаги для отправки и доставки SMS
    private String SENT_SMS_FLAG = "SENT_SMS";
    private String DELIVER_SMS_FLAG = "DELIVER_SMS";

    private PendingIntent sentPIn = null;
    private PendingIntent deliverPIn = null;

    // Список телефонных номеров и текст сообщения
    private String[] numList = null;
    private String smsText = null;
    private String currentPhoneNumber;

    private int currentSMSNumberIndex = 0;

    private int tryCountSendSMS = 1;

    // Для передачи данных обратно приложению
    Intent intentApp;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        Intent sentIn = new Intent(SENT_SMS_FLAG);
        sentPIn = PendingIntent.getBroadcast(this, 0, sentIn, 0);

        Intent deliverIn = new Intent(DELIVER_SMS_FLAG);
        deliverPIn = PendingIntent.getBroadcast(this, 0, deliverIn, 0);

        // Для передачи данных обратно приложению
        intentApp = new Intent("SMSSender");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        // Регистрация на оповещения об отправке и доставке СМС
        registerReceiver(sentReceiver, new IntentFilter(SENT_SMS_FLAG));
        //registerReceiver(deliverReceiver, new IntentFilter(DELIVER_SMS_FLAG));

        sendDataToApp("SMSSenderServiceStatus", "servicestatus", "start");

        // Получаем переданные номера телефонов и текст СМС
        numList     = (String[])intent.getExtras().get("numberList");
        smsText     = (String)intent.getExtras().get("smsText");
        maxSMSIndex = numList.length;

        // Отправляем СМС
        sendSMS();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        // Уведомляем главное окно о том что сервиз остановлен (закрылся)
        sendDataToApp("SMSSenderServiceStatus", "servicestatus", "stop");
        // отмена регистрации на оповещение отправки и доставка СМС
        unregisterReceiver(sentReceiver);
        //unregisterReceiver(deliverReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    // Подготовка и проверки для отправки СМС
    void sendSMS() {
        // Завершаем сервис если отправили максимальное количество СМС (-1 потому что индекс в массиве начинается с 0)
        //if (currentSMSNumberIndex >= maxSMSIndex - 1)
        //        stopSelf();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if ( currentSMSNumberIndex >= maxSMSIndex ) {
            sendDataToApp("SMSSenderServiceStatus", "endtask", "end");
            stopSelf();
            return;
        }

        if ( numList[currentSMSNumberIndex] == null | smsText == null ) {
            sendDataToApp("SMSSenderServiceStatus", "endtask", "end");
            stopSelf();
            return;
        }

        //SmsManager smsManager = SmsManager.getDefault();
        // отправляем сообщение
        Log.d(LOG_TAG, "Отправляется сообщение №" + String.valueOf(currentSMSNumberIndex + 1) + " из " + String.valueOf(maxSMSIndex));

        // Удаляем не нужные символы
        String currentPhoneNumber = numList[currentSMSNumberIndex].replace("-", "").replace(";", "").replace(" ", "").trim();

        // Проверяем длину номера 11 символов или 12, если с +
        if (currentPhoneNumber.length() == 11 || (currentPhoneNumber.substring(0, 1).equals("+") && currentPhoneNumber.length() == 12)) {
            Log.d(LOG_TAG, "Отправляется");
            //smsManager.sendTextMessage(num, null, smsText, sentPIn, deliverPIn);
            //smsManager.sendTextMessage("5556", null, smsText, null, null);
            sendingSMS(currentPhoneNumber, smsText);
        }

        // Оповещаем приложение об отправке СМС
        sendDataToApp("SMSSenderSMSCount", "smscount", String.valueOf(currentSMSNumberIndex + 1));

    }

    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent in) {
            // Завершаем сервис если отправили максимальное количество СМС )
            if (currentSMSNumberIndex >= maxSMSIndex) {
                sendDataToApp("SMSSenderServiceStatus", "endtask", "end");
                stopSelf();
            }
            // Увеличиваем счетчик для номеров телефонов в списке
            currentSMSNumberIndex++;

            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    // sent SMS message successfully;
                    //Toast toast = Toast.makeText(getApplicationContext(),"Сообщение отправлено!", Toast.LENGTH_SHORT);
                    //toast.show();
                    Log.d(LOG_TAG, "Сообщение отправлено!");
                    //currentSMSNumberIndex++;
                    sendSMS();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF :
                    Log.d(LOG_TAG, "Телефонный модуль выключен!");
                    //sendSMS();
                    sendDataToApp("SMSSenderServiceError", "toast", "Телефонный модуль выключен!");
                    trySendingSMS();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU :
                    Log.d(LOG_TAG, "Возникла проблема, связанная с форматом PDU (protocol description unit)!");
                    //sendSMS();
                    sendDataToApp("SMSSenderServiceError", "toast", "Возникла проблема, связанная с форматом PDU (protocol description unit)!");
                    trySendingSMS();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Log.d(LOG_TAG, "При отправке возникли неизвестные проблемы!");
                    //sendSMS();
                    sendDataToApp("SMSSenderServiceError", "toast", "При отправке возникли неизвестные проблемы!");
                    trySendingSMS();
                    break;
                default:
                    // sent SMS message failed
                    Log.d(LOG_TAG, "Сообщение не отправлено!");
                    //sendSMS();
                    sendDataToApp("SMSSenderServiceError", "toast", "Сообщение не отправлено!");
                    trySendingSMS();
                    break;
            }
        }
    };

    BroadcastReceiver deliverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent in) {
            // Завершаем сервис если отправили максимальное количество СМС )
            if (currentSMSNumberIndex >= maxSMSIndex) {
                sendDataToApp("SMSSenderServiceStatus", "endtask", "end");
                stopSelf();
            }

            // Увеличиваем счетчик для номеров телефонов в списке
            //currentSMSNumberIndex++;

            // В зависимости от ответа о доставке СМС выводим лог. Запускаем отправку следующего СМС (проверить как будет если номер отключен)
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    // sent SMS message successfully;
                    //Toast toast = Toast.makeText(getApplicationContext(), "Сообщение доставлено!", Toast.LENGTH_SHORT);
                    //toast.show();
                    Log.d(LOG_TAG, "Сообщение доставлено!");
                    //sendSMS();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Log.d(LOG_TAG, "!Телефонный модуль выключен!");
                    //sendSMS();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU :
                    Log.d(LOG_TAG, "!Возникла проблема, связанная с форматом PDU (protocol description unit)!");
                    //sendSMS();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Log.d(LOG_TAG, "!При отправке возникли неизвестные проблемы!");
                    //sendSMS();
                    break;
                default:
                    // sent SMS message failed
                    Log.d(LOG_TAG, "Сообщение не доставлено!");
                    //sendSMS();
                    break;
            }
        }
    };

    /**
     * Отправка широковещательного сообщения
     * @param action - идентификатор
     * @param name  - ключ
     * @param value - значение
     */
    private void sendDataToApp(String action, String name,String value){
        intentApp.setAction(action);
        intentApp.removeExtra(name);
        intentApp.putExtra(name, value);
        sendBroadcast(intentApp);
    }

    /**
     * Непостредственная отправка СМС
     * @param _num - номер телефона
     * @param _smsText - текст СМС
     */
    private void sendingSMS(String _num, String _smsText){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(_num, null, _smsText, sentPIn, deliverPIn);
    }

    // Попытки отправки СМС
    private void trySendingSMS(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (tryCountSendSMS <= 3){
            sendingSMS(currentPhoneNumber, smsText);
            tryCountSendSMS++;
        } else {
            sendDataToApp("SMSSenderServiceError", "toast", "Отправка приостановлена. Проверьте баланс и наличие сети!");
        }
    }
}
