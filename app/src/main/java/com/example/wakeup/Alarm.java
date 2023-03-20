package com.example.wakeup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

// implements Parcelable, so that we can send our alarmList between activities
public class Alarm implements Parcelable {


    //Alarm fields
    private int id;
    private String name;
    private String mission, soundName;
    private int hour;
    private int minute;
    private boolean active;
    private boolean sunday, monday, tuesday, wednesday, thursday, friday, saturday;
    private boolean hasSound, hasVibrate, hasMission, hasUseMyContacts;

    private boolean isRecurring;


    // initialize an alarm with id, name, mission, hour, minute and which days it will run
    public Alarm(boolean active, int hour, int minute, String name, String mission, String soundName,
                 boolean sunday,
                 boolean monday,
                 boolean tuesday,
                 boolean wednesday,
                 boolean thursday,
                 boolean friday,
                 boolean saturday,
                 boolean hasSound,
                 boolean hasVibrate,
                 boolean hasMission,
                 boolean hasUseMyContacts,
                 boolean isRecurring
    ) {


        this.active = active;
        this.hour = hour;
        this.minute = minute;
        this.name = name;
        this.mission = mission;
        this.soundName = soundName;
        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.hasSound = hasSound;
        this.hasVibrate = hasVibrate;
        this.hasMission = hasMission;
        this.hasUseMyContacts = hasUseMyContacts;
        this.isRecurring = isRecurring;

    }

    // Sometimes an empty constructor is needed.
    public Alarm() {
    }


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMission() {
        return mission;
    }

    public int getHour() {
        return hour;
    }


    public int getMinute() {
        return minute;
    }

    public boolean isActive() {
        return active;
    }


    public boolean isSunday() {
        return sunday;
    }

