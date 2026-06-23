package com.example.recyclerviewwebservice.network;

import com.example.recyclerviewwebservice.model.PriceCalculator;
import com.example.recyclerviewwebservice.model.Product;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OpenLibraryProductParser {
    private static final String COVER_BASE_URL = "https://covers.openlibrary.org/b";

    private final Gson gson = new Gson();

    public Result parse(String json) {
        SearchResponse response = gson.fromJson(json, SearchResponse.class);
        if (response == null) {
            return new Result(Collections.emptyList(), 0L);
        }

        List<Document> documents = response.docs == null
                ? Collections.emptyList()
                : response.docs;
        List<Product> products = new ArrayList<>(documents.size());

        for (int index = 0; index < documents.size(); index++) {
            Document document = documents.get(index);
            String title = valueOrDefault(document.title, "Untitled product");
            String id = valueOrDefault(
                    document.key,
                    "/unknown/" + index + "/" + title.hashCode()
            );

            products.add(new Product(
                    id,
                    title,
                    joinAuthors(document.author_name),
                    buildCoverUrl(id, document.cover_i),
                    PriceCalculator.forProductId(id),
                    document.first_publish_year,
                    document.edition_count
            ));
        }

        return new Result(products, response.numFound);
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String joinAuthors(List<String> authors) {
        if (authors == null) {
            return "Author not listed";
        }
        List<String> validAuthors = new ArrayList<>();
        for (String author : authors) {
            if (author != null && !author.trim().isEmpty()) {
                validAuthors.add(author.trim());
            }
        }
        return validAuthors.isEmpty() ? "Author not listed" : String.join(", ", validAuthors);
    }

    private String buildCoverUrl(String productId, long coverId) {
        if (coverId > 0) {
            return COVER_BASE_URL + "/id/" + coverId + "-M.jpg";
        }
        String workId = productId.startsWith("/works/")
                ? productId.substring("/works/".length())
                : productId.replace("/", "");
        return COVER_BASE_URL + "/olid/" + workId + "-M.jpg?default=false";
    }

    public static final class Result {
        private final List<Product> products;
        private final long totalItems;

        Result(List<Product> products, long totalItems) {
            this.products = products;
            this.totalItems = totalItems;
        }

        public List<Product> getProducts() {
            return products;
        }

        public long getTotalItems() {
            return totalItems;
        }
    }

    private static final class SearchResponse {
        long numFound;
        List<Document> docs;
    }

    private static final class Document {
        String key;
        String title;
        List<String> author_name;
        long cover_i;
        int first_publish_year;
        int edition_count;
    }
}
