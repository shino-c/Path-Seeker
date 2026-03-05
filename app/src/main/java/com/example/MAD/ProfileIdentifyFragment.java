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
 * Use the {@link ProfileIdentifyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileIdentifyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileIdentifyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileIdentifyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileIdentifyFragment newInstance(String param1, String param2) {
        ProfileIdentifyFragment fragment = new ProfileIdentifyFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile_identify, container, false);

        view.post(() -> {
//            try {
                String dob = getCurrentDOB();
                Log.d("ProfileIdentifyFragment", "DOB value: " + dob);
                if (dob != null && !dob.isEmpty()) {
                    Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_selfSeekerFragment);
                } else {
                    Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_selfCompanyFragment);
                }
//            } catch (Exception e) {
//                Log.e("ProfileIdentifyFragment", "Navigation error: " + e.getMessage());
//            }
        });

        // Return the view
        return view;
    }

    private String getCurrentUserEmail() {
        return UserSessionManager.getInstance().getUserEmail();
    }

    private String getCurrentUserName() {
        return UserSessionManager.getInstance().getUserName();
    }

    private String getCurrentDOB() {
        String dob = UserSessionManager.getInstance().getDob();
        return dob != null ? dob : "";  // Return empty string if null
    }

    private String getCurrentWorkingStatus() {
        return UserSessionManager.getInstance().getWorkingStatus();
    }

    private String getCurrentSector() {
        return UserSessionManager.getInstance().getSector();
    }

}