package com.example.wakeup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;


public class DataBaseHelper extends SQLiteOpenHelper {


    //ALARM TABLE
    private static final String ALARM_TABLE = "ALARM_TABLE";
    private static final String COLUMN_ALARM_ID = "_ID";
    private static final String COLUMN_ALARM_ACTIVE = "ACTIVE";
    private static final String COLUMN_ALARM_HOUR = "HOUR";
    private static final String COLUMN_ALARM_MINUTE = "MINUTE";
    private static final String COLUMN_ALARM_NAME = "NAME";
    private static final String COLUMN_ALARM_MISSION = "MISSION";
    private static final String COLUMN_ALARM_SOUND_NAME = "SOUND_NAME";
    private static final String COLUMN_ALARM_SUNDAY = "SUNDAY";
    private static final String COLUMN_ALARM_MONDAY = "MONDAY";
    private static final String COLUMN_ALARM_TUESDAY = "TUESDAY";
    private static final String COLUMN_ALARM_WEDNESDAY = "WEDNESDAY";
    private static final String COLUMN_ALARM_THURSDAY = "THURSDAY";
    private static final String COLUMN_ALARM_FRIDAY = "FRIDAY";
    private static final String COLUMN_ALARM_SATURDAY = "SATURDAY";
    private static final String COLUMN_ALARM_HAS_SOUND = "HAS_SOUND";
    private static final String COLUMN_ALARM_HAS_VIBRATE = "HAS_VIBRATE";
    private static final String COLUMN_ALARM_HAS_MISSION = "HAS_MISSION";
    private static final String COLUMN_ALARM_HAS_CONTACTS = "HAS_CONTACTS";

    private static final String COLUMN_ALARM_IS_RECURRING = "IS_RECURRING";


    public static DataBaseHelper database;


    public DataBaseHelper(@Nullable Context context) {
        super(context, "AlarmsDatabase.db", null, 1);
    }

    // This is called the first time a database is accessed
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createAlarmTableCommand =
                "CREATE TABLE "
                        + ALARM_TABLE +
                        " (" +
                        COLUMN_ALARM_ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_ALARM_ACTIVE + " BOOL, "
                        + COLUMN_ALARM_HOUR + " INT, "
                        + COLUMN_ALARM_MINUTE + " INT, "
                        + COLUMN_ALARM_NAME + " TEXT, "
                        + COLUMN_ALARM_MISSION + " TEXT, "
                        + COLUMN_ALARM_SOUND_NAME + " TEXT, "
                        + COLUMN_ALARM_SUNDAY + " BOOL, "
                        + COLUMN_ALARM_MONDAY + " BOOL, "
                        + COLUMN_ALARM_TUESDAY + " BOOL, "
                        + COLUMN_ALARM_WEDNESDAY + " BOOL, "
                        + COLUMN_ALARM_THURSDAY + " BOOL, "
                        + COLUMN_ALARM_FRIDAY + " BOOL, "
                        + COLUMN_ALARM_SATURDAY + " BOOL, "
                        + COLUMN_ALARM_HAS_SOUND + " BOOL, "
                        + COLUMN_ALARM_HAS_VIBRATE + " BOOL, "
                        + COLUMN_ALARM_HAS_MISSION + " BOOL, "
                        + COLUMN_ALARM_HAS_CONTACTS + " BOOL, "
                        + COLUMN_ALARM_IS_RECURRING + " BOOL"
                        + ");";


