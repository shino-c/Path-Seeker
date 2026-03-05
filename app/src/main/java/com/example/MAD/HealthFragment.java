package com.example.MAD;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HealthFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private List<String> titles;
    private List<Integer> mImages;
    private List<String> descriptions;
    private MyAdapter adapter;
    private SearchView searchView;
    private List<Mentor> expertsList;
    private TabLayout tabLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public HealthFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize lists
        titles = new ArrayList<>();
        mImages = new ArrayList<>();
        descriptions = new ArrayList<>();
        expertsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        mRecyclerView = view.findViewById(R.id.recyclerview2);
        searchView = view.findViewById(R.id.searchView2);

        // Initialize adapter with empty lists
        adapter = new MyAdapter(requireContext(), titles, mImages, descriptions, expertsList);

        ImageView backIcon = view.findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> requireActivity().onBackPressed());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(adapter);

        setupSearchView();
        setupTabLayout(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch data whenever the fragment becomes visible
        fetchHealthExpertsData();
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

    private void setupTabLayout(View view) {
        tabLayout = view.findViewById(R.id.tablayout);
        tabLayout.selectTab(tabLayout.getTabAt(1));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0 && isAdded()) {
                    Navigation.findNavController(requireView()).navigate(R.id.articleFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchHealthExpertsData() {
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.fetchHealthExperts(new FirebaseHelper.DataCallback() {
            @Override
            public void onSuccess(List<Mentor> fetchedExperts) {
                if (!isAdded()) return;  // Check if fragment is still attached

                Log.d("HealthFragment", "Fetched experts: " + fetchedExperts.size());
                expertsList.clear();
                expertsList.addAll(fetchedExperts);
                titles.clear();
                mImages.clear();
                descriptions.clear();

                for (Mentor expert : fetchedExperts) {
                    Log.d("HealthFragment", "Expert: " + expert.mentor_name);
                    titles.add(expert.mentor_name);
                    mImages.add(getValidImageResourceId(expert.mentor_profilepic));
                    descriptions.add(expert.mentor_title);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;  // Check if fragment is still attached
                Log.e("HealthFragment", "Failed to fetch health experts: " + e.getMessage());
                Toast.makeText(requireContext(), "Failed to fetch experts: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getValidImageResourceId(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            return R.drawable.expert1;
        }

        int resId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());
        return resId == 0 ? R.drawable.expert1 : resId;
    }

    private void filterList(String text) {
        if (titles.isEmpty()) {
            return;
        }

        List<String> filteredTitles = new ArrayList<>();
        List<Integer> filteredImages = new ArrayList<>();
        List<String> filteredDescriptions = new ArrayList<>();
        List<Mentor> filteredExperts = new ArrayList<>();  // Add this

        String lowerCaseText = text.toLowerCase();

        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i).toLowerCase();
            String description = descriptions.get(i).toLowerCase();

            if (title.contains(lowerCaseText) || description.contains(lowerCaseText)) {
                filteredTitles.add(titles.get(i));
                filteredImages.add(mImages.get(i));
                filteredDescriptions.add(descriptions.get(i));
                filteredExperts.add(expertsList.get(i));  // Add this
            }
        }

        if (filteredTitles.isEmpty() && !text.isEmpty()) {
            Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
        }

        adapter.setFilteredList(filteredTitles, filteredImages, filteredDescriptions, filteredExperts);  // Update this
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.removeHealthExpertsListener();
    }
}