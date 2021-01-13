package com.ct.xps_custom;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class NotificationHelper {
    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "customnotifs.db";
    private static String TABLE_PT = "customnotifs";
    private static String TABLE_PT_COLUMN_ID = "id";
    private static String TABLE_PT_COLUMN_PTID = "pt_id";
    private long EXECUTOR_THREAD_ID = 0;
    private ExecutorService es;
    private DBHelper dbHelper;
    private static NotificationHelper notificationHelper = null;
    private NotificationHelper(Context context) {
        this.es = Executors.newFixedThreadPool(1);
        this.dbHelper = new DBHelper(context);
    }
    static NotificationHelper getInstance(Context context) {
        if (notificationHelper == null) {
            notificationHelper = new NotificationHelper(context);
        }
        return notificationHelper;
    }
    void postAsyncSafely(final String name, final Runnable runnable) {
        try {
            final boolean executeSync = Thread.currentThread().getId() == EXECUTOR_THREAD_ID;
            if (executeSync) {
                runnable.run();
            } else {
                this.es.submit(new Runnable() {
                    @Override
                    public void run() {
                        EXECUTOR_THREAD_ID = Thread.currentThread().getId();
                        try {
                            runnable.run();
                        } catch (Throwable t) {
                            Log.e("NotificationHelper","Executor service: Failed to complete the scheduled task" + name);
                        }
                    }
                });
            }
        } catch (Throwable t) {
            Log.e("NotificationHelper","Failed to submit task to the executor service");
        }
    }
    boolean isNotificationPresent(Bundle extras) {
        String ptId = extras.getString("wzrk_pid");
        if(ptId == null || ptId.isEmpty()) return false;
        return dbHelper.isNotificationPresentInDB(ptId);
    }
    void saveNotification(Bundle extras) {
        String ptId = extras.getString("wzrk_pid");
        if(ptId == null || ptId.isEmpty()) return;
        dbHelper.savePT(ptId);
    }
    private class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = "" + "CREATE TABLE " + TABLE_PT + " (" +
                    TABLE_PT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                    ", " + TABLE_PT_COLUMN_PTID + " TEXT " +
                    ");";
            SQLiteStatement stmt = db.compileStatement(query);
            stmt.execute();
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PT);
            onCreate(db);
        }
        public void savePT(String ptID) {
            SQLiteDatabase db = getWritableDatabase();
            String sql = "INSERT INTO " + TABLE_PT + " ( " +TABLE_PT_COLUMN_PTID + " ) VALUES ( ? )";
            try {
                db.beginTransactionNonExclusive();
                SQLiteStatement stmt = db.compileStatement(sql);
                stmt.bindString(1, ptID);
                stmt.execute();
                stmt.clearBindings();
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception e) {
                Log.e("DBHelper", Arrays.toString(e.getStackTrace()));
            } finally {
                db.close();
            }
        }
        boolean isNotificationPresentInDB(String ptID) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_PT,null,TABLE_PT_COLUMN_PTID+ " =?",new String[]{ptID},null,null,null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (cursor.getLong(cursor.getColumnIndex(TABLE_PT_COLUMN_ID)) != 0) {
                    if(cursor.getString(cursor.getColumnIndex(TABLE_PT_COLUMN_PTID)).equalsIgnoreCase(ptID)) {
                        cursor.close();
                        db.close();
                        return true;
                    }
                }
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
            return false;
        }
    }
}
