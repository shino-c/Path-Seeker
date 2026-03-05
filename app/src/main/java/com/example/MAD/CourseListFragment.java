package com.example.MAD;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CourseListFragment extends Fragment {
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private EditText searchCourse;
    private List<Course> courseList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_list, container, false);

        recyclerView = view.findViewById(R.id.course_list_recycler_view);
        searchCourse = view.findViewById(R.id.search_course);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize the course list
        courseList = getCourses();

        // Set up the adapter and attach it to RecyclerView
        adapter = new CourseAdapter(requireContext(), courseList, null);
        recyclerView.setAdapter(adapter);

        // Set up the search functionality
        setupSearch();

        return view;
    }

    private void setupSearch() {
        searchCourse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCourses(String query) {
        List<Course> filteredList = new ArrayList<>();
        for (Course course : courseList) {
            if (course.getCourseName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(course);
            }
        }
        adapter = new CourseAdapter(requireContext(), filteredList, null);
        recyclerView.setAdapter(adapter);
    }

    private List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("Basic Japanese Language", "Beginner", "60 hours", "38 videos, 80 exercises",
                "Welcome to the Basic Japanese Language Course, your gateway to mastering the essentials of Japanese communication! Designed for beginners, this course provides a comprehensive introduction to the fundamentals of the language, including pronunciation, hiragana, katakana, and basic kanji. Through engaging lessons and practical exercises, you'll learn essential vocabulary, common phrases, and key grammar concepts that will help you navigate everyday conversations. Whether you're planning to travel to Japan, exploring the culture, or simply starting your language-learning journey, this course offers the perfect foundation to build your skills and confidence in Japanese. Let's embark on this exciting journey together!",
                8.5, R.drawable.basic_japanese, "https://www.lingq.com/en/learn/ja/web/reader/1958760"));
        courses.add(new Course("Introduction to Python", "Beginner", "30 hours", "25 videos, 60 exercises",
                "Welcome to the Introduction to Python Course, your first step into the world of programming! This course is perfect for beginners looking to learn one of the most versatile and in-demand programming languages. You'll start with the basics, including syntax, data types, and control structures, before progressing to more advanced topics like functions, modules, and error handling. Through hands-on projects and practical exercises, you'll build real-world applications and develop problem-solving skills that will empower you to take on coding challenges with confidence. Whether you're aiming for a career in tech or exploring programming as a hobby, this course is the ideal starting point.",
                9.0, R.drawable.intro_python, "https://www.datacamp.com/courses/intro-to-python-for-data-science"));
        courses.add(new Course("Introduction to Marketing", "Beginner", "20 hours", "17 videos, 46 exercises",
                "Welcome to the Introduction to Marketing Course, a course designed to ignite your understanding of how businesses connect with their audience. From the fundamentals of branding and consumer behavior to strategies in digital marketing and advertising, this course covers the essential tools and concepts needed to craft impactful marketing campaigns. You'll explore real-world examples, learn to analyze market trends, and gain insights into building lasting customer relationships. Whether you're a budding entrepreneur, a student, or simply curious about marketing's role in our everyday lives, this course offers valuable knowledge to navigate the dynamic world of marketing.",
                7.3, R.drawable.intro_marketing, "https://www.coursera.org/learn/wharton-marketing"));
        courses.add(new Course("Ancient Roman Arts", "Intermediate", "54 hours", "40 videos, 30 exercises",
                "Step back in time with the Ancient Roman Arts, a course that explores the rich and diverse artistic heritage of ancient Rome. This journey takes you through iconic architecture, sculptures, mosaics, and frescoes that reflect the grandeur and innovation of Roman civilization. You'll discover how Roman art was influenced by Greek traditions and how it, in turn, shaped Western art and culture for centuries. Through vivid examples and historical context, this course will help you appreciate the artistry, craftsmanship, and cultural significance of one of history's greatest empires. Whether you're an art enthusiast or a history buff, this course offers a captivating window into the artistic legacy of Rome.",
                4.6, R.drawable.ancient_arts, "https://www.udemy.com/course/the-art-of-ancient-rome/"));
        return courses;
    }
}