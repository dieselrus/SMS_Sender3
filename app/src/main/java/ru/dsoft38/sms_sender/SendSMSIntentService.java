package ru.dsoft38.sms_sender;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

public class SendSMSIntentService extends IntentService {

    private static final String ACTION_SEND = "ru.dsoft38.sms_sender.action.SEND";
    private static final String EXTRA_PHONE_NUMBER = "12345678910";
    private static final String EXTRA_MSG_TEXT = "EXTRA_MSG_TEXT";
    private static final String EXTRA_SMS_COUNT = "0";


    // Флаги для отправки и доставки SMS
    private String SENT_SMS_FLAG = "SENT_SMS";
    private String DELIVER_SMS_FLAG = "DELIVER_SMS";

    private PendingIntent sentPIn = null;
    private PendingIntent deliverPIn = null;

    // максимальное количество отправляемых сообщений для этого сервиса
    private final String LOG_TAG = "Send SMS Service";
    //private int maxSMSIndex = 100;

    // Для передачи данных обратно приложению
    Intent intentApp;

    /*
        Intent intent = new Intent(context, SendSMSIntentService.class);
        intent.setAction(ACTION_SEND);
        intent.putExtra(EXTRA_PHONE_NUMBER, param1);
        intent.putExtra(EXTRA_MSG_TEXT, param2);
        intent.putExtra(EXTRA_SMS_COUNT, param3);
        context.startService(intent);
    */

    public SendSMSIntentService() {
        super("SendSMSIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            // Создаем интенты для регистрации широковещательных приемников
            Intent sentIn = new Intent(SENT_SMS_FLAG);
            sentPIn = PendingIntent.getBroadcast(this, 0, sentIn, 0);

            Intent deliverIn = new Intent(DELIVER_SMS_FLAG);
            deliverPIn = PendingIntent.getBroadcast(this, 0, deliverIn, 0);

            final String action = intent.getAction();
            if (ACTION_SEND.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                final String param2 = intent.getStringExtra(EXTRA_MSG_TEXT);
                final String param3 = intent.getStringExtra(EXTRA_SMS_COUNT);
                sendSMS(param1, param2, param3);
            }
        }
    }

    // Подготовка и проверки для отправки СМС
    void sendSMS(String strPhoneNumber, String strMsgText, String currentSMSNumberIndex) {
        //SmsManager smsManager = SmsManager.getDefault();
        // отправляем сообщение
        Log.d(LOG_TAG, "Отправляется сообщение №" + currentSMSNumberIndex);

        // Удаляем не нужные символы
        strPhoneNumber = strPhoneNumber.replace("-", "").replace(";", "").replace(" ", "").trim();

        // Проверяем длину номера 11 символов или 12, если с +
        /*
        if (currentPhoneNumber.length() == 11 || (currentPhoneNumber.substring(0, 1).equals("+") && currentPhoneNumber.length() == 12)) {
            Log.d(LOG_TAG, "Отправляется");
            //smsManager.sendTextMessage(num, null, smsText, sentPIn, deliverPIn);
            //smsManager.sendTextMessage("5556", null, smsText, null, null);
            sendingSMS(currentPhoneNumber, smsText);
        }
        */

        sendingSMS(strPhoneNumber, strMsgText);

        // Оповещаем приложение об отправке СМС
        sendDataToApp("SMSSenderSMSCount", "smscount", currentSMSNumberIndex);
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

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d(LOG_TAG, "onCreate");

        Intent sentIn = new Intent(SENT_SMS_FLAG);
        sentPIn = PendingIntent.getBroadcast(this, 0, sentIn, 0);

        Intent deliverIn = new Intent(DELIVER_SMS_FLAG);
        deliverPIn = PendingIntent.getBroadcast(this, 0, deliverIn, 0);

        // Для передачи данных обратно приложению
        intentApp = new Intent("SMSSender");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(LOG_TAG, "onDestroy");
        // Уведомляем главное окно о том что сервиз остановлен (закрылся)
        sendDataToApp("SMSSenderServiceStatus", "servicestatus", "stop");
        // отмена регистрации на оповещение отправки и доставка СМС
        unregisterReceiver(sentReceiver);
        //unregisterReceiver(deliverReceiver);
    }

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

    // Широковещательный приемник для отслеживания отправки СМС
    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent in) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Log.d(LOG_TAG, "Сообщение отправлено!");
                    //currentSMSNumberIndex++;
                    //sendSMS();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF :
                    Log.d(LOG_TAG, "Телефонный модуль выключен!");
                    //sendSMS();
                    sendDataToApp("SMSSenderServiceError", "toast", "Телефонный модуль выключен!");
                    //trySendingSMS();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU :
                    Log.d(LOG_TAG, "Возникла проблема, связанная с форматом PDU (protocol description unit)!");
                    //sendSMS();
                    sendDataToApp("SMSSenderServiceError", "toast", "Возникла проблема, связанная с форматом PDU (protocol description unit)!");
                    //trySendingSMS();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Log.d(LOG_TAG, "При отправке возникли неизвестные проблемы!");
                    //sendSMS();
                    sendDataToApp("SMSSenderServiceError", "toast", "При отправке возникли неизвестные проблемы!");
                    //trySendingSMS();
                    break;
                default:
                    // sent SMS message failed
                    Log.d(LOG_TAG, "Сообщение не отправлено!");
                    //sendSMS();
                    sendDataToApp("SMSSenderServiceError", "toast", "Сообщение не отправлено!");
                    //trySendingSMS();
                    break;
            }
        }
    };
}
