package com.example.MAD;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JobMain#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JobMain extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public JobMain() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment JobMain.
     */
    // TODO: Rename and change types and number of parameters
    public static JobMain newInstance(String param1, String param2) {
        JobMain fragment = new JobMain();
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
        View view = inflater.inflate(R.layout.fragment_job_main, container, false);

        view.post(() -> {
//            try {
            String dob = getCurrentDOB();
            Log.d("JobMainFragment", "DOB value: " + dob);
            if (dob != null && !dob.isEmpty()) {
                Navigation.findNavController(view).navigate(R.id.action_jobMainFragment_to_jobSearchFragment);
            } else {
                Navigation.findNavController(view).navigate(R.id.action_jobMainFragment_to_jobPostedFragment);
            }
//            } catch (Exception e) {
//                Log.e("ProfileIdentifyFragment", "Navigation error: " + e.getMessage());
//            }
        });
        return view;
    }

    private String getCurrentDOB() {
        String dob = UserSessionManager.getInstance().getDob();
        return dob != null ? dob : "";  // Return empty string if null
    }
}