    public boolean isMonday() {
        return monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public boolean isFriday() {
        return friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public boolean hasSound() {
        return hasSound;
    }

    public boolean hasVibrate() {
        return hasVibrate;
    }

    public boolean hasMission() {
        return hasMission;
    }

    public String getSoundName() {
        return soundName;
    }

    public void setSoundName(String soundName) {
        this.soundName = soundName;
    }

    public boolean hasUseMyContacts() {
        return hasUseMyContacts;
    }

    public void setHasSound(boolean hasSound) {
        this.hasSound = hasSound;
    }

    public void setHasVibrate(boolean hasVibrate) {
        this.hasVibrate = hasVibrate;
    }

    public void setHasMission(boolean hasMission) {
        this.hasMission = hasMission;
    }

    public void setHasUseMyContacts(boolean hasUseMyContacts) {
        this.hasUseMyContacts = hasUseMyContacts;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }


    // Alarm methods:

    //Returns an Integer value that represent alarm's closest day.
    public DayOfWeek getClosestToCurrentDay() {
        LocalDateTime localDateTime = LocalDateTime.now();
        boolean[] daysArray = getDaysArray();
        // WeekFields.SUNDAY_START because the default first day is Monday, and here in Israel we start at sunday.
        int index = localDateTime.getDayOfWeek().get(WeekFields.SUNDAY_START.dayOfWeek());

        //if alarm's time passed increment the day by 1.
        if (timeHasAlreadyPassed()) {
            localDateTime = localDateTime.plusDays(1);
            index = localDateTime.getDayOfWeek().get(WeekFields.SUNDAY_START.dayOfWeek());
        }

        while (daysArray[index] == false) {
            //Move to the next day
            localDateTime = localDateTime.plusDays(1);
            index = localDateTime.getDayOfWeek().get(WeekFields.SUNDAY_START.dayOfWeek());
        }
        return localDateTime.getDayOfWeek();
    }

    private boolean[] getDaysArray() {
        // Boolean.False for the first element so the days will be from 1 to 7.
        return new boolean[]{Boolean.FALSE, isSunday(), isMonday(), isTuesday(), isWednesday(), isThursday(),
                isFriday(), isSaturday()};
    }


    // Returns true if no day was chosen.
    public boolean hasNoChosenDay() {
        return !isSunday() && !isMonday() && !isTuesday() && !isWednesday() &&
                !isThursday() && !isFriday() && !isSaturday();
    }

    // Returns true if alarm's time had already passed
    private boolean timeHasAlreadyPassed() {
        LocalDateTime alarmTime = LocalDateTime.now();
        alarmTime = alarmTime.withHour(getHour());
        alarmTime = alarmTime.withMinute(getMinute());
        return LocalDateTime.now().isAfter(alarmTime);
    }

    // Returns alarm day when no day was chosen, if alarm time has already passed increment  the day by one.
    public void whenNoDay_WasChosen() {
        LocalDateTime alarmTime = LocalDateTime.now();
        if (timeHasAlreadyPassed()) {
            // Increment alarm day of week by 1, if the alarm's time has passed.
            alarmTime = alarmTime.plusDays(1);
        }
        setAlarmDay_manually(alarmTime.getDayOfWeek());
    }


    // Add the alarm day to the corresponding day attribute.
    private void setAlarmDay_manually(DayOfWeek dayOfWeek) {
        if (dayOfWeek == DayOfWeek.SUNDAY)
            setSunday(true);
        if (dayOfWeek == DayOfWeek.MONDAY)
            setMonday(true);
        if (dayOfWeek == DayOfWeek.TUESDAY)
            setTuesday(true);
        if (dayOfWeek == DayOfWeek.WEDNESDAY)
            setWednesday(true);
        if (dayOfWeek == DayOfWeek.THURSDAY)
            setThursday(true);
        if (dayOfWeek == DayOfWeek.FRIDAY)
            setFriday(true);
        if (dayOfWeek == DayOfWeek.SATURDAY)
            setSaturday(true);
    }


    public static void sortAlarms(ArrayList<Alarm> alarmList) {
        Collections.sort(alarmList, new Comparator<Alarm>() {
            @Override
            public int compare(Alarm firstAlarm, Alarm secondAlarm) {

                LocalDateTime firstAlarmTime = firstAlarm.getAlarmLocalDateTime();
                LocalDateTime secondAlarmTime = secondAlarm.getAlarmLocalDateTime();

                /*
                This method should return:
                 .-1 if the firstAlarm is closer than the secondAlarm.
                 .+1 if the secondAlarm is closer than the firstAlarm.
                 .0 if they are at the same time.
                 */

                if (firstAlarmTime.isBefore(secondAlarmTime))
                    return -1;
                else if (secondAlarmTime.isBefore(firstAlarmTime))
                    return 1;
                else
                    return 0;

                // The closest alarm will always be on top.
            }
        });
    }

    //Iterates over the entire alarm list and checks if the alarm that called this method is equal to one of them.
    //By equal I mean has the same time and at least one matching day
    public boolean alreadyExist(ArrayList<Alarm> alarms) {
        for (int i = 0; i < alarms.size(); i++) {
            if (this.equals(alarms.get(i))) {
                return true;
            }
        }
        return false;
    }

    public LocalDateTime getAlarmLocalDateTime() {
        LocalDateTime alarmLocalDateTime = LocalDateTime.now();
        //if the time had already passed change the day to the next week
        //otherwise keep it this week.
        if (timeHasAlreadyPassed()) {
            alarmLocalDateTime = alarmLocalDateTime.with(TemporalAdjusters.next(getClosestToCurrentDay()));
        } else {
            alarmLocalDateTime = alarmLocalDateTime.with(TemporalAdjusters.nextOrSame(getClosestToCurrentDay()));
        }

        alarmLocalDateTime = alarmLocalDateTime.withHour(getHour());
        alarmLocalDateTime = alarmLocalDateTime.withMinute(getMinute());
        return alarmLocalDateTime;
    }


    public void setActive(boolean active) {
        this.active = active;
    }


    public String getHowMuchTimeTillAlarm() {

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime alarmTime = getAlarmLocalDateTime();

        long differenceDays, differenceHours, differenceMinutes;

        differenceHours = ChronoUnit.HOURS.between(currentTime, alarmTime);

        if (differenceHours <= 23) {
            differenceDays = 0;
        } else {
            //Only get the days difference. without the hour and minute impact.
            differenceDays = ChronoUnit.DAYS.between(currentTime,
                    alarmTime.withHour(currentTime.getHour()).withMinute(currentTime.getMinute()));
        }


        /*
         Add the differenceHours to currentTime, So the differenceMinutes will be smaller than 60.
         Example: if the currentTime is 17:00 And the alarm time is 19:30
         the difference minutes will be 150 and we don't want that,
         so we add the differenceHours to the currentTime get 19:00,
         and now we can get the differenceMinutes which is 30.
         */

        differenceMinutes = ChronoUnit.MINUTES.between(
                currentTime.plusHours(differenceHours), alarmTime);


        String s1 = "התראה בעוד ";


        if (differenceDays > 0) {
            if (differenceDays > 1)
                return s1 + differenceDays + " " + "ימים";
            else
                return s1 + differenceDays + " " + "יום";
        } else {

            if (differenceHours == 0) {
                if (differenceMinutes > 1)
                    return s1 + differenceMinutes + " " + "דקות";
                else
                    return s1 + "פחות מדקה";
            }

            if (differenceMinutes == 0) {
                if (differenceHours > 1)
                    return s1 + differenceHours + " " + "שעות";
                else
                    return s1 + differenceHours + " " + "שעה";
            }
            return s1 + differenceHours + " " + "שעות" + " " + "ו-" + " " + differenceMinutes + " " + "דקות";
        }
    }


    // Returns the first active alarm. If all the alarms are inactive in will return null.
    public static Alarm getFirstActiveAlarm() {
        ArrayList<Alarm> alarms = DataBaseHelper.database.getAllAlarmsFromDataBase();

        //Beacause they are not sorted in the data base. And we want the closest one to appear
        sortAlarms(alarms);

        Alarm firstActive = null;
        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).isActive()) {
                firstActive = alarms.get(i);
                break;
            }
        }
        return firstActive;
    }

    public void schedule(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmToBroadcastReceiver", this);
        long milliseconds = this.getAlarmLocalDateTime().withSecond(0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, this.id, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(milliseconds, pendingIntent);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);


    }

    public void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, this.id, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

    }


    // Returns true if two alarms has the same Hour, Minute and at least one matching day.
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Alarm alarm = (Alarm) object;
        if (this.getHour() == alarm.getHour()) {
            if (this.getMinute() == alarm.getMinute()) {
                if (this.sunday && alarm.isSunday()
                        || this.monday && alarm.isMonday()
                        || this.tuesday && alarm.isTuesday()
                        || this.wednesday && alarm.isWednesday()
                        || this.thursday && alarm.isThursday()
                        || this.friday && alarm.isFriday()
                        || this.saturday && alarm.isSaturday())
                    return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, minute, sunday, monday, tuesday, wednesday, thursday, friday, saturday);
    }


    //Parcelable implementation

    protected Alarm(Parcel in) {
        id = in.readInt();
        name = in.readString();
        mission = in.readString();
        soundName = in.readString();
        hour = in.readInt();
        minute = in.readInt();
        sunday = in.readByte() != 0;
        monday = in.readByte() != 0;
        tuesday = in.readByte() != 0;
        wednesday = in.readByte() != 0;
        thursday = in.readByte() != 0;
        friday = in.readByte() != 0;
        saturday = in.readByte() != 0;
        hasSound = in.readByte() != 0;
        hasVibrate = in.readByte() != 0;
        hasMission = in.readByte() != 0;
        hasUseMyContacts = in.readByte() != 0;
        isRecurring = in.readByte() != 0;


    }

    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(mission);
        parcel.writeString(soundName);
        parcel.writeInt(hour);
        parcel.writeInt(minute);
        parcel.writeByte((byte) (sunday ? 1 : 0));
        parcel.writeByte((byte) (monday ? 1 : 0));
        parcel.writeByte((byte) (tuesday ? 1 : 0));
        parcel.writeByte((byte) (wednesday ? 1 : 0));
        parcel.writeByte((byte) (thursday ? 1 : 0));
        parcel.writeByte((byte) (friday ? 1 : 0));
        parcel.writeByte((byte) (saturday ? 1 : 0));
        parcel.writeByte((byte) (hasSound ? 1 : 0));
        parcel.writeByte((byte) (hasVibrate ? 1 : 0));
        parcel.writeByte((byte) (hasMission ? 1 : 0));
        parcel.writeByte((byte) (hasUseMyContacts ? 1 : 0));
        parcel.writeByte((byte) (isRecurring ? 1 : 0));

    }


    @Override
    public String toString() {

        String time = String.format("%02d:%02d", getHour(), getMinute());
        String s1 = "התראה לשעה";

        if (!this.isRecurring) { // If the alarm is not recurring
            int dayInMonth = getAlarmLocalDateTime().getDayOfMonth();


            String s2 = "בתאריך";
            int month = LocalDateTime.now().getMonthValue();
            return s1 + " " + time + " " + s2 + " " + dayInMonth + "/" + month;


        } else { // If alarm is recurring

            // More efficient because it dose not create a new String in memory each time we change the text
            StringBuilder daysStringBuilder = new StringBuilder();
            if (isSunday())
                daysStringBuilder.append("א', ");
            if (isMonday())
                daysStringBuilder.append("ב', ");
            if (isTuesday())
                daysStringBuilder.append("ג', ");
            if (isWednesday())
                daysStringBuilder.append("ד', ");
            if (isThursday())
                daysStringBuilder.append("ה', ");
            if (isFriday())
                daysStringBuilder.append("ו', ");
            if (isSaturday())
                daysStringBuilder.append("ש'");

            String s2 = "מדי יום ";
            return s1 + " " + time + " " + s2 + " " + daysStringBuilder;

        }
    }


}
