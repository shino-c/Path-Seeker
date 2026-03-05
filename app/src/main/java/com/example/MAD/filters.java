package com.example.MAD;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class filters extends Fragment {

    private double latitude, longitude;
    private TextView dropdownJobTypeHeader, dropdownRemoteHeader, dropdownExperienceHeader, dropdownJobCategoryHeader, radiusValue;
    private RecyclerView dropdownJobTypeRecyclerView, dropdownRemoteRecyclerView, dropdownExperienceRecyclerView, dropdownJobCategoryRecyclerView;
    private SeekBar radiusSeekBar;
    private boolean isJobTypeExpanded = false, isRemoteExpanded = false, isExperienceExpanded = false, isJobCategoryExpanded = false;

    private List<String> jobTypes = new ArrayList<>(Arrays.asList("Full-Time", "Part-Time", "Contract", "Temporary", "Internship"));
    private List<Boolean> jobTypeSelections = new ArrayList<>(Arrays.asList(false, false, false, false, false));

    private List<String> remoteOptions = new ArrayList<>(Arrays.asList("On-Site", "Remote", "Hybrid"));
    private List<Boolean> remoteSelections = new ArrayList<>(Arrays.asList(false, false, false));

    private List<String> experienceLevels = new ArrayList<>(Arrays.asList("Internship", "Entry-Level", "Mid-Level", "Senior-Level", "Managerial", "Executive", "Freelance"));
    private List<Boolean> experienceSelections = new ArrayList<>(Arrays.asList(false, false, false, false, false, false, false));

    private List<String> jobCategory = new ArrayList<>(Arrays.asList("Technology", "Engineering", "Healthcare", "Business", "Education", "Creative", "Retail", "Food", "Transportation", "Administrative", "Law", "Science", "Others"));
    private List<Boolean> jobCategorySelections = new ArrayList<>(Arrays.asList(false, false, false, false, false, false, false, false, false, false, false, false, false));

    public filters() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_filter, container, false);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Location", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("RADIUS", 100);
        editor.apply();
        latitude = sharedPreferences.getFloat("LATITUDE", 0f);
        longitude = sharedPreferences.getFloat("LONGITUDE", 0f);
        // Back button to return to previous page
        ImageButton btnBack = rootView.findViewById(R.id.IBback);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Navigate to Map when clicking "Use Current Location"
        TextView tvUseLoc = rootView.findViewById(R.id.TVuseLoc);
        tvUseLoc.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Navigation.findNavController(requireView()).navigate(R.id.mapsFragment);
                                        }
                                    });

        // Job Type Dropdown
        dropdownJobTypeHeader = rootView.findViewById(R.id.dropdownJobTypeHeader);
        dropdownJobTypeRecyclerView = rootView.findViewById(R.id.dropdownJobTypeRecyclerView);
        setupDropdown(dropdownJobTypeHeader, dropdownJobTypeRecyclerView, jobTypes, jobTypeSelections, new boolean[]{isJobTypeExpanded});

        // Remote Dropdown
        dropdownRemoteHeader = rootView.findViewById(R.id.dropdownRemoteHeader);
        dropdownRemoteRecyclerView = rootView.findViewById(R.id.dropdownRemoteRecyclerView);
        setupDropdown(dropdownRemoteHeader, dropdownRemoteRecyclerView, remoteOptions, remoteSelections, new boolean[]{isRemoteExpanded});

        // Experience Dropdown
        dropdownExperienceHeader = rootView.findViewById(R.id.dropdownExperienceHeader);
        dropdownExperienceRecyclerView = rootView.findViewById(R.id.dropdownExperienceRecyclerView);
        setupDropdown(dropdownExperienceHeader, dropdownExperienceRecyclerView, experienceLevels, experienceSelections, new boolean[]{isExperienceExpanded});

        // Job Category Dropdown
        dropdownJobCategoryHeader = rootView.findViewById(R.id.dropdownJobCategoryHeader);
        dropdownJobCategoryRecyclerView = rootView.findViewById(R.id.dropdownJobCategoryRecyclerView);
        setupDropdown(dropdownJobCategoryHeader, dropdownJobCategoryRecyclerView, jobCategory, jobCategorySelections, new boolean[]{isJobCategoryExpanded});

        // Radius SeekBar setup
        radiusSeekBar = rootView.findViewById(R.id.radiusSeekBar);
        radiusValue = rootView.findViewById(R.id.TVWithinKM);

        radiusSeekBar.setMax(100);
        radiusSeekBar.setProgress(100);
        radiusValue.setText("Within 100 km");
        if (getArguments() != null) {
            latitude = getArguments().getDouble("LATITUDE", latitude);
            longitude = getArguments().getDouble("LONGITUDE", longitude);
//            int savedRadius = getArguments().getInt("RADIUS", sharedPreferences.getInt("RADIUS", 1000));
//            radiusSeekBar.setProgress(savedRadius);
//            radiusValue.setText("Within " + savedRadius + " km");
            radiusSeekBar.setProgress(100);
            radiusValue.setText("Within 100 km");
        } else {
//            int savedRadius = sharedPreferences.getInt("RADIUS", 1000);
//            radiusSeekBar.setProgress(savedRadius);
//            radiusValue.setText("Within " + savedRadius + " km");
            radiusSeekBar.setProgress(100);
            radiusValue.setText("Within 100 km");
        }
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                radiusValue.setText("Within " + progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save the new radius value
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("RADIUS", seekBar.getProgress());
                editor.apply();
            }
        });

        // Apply filters button
        Button btnApplyFilters = rootView.findViewById(R.id.btnApply);
        btnApplyFilters.setOnClickListener(v -> {
            // Update SharedPreferences
            editor.putFloat("LATITUDE", (float) latitude);
            editor.putFloat("LONGITUDE", (float) longitude);
            editor.putInt("RADIUS", radiusSeekBar.getProgress());
            editor.apply();


            // Create a bundle to pass data to the jobSearchFragment
            Bundle bundle = new Bundle();

            // Collect filter data
            int selectedRadius = radiusSeekBar.getProgress();
            bundle.putInt("RADIUS", selectedRadius);
            bundle.putDouble("LATITUDE", latitude);
            bundle.putDouble("LONGITUDE", longitude);
            bundle.putStringArrayList("selectedJobTypes", getSelectedItems(jobTypes, jobTypeSelections));
            bundle.putStringArrayList("selectedRemoteOptions", getSelectedItems(remoteOptions, remoteSelections));
            bundle.putStringArrayList("selectedExperienceLevels", getSelectedItems(experienceLevels, experienceSelections));
            bundle.putStringArrayList("selectedJobCategory", getSelectedItems(jobCategory, jobCategorySelections));

            // Navigate to the jobSearchFragment with the bundle
            Navigation.findNavController(requireView()).navigate(R.id.jobSearchFragment, bundle);
        });

        return rootView;
    }

    private void setupDropdown(TextView header, RecyclerView recyclerView, List<String> items, List<Boolean> selections, boolean[] isExpanded) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DropdownAdapter adapter = new DropdownAdapter(items, selections, () -> updateHeaderText(header, items, selections));
        recyclerView.setAdapter(adapter);

        header.setOnClickListener(v -> toggleDropdown(recyclerView, header, isExpanded));
    }

    private void toggleDropdown(RecyclerView recyclerView, TextView header, boolean[] isExpanded) {
        boolean currentState = recyclerView.getVisibility() == View.VISIBLE;
        recyclerView.setVisibility(currentState ? View.GONE : View.VISIBLE);
        recyclerView.getLayoutParams().height = currentState ? 0 : RecyclerView.LayoutParams.WRAP_CONTENT;
        recyclerView.requestLayout();
        header.setCompoundDrawablesWithIntrinsicBounds(0, 0, currentState ? R.drawable.baseline_keyboard_arrow_down_24 : R.drawable.baseline_keyboard_arrow_up_24, 0);
        isExpanded[0] = !currentState;
    }

    private void updateHeaderText(TextView header, List<String> items, List<Boolean> selections) {
        StringBuilder selectedText = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (selections.get(i)) {
                selectedText.append(items.get(i)).append(", ");
            }
        }

        if (selectedText.length() > 0) {
            selectedText.setLength(selectedText.length() - 2);
            header.setText(selectedText.toString());
        } else {
            header.setText(header.getHint());
        }
    }

    private ArrayList<String> getSelectedItems(List<String> items, List<Boolean> selections) {
        ArrayList<String> selectedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (selections.get(i)) {
                selectedItems.add(items.get(i));
            }
        }
        return selectedItems;
    }
}
