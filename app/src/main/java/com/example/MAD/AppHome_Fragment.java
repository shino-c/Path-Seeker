package com.example.MAD;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import java.util.ArrayList;

public class AppHome_Fragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ImageSlider imageSlider;
    private TextView userNameTextView;
    private String mParam1;
    private String mParam2;

    private String dob;

    public AppHome_Fragment() {
        // Required empty public constructor
    }

    public static AppHome_Fragment newInstance(String param1, String param2) {
        AppHome_Fragment fragment = new AppHome_Fragment();
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
        View view = inflater.inflate(R.layout.fragment_app_home, container, false);
        ImageView imageViewCard4 = view.findViewById(R.id.imageViewCard4);
        dob = getCurrentDOB();
            if (dob != null && !dob.isEmpty()) {
                imageViewCard4.setImageResource(R.drawable.bookmark);
            } else {
                imageViewCard4.setImageResource(R.drawable.job);

            }
        // Initialize the ImageSlider
        imageSlider = view.findViewById(R.id.imageSlider);

        // Prepare the slide models
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.slider1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slider2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slider3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slider4, ScaleTypes.FIT));

        // Set the image list for the slider
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        userNameTextView = view.findViewById(R.id.userName);

        // Get username from UserSessionManager first
        String savedUserName = UserSessionManager.getInstance().getUserName();

        // If not in UserSessionManager, try to get from arguments
        if (savedUserName == null || savedUserName.isEmpty()) {
            Bundle args = getArguments();
            if (args != null) {
                savedUserName = args.getString("userName");
                // Save to UserSessionManager for future use
                if (savedUserName != null) {
                    UserSessionManager.getInstance().setUserName(savedUserName);
                }
            }
        }

        // Set the username
        userNameTextView.setText(savedUserName != null ? savedUserName : "No Name");

        // Initialize card1 and set click listener
        CardView card1 = view.findViewById(R.id.card1);
        card1.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_forumFragment);
        });



        // Initialize card3 and set click listener
        CardView card3 = view.findViewById(R.id.card3);
        card3.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_mentorshipFragment);
        });

        // Initialize card1 and set click listener
        CardView card2 = view.findViewById(R.id.card2);
        String dob = getCurrentDOB();
        card2.setOnClickListener(v -> {
            if (dob != null && !dob.isEmpty()) {
                Navigation.findNavController(view).navigate(R.id.notificationStatus);
            } else {
                Navigation.findNavController(view).navigate(R.id.notiRequestFragment);
            }
        });

        CardView card4 = view.findViewById(R.id.card4);
        card4.setOnClickListener(v -> {
            if (dob != null && !dob.isEmpty()) {
                Navigation.findNavController(view).navigate(R.id.savedFragment);
            } else {
                Navigation.findNavController(view).navigate(R.id.createNewJobFragment);

            }
        });


        return view;
    }

    private String getCurrentDOB() {
        String dob = UserSessionManager.getInstance().getDob();
        return dob != null ? dob : "";  // Return empty string if null
    }
}