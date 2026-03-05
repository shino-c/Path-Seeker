package com.example.MAD;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
    private List<String> timeSlots;
    private String bookedSlot;
    private FirebaseHelper firebaseHelper;
    private String mentorId, date;
    private Context context;

    public TimeSlotAdapter(Context context, List<String> timeSlots, String bookedSlot, String mentorId, String date) {
        this.context = context;
        this.timeSlots = timeSlots;
        this.bookedSlot = bookedSlot;
        this.mentorId = mentorId;
        this.date = date;
        firebaseHelper = new FirebaseHelper();
    }

    @Override
    public TimeSlotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.timeslot_item, parent, false);
        return new TimeSlotViewHolder(view);
    }

    private boolean isTimeSlotPassed(String timeSlot) {
        try {
            // Get current date and time
            Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));

            // Parse the selected date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date selectedDate = date.isEmpty() ? new Date() : dateFormat.parse(date);

            // Parse the time slot
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
            Date slotTime = timeFormat.parse(timeSlot);

            // Combine selected date with time slot
            Calendar slotCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
            slotCalendar.setTime(selectedDate);

            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(slotTime);

            slotCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            slotCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

            // Check if the slot is in the past
            if (slotCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    slotCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)) {
                // For today, compare with current time
                return slotCalendar.before(currentCalendar);
            } else {
                // For other days, compare the whole date
                return slotCalendar.before(currentCalendar);
            }

        } catch (ParseException e) {
            Log.e("TimeSlotAdapter", "Error parsing date/time", e);
            return false;
        }
    }


    @Override
    public void onBindViewHolder(TimeSlotViewHolder holder, int position) {
        String timeSlot = timeSlots.get(position);
        holder.timeSlotTextView.setText(timeSlot);
        boolean isPassedTime = isTimeSlotPassed(timeSlot);

        if (isPassedTime) {
            // Grey out and disable passed time slots
            holder.itemView.setBackgroundColor(Color.GRAY);
            holder.itemView.setEnabled(false);
            holder.timeSlotTextView.setTextColor(Color.WHITE); // Make text more visible on gray background
            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(context, "This time slot has passed", Toast.LENGTH_SHORT).show());
            return;
        }

        // Rest of your existing onBindViewHolder code for checking bookings
        firebaseHelper.isSlotBooked(mentorId, date, timeSlot, new FirebaseHelper.DataCallback() {
            @Override
            public void onSuccess(List<Mentor> mentors) {
                boolean isBooked = mentors.size() > 0;
                if (isBooked) {
                    holder.itemView.setBackgroundColor(Color.GRAY);
                    holder.itemView.setEnabled(false);
                    holder.timeSlotTextView.setTextColor(Color.BLACK);
                    holder.itemView.setOnClickListener(v ->
                            Toast.makeText(context, "This slot is already booked", Toast.LENGTH_SHORT).show());
                } else {
                    holder.itemView.setBackgroundColor(Color.WHITE);
                    holder.itemView.setEnabled(true);
                    holder.timeSlotTextView.setTextColor(Color.BLACK);
                    setupBookingClickListener(holder, timeSlot);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "Error checking booking status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentUserEmail() {
        return UserSessionManager.getInstance().getUserEmail();
    }

    private void setupBookingClickListener(TimeSlotViewHolder holder, String timeSlot) {
        holder.itemView.setOnClickListener(v -> {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (date.isEmpty() || !date.matches("\\d{2}/\\d{2}/\\d{4}")) {
                Toast.makeText(context, "Please select a valid date first", Toast.LENGTH_SHORT).show();
                return;
            }


            firebaseHelper.bookSlot(mentorId, date, timeSlot, userEmail, new FirebaseHelper.DataCallback() {
                @Override
                public void onSuccess(List<Mentor> mentors) {
                    Toast.makeText(context, "Slot booked successfully!", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context,
                            "Failed to book slot: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    // View holder class for time slot
    // View holder class for time slot
    public static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView timeSlotTextView;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeSlotTextView = itemView.findViewById(R.id.timeSlotText);
        }
    }
}