package com.example.MAD;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {
    private Context context;
    private List<String> articleTitles;
    private List<String> articleSubtitles;
    private List<Integer> articleImages;
    private List<Article> articles; // Assuming you have an Article class for complete article data.

    public ArticleAdapter(Context context, List<String> articleTitles, List<String> articleSubtitles, List<Integer> articleImages, List<Article> articles) {
        this.context = context;
        this.articleTitles = articleTitles;
        this.articleSubtitles = articleSubtitles;
        this.articleImages = articleImages;
        this.articles = articles;
    }

    public void setFilteredList(List<String> filteredTitles, List<String> filteredSubtitles,
                                List<Integer> filteredImages, List<Article> filteredArticles) {
        this.articleTitles = filteredTitles;
        this.articleSubtitles = filteredSubtitles;
        this.articleImages = filteredImages;
        this.articles = filteredArticles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false); // Replace with your article layout resource
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        if (position < 0 || position >= articles.size()) {
            return;
        }

        Article article = articles.get(position);
        if (article == null) {
            return;
        }

        holder.articleTitle.setText(articleTitles.get(position));
        holder.articleSubtitle.setText(articleSubtitles.get(position));
        holder.articleImage.setImageResource(articleImages.get(position));

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("articleTitle", article.getTitle());
            bundle.putString("articleSubtitle", article.getSubtitle());
            bundle.putString("articleContent", article.getContent());
            bundle.putString("articleAuthor", article.getAuthor());
            bundle.putString("articleImage", article.getImage());

            Navigation.findNavController(v).navigate(R.id.articleDetailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return articleTitles.size();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView articleTitle;
        TextView articleSubtitle;
        ImageView articleImage;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            articleTitle = itemView.findViewById(R.id.articleTitle);
            articleSubtitle = itemView.findViewById(R.id.articleSubtitle);
            articleImage = itemView.findViewById(R.id.articleImage);
        }
    }
}
