package ru.dsoft38.sms_sender;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SendSMSIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "ru.dsoft38.sms_sender.action.FOO";
    private static final String ACTION_BAZ = "ru.dsoft38.sms_sender.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "ru.dsoft38.sms_sender.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "ru.dsoft38.sms_sender.extra.PARAM2";


    // Флаги для отправки и доставки SMS
    private String SENT_SMS_FLAG = "SENT_SMS";
    private String DELIVER_SMS_FLAG = "DELIVER_SMS";

    private PendingIntent sentPIn = null;
    private PendingIntent deliverPIn = null;

    // максимальное количество отправляемых сообщений для этого сервиса
    private final String LOG_TAG = "Send SMS Service";
    private int maxSMSIndex = 100;

    // Для передачи данных обратно приложению
    Intent intentApp;

    // Список телефонных номеров и текст сообщения
    private String[] numList = null;
    private String smsText = null;
    private String currentPhoneNumber;

    private int currentSMSNumberIndex = 0;

    private int tryCountSendSMS = 1;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SendSMSIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SendSMSIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

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
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
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
            // Завершаем сервис если отправили максимальное количество СМС )
            if (currentSMSNumberIndex >= maxSMSIndex) {
                sendDataToApp("SMSSenderServiceStatus", "endtask", "end");
                stopSelf();
            }
            // Увеличиваем счетчик для номеров телефонов в списке
            currentSMSNumberIndex++;

            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Log.d(LOG_TAG, "Сообщение отправлено!");
                    //currentSMSNumberIndex++;
                    sendSMS();
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
