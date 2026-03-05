package com.example.MAD;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ScheduleFragment extends Fragment {
    private RecyclerView timeSlotRecyclerView;
    private TimeSlotAdapter adapter;
    private List<String> timeSlots;
    private EditText etdate;
    private ImageButton btndate;
    private int date, month, year;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Initialize RecyclerView but don't set adapter yet
        timeSlotRecyclerView = view.findViewById(R.id.timeslotRecyclerView);
        timeSlotRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        timeSlotRecyclerView.setVisibility(View.GONE); // Hide initially

        // Initialize date picker views
        etdate = view.findViewById(R.id.etdate);
        btndate = view.findViewById(R.id.btndate);
        setupDatePicker();

        ImageView backIcon = view.findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> requireActivity().onBackPressed());

        // Handle profile data
        setupProfile(view);

        return view;


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup time slots first
        setupTimeSlots(view);

        Button BtnCalendar = view.findViewById(R.id.button4);
        BtnCalendar.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.calendarFragment)
        );
    }

    private void setupDatePicker() {
        View.OnClickListener dateClickListener = v -> {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            date = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        String formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                        etdate.setText(formattedDate);

                        // Initialize and show time slots only after date is selected
                        setupTimeSlots(formattedDate);
                    }, year, month, date);

            dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            dialog.show();
        };

        btndate.setOnClickListener(dateClickListener);
        etdate.setOnClickListener(dateClickListener);
    }

    private void setupTimeSlots(String selectedDate) {
        timeSlots = new ArrayList<>();
        String[] times = {"8:00 AM", "8:30 AM", "9:00 AM", "9:30 AM", "10:00 AM",
                "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
                "2:00 PM", "2:30 PM","3:00 PM", "3:30 PM","4:00 PM"};
        timeSlots.addAll(Arrays.asList(times));

        Bundle bundle = getArguments();
        String mentorId = bundle != null ? bundle.getString("profileName") : "";

        if (mentorId.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Mentor information not found", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new TimeSlotAdapter(requireContext(), timeSlots, "", mentorId, selectedDate);
        timeSlotRecyclerView.setAdapter(adapter);
        timeSlotRecyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView
    }

    private String bookedSlot = ""; // To keep track of the booked slot

    private void setupTimeSlots(View view) {
        timeSlotRecyclerView = view.findViewById(R.id.timeslotRecyclerView);
        timeSlots = new ArrayList<>();
        String[] times = {"8:00 AM", "8:30 AM", "9:00 AM", "9:30 AM", "10:00 AM",
                "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
                "2:00 PM", "2:30 PM"};
        timeSlots.addAll(Arrays.asList(times));

        // Add null checks
        Bundle bundle = getArguments();
        String mentorId = bundle != null ? bundle.getString("profileName") : "";
        String date = etdate.getText() != null ? etdate.getText().toString() : "";

        adapter = new TimeSlotAdapter(requireContext(), timeSlots, "", mentorId, date);
        timeSlotRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        timeSlotRecyclerView.setAdapter(adapter);
    }


    private void setupProfile(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String profileImage = bundle.getString("profileImage");
            if (profileImage != null) {
                ImageView profileImageView = view.findViewById(R.id.profileImage);
                profileImageView.setImageResource(getResources().getIdentifier(
                        profileImage, "drawable", requireContext().getPackageName()));
            }

            TextView profileNameView = view.findViewById(R.id.profileName);
            TextView profileDescriptionView = view.findViewById(R.id.profileDescription);
            TextView profileBioView = view.findViewById(R.id.profileBio);

            profileNameView.setText(bundle.getString("profileName", ""));
            String description = String.format("%s | %d+ years experience | %s",
                    bundle.getString("profileTitle", ""),
                    bundle.getInt("profileYearsExperience", 0),
                    bundle.getString("profileLivingArea", ""));
            profileDescriptionView.setText(description);
            profileBioView.setText(bundle.getString("profileBio", ""));
        }
    }
}