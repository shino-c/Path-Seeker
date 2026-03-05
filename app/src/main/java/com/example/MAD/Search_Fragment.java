package com.example.MAD;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Search_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Search_Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView jobText;

    public Search_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchMainPage.
     */
    // TODO: Rename and change types and number of parameters
    public static Search_Fragment newInstance(String param1, String param2) {
        Search_Fragment fragment = new Search_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_main_page, container, false);
        jobText = view.findViewById(R.id.jobText);
        view.post(() -> {
//            try {
            String dob = getCurrentDOB();
            Log.d("JobMainFragment", "DOB value: " + dob);
            if (dob != null && !dob.isEmpty()) {
                jobText.setText("Find a Job");
            } else {
                jobText.setText("Post a Job");
            }
//            } catch (Exception e) {
//                Log.e("ProfileIdentifyFragment", "Navigation error: " + e.getMessage());
//            }
        });

        CardView findAJobCard = view.findViewById(R.id.findAJobCard);
        findAJobCard.setOnClickListener(v -> {
            // Check current destination before navigating
            if (Navigation.findNavController(v).getCurrentDestination().getId() == R.id.searchFragment) {
                Navigation.findNavController(view).navigate(R.id.action_searchFragment_to_jobMainFragment);
            }
        });

        CardView skillCard = view.findViewById(R.id.skillCard);
        skillCard.setOnClickListener(v -> {
            // Check current destination before navigating
            if (Navigation.findNavController(v).getCurrentDestination().getId() == R.id.searchFragment) {
                Navigation.findNavController(view).navigate(R.id.action_searchFragment_to_courseFragment);
            }
        });

        CardView partnerCard = view.findViewById(R.id.partnerCard);
        partnerCard.setOnClickListener(v -> {
            // Check current destination before navigating
            if (Navigation.findNavController(v).getCurrentDestination().getId() == R.id.searchFragment) {
                Navigation.findNavController(view).navigate(R.id.action_searchFragment_to_partnershipProgramFragment);
            }
        });

        CardView eventCard = view.findViewById(R.id.eventCard);
        eventCard.setOnClickListener(v -> {
            // Check current destination before navigating
            if (Navigation.findNavController(v).getCurrentDestination().getId() == R.id.searchFragment) {
                Navigation.findNavController(view).navigate(R.id.action_searchFragment_to_eventsFragment);
            }
        });


        return view;
    }

    private String getCurrentDOB() {
        String dob = UserSessionManager.getInstance().getDob();
        return dob != null ? dob : "";  // Return empty string if null
    }
}