        sqLiteDatabase.execSQL(createAlarmTableCommand);

    }

    // This is called if the database version number changes
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void addAlarmToDataBase(Alarm newAlarm) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.insert(ALARM_TABLE, null, getAllContentValues(newAlarm));
    }


    public ArrayList<Alarm> getAllAlarmsFromDataBase() {
        ArrayList<Alarm> returnList = new ArrayList<>();
        String getAlarmsCommand = "SELECT * FROM " + ALARM_TABLE;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(getAlarmsCommand, null);

        // To move the cursor reference to the first row. In order to access data.
        if (cursor.moveToFirst()) {

            do {


                boolean isActive = cursor.getInt(1) == 1;
                int hour = cursor.getInt(2);
                int minute = cursor.getInt(3);
                String name = cursor.getString(4);
                String mission = cursor.getString(5);
                String soundName = cursor.getString(6);
                boolean sunday = cursor.getInt(7) == 1;
                boolean monday = cursor.getInt(8) == 1;
                boolean tuesday = cursor.getInt(9) == 1;
                boolean wednesday = cursor.getInt(10) == 1;
                boolean thursday = cursor.getInt(11) == 1;
                boolean friday = cursor.getInt(12) == 1;
                boolean saturday = cursor.getInt(13) == 1;
                boolean hasSound = cursor.getInt(14) == 1;
                boolean hasVibrate = cursor.getInt(15) == 1;
                boolean hasMission = cursor.getInt(16) == 1;
                boolean hasContacts = cursor.getInt(17) == 1;
                boolean isRecurring = cursor.getInt(18) == 1;


                Alarm newAlarm = new Alarm(
                        isActive,
                        hour,
                        minute,
                        name,
                        mission,
                        soundName,
                        sunday,
                        monday,
                        tuesday,
                        wednesday,
                        thursday,
                        friday,
                        saturday,
                        hasSound,
                        hasVibrate,
                        hasMission,
                        hasContacts,
                        isRecurring
                );

                //Get id
                newAlarm.setId(cursor.getInt(0));

                returnList.add(newAlarm);

            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return returnList;
    }

    public void changeAlarmActiveState(boolean newState, Alarm alarm) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ALARM_ACTIVE, newState);
        database.update(ALARM_TABLE, contentValues, COLUMN_ALARM_ID + " = " + alarm.getId(), null);

        database.close();
    }

    public void deleteAlarm(Alarm alarm) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(ALARM_TABLE, COLUMN_ALARM_ID + " = " + alarm.getId(), null);
        database.close();
    }

    public void changeAlarmSettings(int id, Alarm newAlarm) {

        SQLiteDatabase database = this.getWritableDatabase();
        database.update(ALARM_TABLE, getAllContentValues(newAlarm), COLUMN_ALARM_ID + " = " + id, null);
        database.close();

    }


    private ContentValues getAllContentValues(Alarm newAlarm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ALARM_ACTIVE, newAlarm.isActive());
        contentValues.put(COLUMN_ALARM_HOUR, newAlarm.getHour());
        contentValues.put(COLUMN_ALARM_MINUTE, newAlarm.getMinute());
        contentValues.put(COLUMN_ALARM_NAME, newAlarm.getName());
        contentValues.put(COLUMN_ALARM_MISSION, newAlarm.getMission());
        contentValues.put(COLUMN_ALARM_SOUND_NAME, newAlarm.getSoundName());
        contentValues.put(COLUMN_ALARM_SUNDAY, newAlarm.isSunday());
        contentValues.put(COLUMN_ALARM_MONDAY, newAlarm.isMonday());
        contentValues.put(COLUMN_ALARM_TUESDAY, newAlarm.isTuesday());
        contentValues.put(COLUMN_ALARM_WEDNESDAY, newAlarm.isWednesday());
        contentValues.put(COLUMN_ALARM_THURSDAY, newAlarm.isThursday());
        contentValues.put(COLUMN_ALARM_FRIDAY, newAlarm.isFriday());
        contentValues.put(COLUMN_ALARM_SATURDAY, newAlarm.isSaturday());
        contentValues.put(COLUMN_ALARM_HAS_SOUND, newAlarm.hasSound());
        contentValues.put(COLUMN_ALARM_HAS_VIBRATE, newAlarm.hasVibrate());
        contentValues.put(COLUMN_ALARM_HAS_MISSION, newAlarm.hasMission());
        contentValues.put(COLUMN_ALARM_HAS_CONTACTS, newAlarm.hasUseMyContacts());
        contentValues.put(COLUMN_ALARM_IS_RECURRING, newAlarm.isRecurring());


        return contentValues;
    }


}

