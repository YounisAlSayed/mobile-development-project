package com.example.recyclerviewwebservice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.recyclerviewwebservice.model.Product;
import com.example.recyclerviewwebservice.network.OpenLibraryWorkApi;
import com.example.recyclerviewwebservice.storage.FavoriteStore;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String EXTRA_ID = "product_id";
    private static final String EXTRA_TITLE = "product_title";
    private static final String EXTRA_AUTHOR = "product_author";
    private static final String EXTRA_IMAGE_URL = "product_image_url";
    private static final String EXTRA_PRICE_CENTS = "product_price_cents";
    private static final String EXTRA_YEAR = "product_year";
    private static final String EXTRA_EDITIONS = "product_editions";

    private FavoriteStore favoriteStore;
    private String productId;
    private String productTitle;
    private ImageButton favoriteButton;
    private OpenLibraryWorkApi workApi;

    public static void open(Context context, Product product) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra(EXTRA_ID, product.getId());
        intent.putExtra(EXTRA_TITLE, product.getTitle());
        intent.putExtra(EXTRA_AUTHOR, product.getSubtitle());
        intent.putExtra(EXTRA_IMAGE_URL, product.getImageUrl());
        intent.putExtra(EXTRA_PRICE_CENTS, product.getPriceCents());
        intent.putExtra(EXTRA_YEAR, product.getFirstPublishYear());
        intent.putExtra(EXTRA_EDITIONS, product.getEditionCount());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productId = valueOrFallback(getIntent().getStringExtra(EXTRA_ID));
        productTitle = valueOrFallback(getIntent().getStringExtra(EXTRA_TITLE));
        String author = valueOrFallback(getIntent().getStringExtra(EXTRA_AUTHOR));
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        int priceCents = getIntent().getIntExtra(EXTRA_PRICE_CENTS, 0);
        int firstPublishYear = getIntent().getIntExtra(EXTRA_YEAR, 0);
        int editionCount = getIntent().getIntExtra(EXTRA_EDITIONS, 0);

        ImageView coverImage = findViewById(R.id.detailCoverImage);
        TextView titleText = findViewById(R.id.detailTitleText);
        TextView authorText = findViewById(R.id.detailAuthorText);
        TextView priceText = findViewById(R.id.detailPriceText);
        TextView yearText = findViewById(R.id.detailYearText);
        TextView editionsText = findViewById(R.id.detailEditionsText);
        TextView descriptionText = findViewById(R.id.detailDescriptionText);
        ProgressBar descriptionProgress = findViewById(R.id.detailDescriptionProgress);
        favoriteButton = findViewById(R.id.detailFavoriteButton);

        titleText.setText(productTitle);
        authorText.setText(author);
        priceText.setText(NumberFormat.getCurrencyInstance(Locale.US)
                .format(priceCents / 100.0));
        yearText.setText(firstPublishYear > 0
                ? String.valueOf(firstPublishYear)
                : getString(R.string.detail_unknown));
        editionsText.setText(editionCount > 0
                ? NumberFormat.getIntegerInstance().format(editionCount)
                : getString(R.string.detail_unknown));

        Glide.with(coverImage)
                .load(imageUrl)
                .placeholder(R.drawable.ic_product_placeholder)
                .error(R.drawable.ic_product_placeholder)
                .centerCrop()
                .into(coverImage);

        favoriteStore = new FavoriteStore(this);
        bindFavoriteButton();
        favoriteButton.setOnClickListener(view -> {
            favoriteStore.toggle(productId);
            bindFavoriteButton();
        });
        findViewById(R.id.backButton).setOnClickListener(view -> finish());

        workApi = new OpenLibraryWorkApi(this);
        workApi.fetchDescription(productId, new OpenLibraryWorkApi.Callback() {
            @Override
            public void onSuccess(String description) {
                descriptionProgress.setVisibility(View.GONE);
                descriptionText.setText(description);
            }

            @Override
            public void onError() {
                descriptionProgress.setVisibility(View.GONE);
                descriptionText.setText(R.string.detail_description_unavailable);
            }
        });
    }

    private void bindFavoriteButton() {
        boolean favorite = favoriteStore.isFavorite(productId);
        favoriteButton.setImageResource(
                favorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
        );
        favoriteButton.setContentDescription(getString(
                favorite ? R.string.remove_from_favorites : R.string.add_to_favorites,
                productTitle
        ));
    }

    private String valueOrFallback(String value) {
        return value == null || value.trim().isEmpty()
                ? getString(R.string.detail_unknown)
                : value;
    }

    @Override
    protected void onDestroy() {
        if (workApi != null) {
            workApi.shutdown();
        }
        super.onDestroy();
    }
}
