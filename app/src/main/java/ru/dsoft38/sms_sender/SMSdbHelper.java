package ru.dsoft38.sms_sender;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by diesel on 12.05.2015.
 */
public class SMSdbHelper extends SQLiteOpenHelper implements BaseColumns {

    // константы дл€ конструктора
    private static final String DATABASE_NAME = "sms_database.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "plugin_history_table";
    public static final String PLUGIN = "plugin";
    public static final String SENDTIMESTAMP = "smstime";

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + TABLE_NAME + " ( INTEGER PRIMARY KEY AUTOINCREMENT,"
            + PLUGIN + " VARCHAR(255)),"
            + SENDTIMESTAMP + " VARCHAR(19));";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    public SMSdbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("LOG_TAG", "ќбновление базы данных с версии " + oldVersion
                + " до версии " + newVersion + ", которое удалит все старые данные");
        // ”дал€ем предыдущую таблицу при апгрейде
        db.execSQL(SQL_DELETE_ENTRIES);
        // —оздаЄм новый экземпл€р таблицы
        onCreate(db);
    }
}
