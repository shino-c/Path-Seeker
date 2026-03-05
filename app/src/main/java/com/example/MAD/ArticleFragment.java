package com.example.MAD;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ArticleFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private List<String> titles;
    private List<Integer> mImages;
    private List<String> subtitle;
    private ArticleAdapter adapter;
    private SearchView searchView;
    private List<Article> articleList;
    private TabLayout tabLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public ArticleFragment() {
    }

    public static ArticleFragment newInstance(String param1, String param2) {
        ArticleFragment fragment = new ArticleFragment();
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
        subtitle = new ArrayList<>();
        articleList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        mRecyclerView = view.findViewById(R.id.recyclerview3);
        searchView = view.findViewById(R.id.searchView3);

        // Initialize adapter with empty lists
        adapter = new ArticleAdapter(requireContext(), titles, subtitle, mImages, articleList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setAdapter(adapter);

        ImageView backIcon = view.findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> requireActivity().onBackPressed());

        setupSearchView();
        setupTabLayout(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch data whenever the fragment becomes visible
        fetchArticlesData();
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
        tabLayout.selectTab(tabLayout.getTabAt(0));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1 && isAdded()) {
                    Navigation.findNavController(requireView()).navigate(R.id.healthFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchArticlesData() {
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.fetchArticle(new FirebaseHelper.ArticleCallback() {
            @Override
            public void onSuccess(List<Article> fetchedArticle) {
                if (!isAdded()) return;

                articleList.clear();
                articleList.addAll(fetchedArticle);
                titles.clear();
                mImages.clear();
                subtitle.clear();

                for (Article article : fetchedArticle) {
                    if (article != null) {
                        titles.add(article.getTitle());
                        int imageResId = getValidImageResourceId(article.getImage());
                        mImages.add(imageResId);
                        subtitle.add(article.getSubtitle());
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to fetch articles: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getValidImageResourceId(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            return R.drawable.article1;
        }

        int resId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());
        return resId == 0 ? R.drawable.article1 : resId;
    }

    private void filterList(String text) {
        if (titles.isEmpty()) {
            // Don't show "No data found" toast if we're still loading data
            return;
        }

        List<String> filteredTitles = new ArrayList<>();
        List<Integer> filteredImages = new ArrayList<>();
        List<String> filteredSubtitle = new ArrayList<>();
        List<Article> filteredArticles = new ArrayList<>();

        String lowerCaseText = text.toLowerCase();

        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i).toLowerCase();
            String description = subtitle.get(i).toLowerCase();

            if (title.contains(lowerCaseText) || description.contains(lowerCaseText)) {
                filteredTitles.add(titles.get(i));
                filteredImages.add(mImages.get(i));
                filteredSubtitle.add(subtitle.get(i));
                filteredArticles.add(articleList.get(i));
            }
        }

        if (filteredTitles.isEmpty() && !text.isEmpty()) {
            // Only show "No data found" toast if we have data but no matches
            Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
        }

        adapter.setFilteredList(filteredTitles, filteredSubtitle, filteredImages, filteredArticles);
    }
}