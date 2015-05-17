package ru.dsoft38.sms_sender;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by diesel on 17.05.15.
 */
public class SMSDataBaseHelper extends SQLiteOpenHelper implements BaseColumns {

    // константы для конструктора
    private static final String DATABASE_NAME = "sms_database.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "plugin_sent_table";
    public static final String PLUGIN_NAME = "plugin_name";
    public static final String SENT_TIME = "sent_time";

    //private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
    //        + TABLE_NAME + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    //        + CATNAME + " VARCHAR(255));";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE `"
            + TABLE_NAME + "` (`" + PLUGIN_NAME + "`	TEXT, `" + SENT_TIME + "`	INTEGER);";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    public SMSDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w("LOG_TAG", "Обновление базы данных с версии " + oldVersion
                + " до версии " + newVersion + ", которое удалит все старые данные");
        // Удаляем предыдущую таблицу при апгрейде
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        // Создаём новый экземпляр таблицы
        onCreate(sqLiteDatabase);
    }
}
