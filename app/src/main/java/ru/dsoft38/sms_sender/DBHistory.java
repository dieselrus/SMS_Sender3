package ru.dsoft38.sms_sender;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by diesel on 11.06.2015.
 */
public class DBHistory {
    private SMSDataBaseHelper sqlHelper;
    private SQLiteDatabase sdb;

    // �������� ���� ������
    public void Create(Context context){
        // �������������� ��� �����-������
        sqlHelper = new SMSDataBaseHelper(context, null, null, 1);

        // ���� ��� ����� ��� ������ � ������
        sdb = sqlHelper.getWritableDatabase();
    }

    // ����� ������ � ������� �������� ���
    public void setCurrentSMSAndHash(String pluginName, long currentTime, int smsCount, String FILE_MD5_SUMM){
        // count miliseconds from 01.01.1970
        String insertQuery = "INSERT INTO `" + sqlHelper.TABLE_NAME
                + "` (`" + sqlHelper.PLUGIN_NAME + "`, `" + sqlHelper.SENT_TIME + "`) VALUES ('"
                +  pluginName + "','" + currentTime + "')";

        sdb.execSQL(insertQuery);

        sdb.execSQL("DELETE FROM `resume_send_table`;");

        String insertQueryResumeSend = "INSERT INTO `resume_send_table` (`current_sms`, `md5hash`) VALUES ('" + smsCount + "', '" + FILE_MD5_SUMM + "');";
        sdb.execSQL(insertQueryResumeSend);
    }

    // ����� ���������� ������������� ��� (����� �����)
    public int getSMSSentCount(){
        Cursor cursor2 = sdb.rawQuery("SELECT `current_sms` FROM `resume_send_table`;", null);

        int index = 0;

        while (cursor2.moveToNext()) {
            index = cursor2.getInt(cursor2.getColumnIndex("current_sms"));
        }
        cursor2.close();

        return  index;
    }

    // ������� ������� ������� ��� �����������.
    public void clearResumeTable(){
        sdb.execSQL("DELETE FROM `resume_send_table`;");
    }

    // ������� ������� ����� � ������ ������� � ��� ����� � ��������, ��� ����������� ��������
    public void setCurrentNumberAndHash(int smsCount, String FILE_MD5_SUMM){
        String insertQueryResumeSend = "INSERT INTO `resume_send_table` (`current_sms`, `md5hash`) VALUES ('" + smsCount + "', '" + FILE_MD5_SUMM + "');";
        sdb.execSQL(insertQueryResumeSend);
    }

    public void closeConnectDB(){
        sdb.close();
        sqlHelper.close();
    }

    /** ��������� ���������� ������������ ��� �� ����� �����������
     * @param pluginName - ��� ������� ��� ������
     * @return
     */
    public int getSentSMSCountPluIn(String pluginName, long currentTime, long lTimeSMSLimitForApp){
        int count = 0;

        String query = "SELECT COUNT(" + sqlHelper.PLUGIN_NAME + ") AS count FROM "
                + sqlHelper.TABLE_NAME + " WHERE "
                + sqlHelper.PLUGIN_NAME + " = '" + pluginName
                + "' AND " + sqlHelper.SENT_TIME + " > "
                + currentTime + "-" + lTimeSMSLimitForApp
                + ";";

        Cursor cursor2 = sdb.rawQuery(query, null);

        while (cursor2.moveToNext()) {
            count = cursor2.getInt(cursor2.getColumnIndex("count"));
        }
        cursor2.close();

        return count;
    }
}