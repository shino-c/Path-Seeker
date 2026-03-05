package com.example.MAD;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MentorshipFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private List<String> titles;
    private List<Integer> mImages;
    private List<String> descriptions;
    private MyAdapter adapter;
    private SearchView searchView;
    private List<Mentor> mentorsList;

    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MentorshipFragment() {
        // Required empty public constructor
    }

    public static MentorshipFragment newInstance(String param1, String param2) {
        MentorshipFragment fragment = new MentorshipFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize empty lists
        titles = new ArrayList<>();
        mImages = new ArrayList<>();
        descriptions = new ArrayList<>();
        mentorsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mentorship, container, false);

        mRecyclerView = view.findViewById(R.id.recyclerview);
        searchView = view.findViewById(R.id.searchView);

        // Initialize adapter with empty lists
        adapter = new MyAdapter(requireContext(), titles, mImages, descriptions, mentorsList);

        ImageView backIcon = view.findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> requireActivity().onBackPressed());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(adapter);

        setupSearchView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch data whenever the fragment becomes visible
        fetchMentorsData();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!titles.isEmpty()) {  // Only filter if we have data
                    filterList(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!titles.isEmpty()) {  // Only filter if we have data
                    filterList(newText);
                }
                return false;
            }
        });
    }

    private void fetchMentorsData() {
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.fetchMentors(new FirebaseHelper.DataCallback() {
            @Override
            public void onSuccess(List<Mentor> fetchedMentors) {
                if (!isAdded()) return;

                mentorsList.clear();
                mentorsList.addAll(fetchedMentors);
                titles.clear();
                mImages.clear();
                descriptions.clear();

                for (Mentor mentor : fetchedMentors) {
                    titles.add(mentor.mentor_name);
                    int imageResId = getValidImageResourceId(mentor.mentor_profilepic);
                    mImages.add(imageResId);
                    descriptions.add(mentor.mentor_title);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to fetch mentors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(String text) {
        if (titles.isEmpty()) {
            return;
        }

        List<String> filteredTitles = new ArrayList<>();
        List<Integer> filteredImages = new ArrayList<>();
        List<String> filteredDescriptions = new ArrayList<>();
        List<Mentor> filteredMentors = new ArrayList<>();  // Add this

        String lowerCaseText = text.toLowerCase();

        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i).toLowerCase();
            String description = descriptions.get(i).toLowerCase();

            if (title.contains(lowerCaseText) || description.contains(lowerCaseText)) {
                filteredTitles.add(titles.get(i));
                filteredImages.add(mImages.get(i));
                filteredDescriptions.add(descriptions.get(i));
                filteredMentors.add(mentorsList.get(i));  // Add this
            }
        }

        if (filteredTitles.isEmpty() && !text.isEmpty()) {
            Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
        }

        adapter.setFilteredList(filteredTitles, filteredImages, filteredDescriptions, filteredMentors);  // Update this
    }

    private int getValidImageResourceId(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            // Return a default placeholder image resource ID
            return R.drawable.expert1;
        }

        int resId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());
        if (resId == 0) {
            // Return a default placeholder if the resource is not found
            return R.drawable.expert1;
        }

        return resId;
    }
}
