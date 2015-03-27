package ru.dsoft38.sms_sender;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Created by user on 27.03.2015.
 */
public class SentMessages {

    private int iDay = 1;
    private int sentSMSCount = 0;
    private Calendar calendar;
    public int MaxSMSCountSend = 100;

    private SharedPreferences sp;
    private  Context context;

    public SentMessages (Context context){
        this.context = context;
        // Если текущий день года не равен записаному. сбрасываем счетчик СМС
        calendar = Calendar.getInstance();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        //Object i = calendar.get(java.util.Calendar.DAY_OF_YEAR);
        iDay = sp.getInt("DAY_OF_YEAR", 1);

        if(iDay != calendar.get(java.util.Calendar.DAY_OF_YEAR)){
            resetSentSMSCount();
        }
    }

    // Получаем количество отправленных СМС
    public int getSentSMSCount(){
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        sentSMSCount = sp.getInt("sentSMSCount", 0);

        return sentSMSCount;
    }

    // Записываем отправленное количество СМС
    public void setSentSMSCount(int count){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("DAY_OF_YEAR", calendar.get(Calendar.DAY_OF_YEAR));
        editor.putInt("sentSMSCount", count);
        editor.commit();
    }

    // Добавляем отправленное количество СМС
    public void addSentSMSCount(int count){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("DAY_OF_YEAR", calendar.get(Calendar.DAY_OF_YEAR));
        editor.putInt("sentSMSCount", sentSMSCount + count);
        editor.commit();
    }

    // Сбрасываем количество отправленных СМС
    public void resetSentSMSCount(){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("sentSMSCount", 0);
        //editor.putInt("iMount", calendar.get(java.util.Calendar.MONTH));
        editor.putInt("DAY_OF_YEAR", calendar.get(Calendar.DAY_OF_YEAR));
        editor.commit();
    }

    // Получаем количество осташихся СМС, которые можно отправить исходя из лимита
    public int getFreeSMSCount(){
        int freeSMSCount = MaxSMSCountSend - getSentSMSCount();

        if ( freeSMSCount > 0 ) {
            return freeSMSCount;
        } else {
            return 0;
        }
    }
}
