package com.example.MAD;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView bookedInfoTextView;
    private DatabaseReference databaseReference;
    private static final String TAG = "CalendarFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        bookedInfoTextView = view.findViewById(R.id.bookedInfoTextView);

        ImageView backIcon = view.findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> requireActivity().onBackPressed());

        // Get reference to the root of the database
        databaseReference = FirebaseDatabase.getInstance("https://pathseeker-40c02-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                // Add leading zeros to match the database format
                String dayStr = String.format("%02d", dayOfMonth);    // Changed
                String monthStr = String.format("%02d", month + 1);   // Changed
                String yearStr = String.format("%d", year);

                Log.d(TAG, "Selected date: " + dayStr + "/" + monthStr + "/" + yearStr);
                Log.d(TAG, "Looking for Firebase path: " + dayStr + " / " + monthStr + " / " + yearStr);

                fetchBookedSlots(dayStr, monthStr, yearStr);
            }
        });

        return view;
    }

    private String getCurrentUserEmail() {
        return UserSessionManager.getInstance().getUserEmail();
    }

    // Update the fetchBookedSlots method
    private void fetchBookedSlots(String day, String month, String year) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            bookedInfoTextView.setText("Please log in to view your bookings");
            return;
        }

        Log.d(TAG, "Fetching booked slots for date: " + day + "/" + month + "/" + year);

        databaseReference.child("bookedSlots").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder bookedSlotsInfo = new StringBuilder();

                for (DataSnapshot mentorSnapshot : snapshot.getChildren()) {
                    String mentorName = mentorSnapshot.getKey();

                    DataSnapshot yearSnapshot = mentorSnapshot.child(year);
                    if (yearSnapshot.exists()) {
                        DataSnapshot monthSnapshot = yearSnapshot.child(month);
                        if (monthSnapshot.exists()) {
                            DataSnapshot daySnapshot = monthSnapshot.child(day);
                            if (daySnapshot.exists()) {
                                for (DataSnapshot timeSlotSnapshot : daySnapshot.getChildren()) {
                                    String timeSlot = timeSlotSnapshot.getKey();
                                    try {
                                        Object value = timeSlotSnapshot.getValue();
                                        boolean isUserBooking = false;

                                        if (value instanceof Boolean) {
                                            // Old format - show all bookings
                                            Boolean isBooked = (Boolean) value;
                                            isUserBooking = isBooked;
                                        } else {
                                            // New format - only show user's bookings
                                            FirebaseHelper.BookingData bookingData =
                                                    timeSlotSnapshot.getValue(FirebaseHelper.BookingData.class);
                                            isUserBooking = bookingData != null &&
                                                    bookingData.isBooked() &&
                                                    bookingData.getUserEmail().equals(userEmail);
                                        }

                                        if (isUserBooking) {
                                            bookedSlotsInfo.append(mentorName)
                                                    .append(" - ")
                                                    .append(timeSlot)
                                                    .append("\n");
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error reading booking data", e);
                                    }
                                }
                            }
                        }
                    }
                }

                String finalText = bookedSlotsInfo.length() > 0 ?
                        bookedSlotsInfo.toString().trim() :
                        "No booked slots for this date.";
                bookedInfoTextView.setText(finalText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bookedInfoTextView.setText("Error fetching booked slots.");
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }

}