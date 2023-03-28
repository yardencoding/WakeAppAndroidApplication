package com.example.wakeup;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;

    private ArrayList<Alarm> alarms;
    private final int[] daysRgbValues = {255, 182, 193};
    private final int checkedDays_TextViewSize = 18;


    public AlarmAdapter(ArrayList<Alarm> alarms, RecyclerViewInterface recyclerViewInterface) {
        this.alarms = alarms;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView alarmNameTextView;
        TextView alarmTimeTextView;
        TextView alarmMissionTextView;
        TextView sundayTextView, mondayTextView, tuesdayTextView, wednesdayTextView, thursdayTextView, fridayTextView, saturdayTextView;
        Switch alarmSwitch;
        ConstraintLayout cardViewLayout;


        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmNameTextView = itemView.findViewById(R.id.alarm_name_text_view);
            alarmTimeTextView = itemView.findViewById(R.id.alarm_time_text_view);
            alarmMissionTextView = itemView.findViewById(R.id.alarm_mission_text_view);
            alarmSwitch = itemView.findViewById(R.id.alarm_switch);
            sundayTextView = itemView.findViewById(R.id.sunday_text_view);
            mondayTextView = itemView.findViewById(R.id.monday_text_view);
            tuesdayTextView = itemView.findViewById(R.id.tuesday_text_view);
            wednesdayTextView = itemView.findViewById(R.id.wednesday_text_view);
            thursdayTextView = itemView.findViewById(R.id.thursday_text_view);
            fridayTextView = itemView.findViewById(R.id.friday_text_view);
            saturdayTextView = itemView.findViewById(R.id.saturday_text_view);
            cardViewLayout = itemView.findViewById(R.id.card_view_layout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            recyclerViewInterface.onItemClick(position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (recyclerViewInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            recyclerViewInterface.onItemLongClick(position);
                    }
                    return true;
                }
            });

        }
    }


    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View alarmView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.alarm_recycler_item, parent, false);
        return new AlarmViewHolder(alarmView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);

        holder.alarmNameTextView.setText(alarm.getName());
        holder.alarmTimeTextView.setText(String.format("%02d:%02d", alarm.getHour(), alarm.getMinute()));
        if (alarm.hasMission())
            holder.alarmMissionTextView.setText(alarm.getMission());

        // mark alarm days if the alarm is recurring
        if (alarm.isRecurring()) {
            if (alarm.isSunday()) markSunday(holder);
            if (alarm.isMonday()) markMonday(holder);
            if (alarm.isTuesday()) markTuesday(holder);
            if (alarm.isWednesday()) markWednesday(holder);
            if (alarm.isThursday()) markThursday(holder);
            if (alarm.isFriday()) markFriday(holder);
            if (alarm.isSaturday()) markSaturday(holder);

        } else {
            showAlarmTime_WhenTheAlarm_IsNotRecurring(holder, alarm);
        }

        if (alarm.isActive()) { //turn on Alarm switch, if alarm is Active
            holder.alarmSwitch.setChecked(true);
        } else { // change alarm background color to gray, if Inactive.
            holder.cardViewLayout.setBackgroundResource(R.color.inactiveAlarm);
            holder.alarmSwitch.setChecked(false);
        }


        // change cardView background color based on the switch,
        // and the active state in data base
        holder.alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (!isChecked) {
                    holder.cardViewLayout.setBackgroundResource(R.color.inactiveAlarm);
                    DataBaseHelper.database.changeAlarmActiveState(false, alarm);
                    alarm.setActive(false);
                    alarm.cancel(holder.itemView.getContext());
                } else {
                    holder.cardViewLayout.setBackgroundResource(R.color.recyclerViewItemColor);
                    DataBaseHelper.database.changeAlarmActiveState(true, alarm);
                    alarm.setActive(true);

                    //Update the alarm to the next day if the time had passed.
                    alarm.removeDays();
                    alarm.whenNoDay_WasChosen();
                    showAlarmTime_WhenTheAlarm_IsNotRecurring(holder, alarm);
                    //Update the alarm so that it will have the updated day
                    DataBaseHelper.database.changeAlarmSettings(alarm.getId(), alarm);


                    alarm.schedule(holder.itemView.getContext());
                }

            }
        });

    }

    private void markSunday(AlarmViewHolder holder) {
        holder.sundayTextView.setTextSize(checkedDays_TextViewSize);
        holder.sundayTextView.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
    }

    private void markMonday(AlarmViewHolder holder) {
        holder.mondayTextView.setTextSize(checkedDays_TextViewSize);
        holder.mondayTextView.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
    }

    private void markTuesday(AlarmViewHolder holder) {
        holder.tuesdayTextView.setTextSize(checkedDays_TextViewSize);
        holder.tuesdayTextView.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
    }

    private void markWednesday(AlarmViewHolder holder) {
        holder.wednesdayTextView.setTextSize(checkedDays_TextViewSize);
        holder.wednesdayTextView.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
    }

    private void markThursday(AlarmViewHolder holder) {
        holder.thursdayTextView.setTextSize(checkedDays_TextViewSize);
        holder.thursdayTextView.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
    }

    private void markFriday(AlarmViewHolder holder) {
        holder.fridayTextView.setTextSize(checkedDays_TextViewSize);
        holder.fridayTextView.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
    }

    private void markSaturday(AlarmViewHolder holder) {
        holder.saturdayTextView.setTextSize(checkedDays_TextViewSize);
        holder.saturdayTextView.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
    }

    private void showAlarmTime_WhenTheAlarm_IsNotRecurring(AlarmViewHolder holder, Alarm alarm) {
        LocalDateTime localDateTime;
        localDateTime = alarm.getAlarmLocalDateTime();
        holder.sundayTextView.setText(localDateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("he-IL"))
                + ", " + localDateTime.getDayOfMonth() + "/" + localDateTime.getMonthValue()
        );

        holder.mondayTextView.setText("");
        holder.tuesdayTextView.setText("");
        holder.wednesdayTextView.setText("");
        holder.thursdayTextView.setText("");
        holder.fridayTextView.setText("");
        holder.saturdayTextView.setText("");

    }


    @Override
    public int getItemCount() {
        return alarms.size();
    }


}

