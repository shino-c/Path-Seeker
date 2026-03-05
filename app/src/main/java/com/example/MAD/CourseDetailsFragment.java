package com.example.MAD;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CourseDetailsFragment extends Fragment {

    private TextView courseName, courseDescription, courseLevel, courseDuration, courseRating,courseContent;
    private ImageView courseImage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_details, container, false);

        // Initialize views
        courseName = view.findViewById(R.id.course_name_details);
        courseDescription = view.findViewById(R.id.course_description_details);
        courseLevel = view.findViewById(R.id.course_level_details);
        courseContent = view.findViewById(R.id.course_content_details);
        courseDuration = view.findViewById(R.id.course_duration_details);
        courseRating = view.findViewById(R.id.course_rating_details);
        courseImage = view.findViewById(R.id.course_image_details);
        Button enrollButton = view.findViewById(R.id.btn_enroll);
        ImageButton backButton = view.findViewById(R.id.back_button);

        // Get the arguments passed from CourseListFragment
        Bundle args = getArguments();
        if (args != null) {
            Course course = (Course) args.getSerializable("course");

            if (course != null) {
                // Set the course details
                courseName.setText(course.getCourseName());
                courseDescription.setText(course.getDescription());
                courseLevel.setText(course.getLevel());
                courseContent.setText(course.getContentDetails());
                courseDuration.setText(course.getDuration());
                courseRating.setText(String.valueOf(course.getRating()));
                courseImage.setImageResource(course.getImageResId());

                enrollButton.setOnClickListener(v -> {
                    String courseUrl = course.getUrl();
                    if (courseUrl != null && !courseUrl.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(courseUrl));
                        startActivity(intent);
                    } else {
                        showError("Course URL not available");
                    }
                });
            }
        }

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
