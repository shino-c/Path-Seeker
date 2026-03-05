package com.example.MAD;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.navigation.Navigation;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ArticleDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArticleDetailFragment extends Fragment {

    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvContent;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ArticleDetailFragment() {
    }
    public static ArticleDetailFragment newInstance(String param1, String param2) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);
        setupArticle(view);


        ImageView backIcon = view.findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void setupArticle(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String articleImage = bundle.getString("articleImage");
            if (articleImage != null) {
                ImageView articleImageView = view.findViewById(R.id.imageView);
                articleImageView.setImageResource(getResources().getIdentifier(
                        articleImage, "drawable", requireContext().getPackageName()));
            }

            tvTitle = view.findViewById(R.id.tvTitle);
            tvAuthor = view.findViewById(R.id.tvAuthor);
            tvContent = view.findViewById(R.id.tvContent);

            tvTitle.setText(bundle.getString("articleTitle", ""));
            String description = String.format("Written by: %s %s",
                    bundle.getString("articleAuthor", ""),
                    bundle.getString("articlePosition", ""));
            tvAuthor.setText(description);
            tvContent.setText(bundle.getString("articleContent", ""));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

}