package com.example.MAD;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PartnershipProgramFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgramAdapter adapter;
    private List<Program> freePrograms;
    private List<Program> paidPrograms;
    private TextView tabFreeProgram;
    private TextView tabPaidProgram;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_partnership_program, container, false);

        recyclerView = view.findViewById(R.id.program_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView titleText = view.findViewById(R.id.title_text);
        titleText.setText("Partnership Program");

        tabFreeProgram = view.findViewById(R.id.tab_free_program);
        tabPaidProgram = view.findViewById(R.id.tab_paid_program);

        initializePrograms();

        tabFreeProgram.setOnClickListener(v -> {
            loadPrograms(freePrograms);
            updateTabColors(tabFreeProgram, tabPaidProgram);
        });

        tabPaidProgram.setOnClickListener(v -> {
            loadPrograms(paidPrograms);
            updateTabColors(tabPaidProgram, tabFreeProgram);
        });

        // Load free programs by default
        loadPrograms(freePrograms);
        updateTabColors(tabFreeProgram, tabPaidProgram);

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    private void initializePrograms() {
        freePrograms = new ArrayList<>();
        freePrograms.add(new Program("Harvard Online Learning", R.drawable.harvard_image, "https://online-learning.harvard.edu"));
        freePrograms.add(new Program("LinkedIn Learning", R.drawable.linkedin_image, "https://linkedin.com/learning"));

        paidPrograms = new ArrayList<>();
        paidPrograms.add(new Program("Coursera for Business", R.drawable.coursera_image, "https://coursera.org"));
        paidPrograms.add(new Program("edX for Business", R.drawable.edx_image, "https://edx.org"));
    }

    private void loadPrograms(List<Program> programs) {
        adapter = new ProgramAdapter(programs, getContext());
        recyclerView.setAdapter(adapter);
    }

    private void updateTabColors(TextView selectedTab, TextView unselectedTab) {
        selectedTab.setBackgroundColor(getResources().getColor(R.color.orange));
        selectedTab.setTextColor(Color.WHITE);

        unselectedTab.setBackgroundColor(Color.TRANSPARENT);
        unselectedTab.setTextColor(Color.WHITE);
    }
}
