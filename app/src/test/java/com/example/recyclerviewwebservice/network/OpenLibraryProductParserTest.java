package com.example.recyclerviewwebservice.network;

import static org.junit.Assert.assertEquals;

import com.example.recyclerviewwebservice.model.Product;

import org.junit.Test;

public class OpenLibraryProductParserTest {
    private final OpenLibraryProductParser parser = new OpenLibraryProductParser();

    @Test
    public void parsesSearchResultIntoProductObjects() {
        String json = "{"
                + "\"numFound\":123,"
                + "\"docs\":[{"
                + "\"key\":\"/works/OL17508740W\","
                + "\"title\":\"The Summer I Turned Pretty Trilogy\","
                + "\"author_name\":[\"Jenny Han\"],"
                + "\"cover_i\":123,"
                + "\"first_publish_year\":2009,"
                + "\"edition_count\":12"
                + "}]}";

        OpenLibraryProductParser.Result result = parser.parse(json);
        Product product = result.getProducts().get(0);

        assertEquals(123L, result.getTotalItems());
        assertEquals(1, result.getProducts().size());
        assertEquals("/works/OL17508740W", product.getId());
        assertEquals("The Summer I Turned Pretty Trilogy", product.getTitle());
        assertEquals("Jenny Han", product.getSubtitle());
        assertEquals("https://covers.openlibrary.org/b/id/123-M.jpg", product.getImageUrl());
        assertEquals(5630, product.getPriceCents());
        assertEquals(2009, product.getFirstPublishYear());
        assertEquals(12, product.getEditionCount());
    }

    @Test
    public void suppliesFallbackValuesForIncompleteDocuments() {
        String json = "{\"numFound\":1,\"docs\":[{\"key\":\"/works/OL1W\"}]}";

        OpenLibraryProductParser.Result result = parser.parse(json);
        Product product = result.getProducts().get(0);

        assertEquals(1L, result.getTotalItems());
        assertEquals("Untitled product", product.getTitle());
        assertEquals("Author not listed", product.getSubtitle());
        assertEquals(
                "https://covers.openlibrary.org/b/olid/OL1W-M.jpg?default=false",
                product.getImageUrl()
        );
    }
}
