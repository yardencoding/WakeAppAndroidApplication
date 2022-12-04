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
import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;

    private ArrayList<Alarm> alarms;
    private final int[] daysRgbValues = {255, 182, 193};
    private final int checkedDays_TextViewSize = 20;


    public AlarmAdapter(ArrayList<Alarm> alarms, RecyclerViewInterface recyclerViewInterface) {
        this.alarms = alarms;
        this.recyclerViewInterface = recyclerViewInterface;
    }

     class AlarmViewHolder extends RecyclerView.ViewHolder {
         TextView alarm_name;
         TextView alarm_time;
         TextView alarm_mission;
         TextView sunday, monday, tuesday, wednesday, thursday, friday, saturday;
         Switch alarm_switch;
         ConstraintLayout cardViewLayout;


         public AlarmViewHolder(@NonNull View itemView) {
             super(itemView);
             alarm_name = itemView.findViewById(R.id.alarm_name_tv);
             alarm_time = itemView.findViewById(R.id.alarm_time_tv);
             alarm_mission = itemView.findViewById(R.id.alarm_mission_tv);
             alarm_switch = itemView.findViewById(R.id.alarm_switch);
             sunday = itemView.findViewById(R.id.sunday_tv);
             monday = itemView.findViewById(R.id.monday_tv);
             tuesday = itemView.findViewById(R.id.tuesday_tv);
             wednesday = itemView.findViewById(R.id.wednesday_tv);
             thursday = itemView.findViewById(R.id.thursday_tv);
             friday = itemView.findViewById(R.id.friday_tv);
             saturday = itemView.findViewById(R.id.saturday_tv);
             cardViewLayout = itemView.findViewById(R.id.cardViewLayout);

             itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if(recyclerViewInterface!= null){
                         int position = getAdapterPosition();
                         if(position != RecyclerView.NO_POSITION )
                             recyclerViewInterface.onItemClick(position);
                     }
                 }
             });

             itemView.setOnLongClickListener(new View.OnLongClickListener() {
                 @Override
                 public boolean onLongClick(View view) {
                     if(recyclerViewInterface!= null){
                         int position = getAdapterPosition();
                         if(position != RecyclerView.NO_POSITION )
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
                    R.layout.recycler_item, parent, false);
            return new AlarmViewHolder(alarmView);
        }

        @Override
        public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
            Alarm alarm = alarms.get(position);

            holder.alarm_name.setText(alarm.getName());
            holder.alarm_time.setText(String.format("%02d:%02d", alarm.getHour(), alarm.getMinute()));
            holder.alarm_mission.setText(alarm.getMission());

            // mark alarm days
            if (alarm.isSunday()) markSunday(holder);
            if (alarm.isMonday()) markMonday(holder);
            if (alarm.isTuesday()) markTuesday(holder);
            if (alarm.isWednesday()) markWednesday(holder);
            if (alarm.isThursday()) markThursday(holder);
            if (alarm.isFriday()) markFriday(holder);
            if (alarm.isSaturday()) markSaturday(holder);


            if (alarm.isActive()) //turn on Alarm switch, if alarm is Active
                holder.alarm_switch.setChecked(true);
            else // change alarm background color to gray, if Inactive.
                holder.cardViewLayout.setBackgroundResource(R.color.inactiveAlarm);


            // change cardView background color based on the switch,
            // and the active state in data base
            holder.alarm_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    if (!isChecked) {
                        holder.cardViewLayout.setBackgroundResource(R.color.inactiveAlarm);
                        DataBaseHelper.database.changeAlarmActiveState(false, alarm);
                        alarm.setActive(false);
                    } else {
                        holder.cardViewLayout.setBackgroundResource(R.color.recyclerViewItemColor);
                        DataBaseHelper.database.changeAlarmActiveState(true, alarm);
                        alarm.setActive(true);
                    }

                }
            });


        }

        private void markSunday(AlarmViewHolder holder) {
            holder.sunday.setTextSize(checkedDays_TextViewSize);
            holder.sunday.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
        }

        private void markMonday(AlarmViewHolder holder) {
            holder.monday.setTextSize(checkedDays_TextViewSize);
            holder.monday.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
        }

        private void markTuesday(AlarmViewHolder holder) {
            holder.tuesday.setTextSize(checkedDays_TextViewSize);
            holder.tuesday.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
        }

        private void markWednesday(AlarmViewHolder holder) {
            holder.wednesday.setTextSize(checkedDays_TextViewSize);
            holder.wednesday.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
        }

        private void markThursday(AlarmViewHolder holder) {
            holder.thursday.setTextSize(checkedDays_TextViewSize);
            holder.thursday.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
        }

        private void markFriday(AlarmViewHolder holder) {
            holder.friday.setTextSize(checkedDays_TextViewSize);
            holder.friday.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
        }

        private void markSaturday(AlarmViewHolder holder) {
            holder.saturday.setTextSize(checkedDays_TextViewSize);
            holder.saturday.setTextColor(Color.rgb(daysRgbValues[0], daysRgbValues[1], daysRgbValues[2]));
        }


        @Override
        public int getItemCount() {
            return alarms.size();
        }


    }

