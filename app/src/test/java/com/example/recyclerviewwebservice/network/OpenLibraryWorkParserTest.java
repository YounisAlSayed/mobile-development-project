package com.example.recyclerviewwebservice.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OpenLibraryWorkParserTest {
    private final OpenLibraryWorkParser parser = new OpenLibraryWorkParser();

    @Test
    public void readsObjectDescriptionAndRemovesMarkdownLinks() {
        String json = "{\"description\":{\"value\":"
                + "\"A story [source](https://example.com)... about summer.\"}}";

        assertEquals("A story about summer.", parser.parseDescription(json));
    }

    @Test
    public void readsStringDescription() {
        String json = "{\"description\":\"A direct description.\"}";

        assertEquals("A direct description.", parser.parseDescription(json));
    }

    @Test
    public void fallsBackToFirstSentenceAndThenSubjects() {
        String firstSentence = "{\"first_sentence\":{\"value\":\"The opening line.\"}}";
        String subjects = "{\"subjects\":[\"Fiction\",\"Summer\",\"Family\"]}";

        assertEquals("The opening line.", parser.parseDescription(firstSentence));
        assertEquals(
                "Subjects include Fiction, Summer, Family.",
                parser.parseDescription(subjects)
        );
    }
}
