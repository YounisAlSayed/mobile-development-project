package com.example.recyclerviewwebservice.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public final class OpenLibraryWorkParser {
    private final Gson gson = new Gson();

    public String parseDescription(String json) {
        WorkResponse response = gson.fromJson(json, WorkResponse.class);
        if (response == null) {
            return "";
        }

        String description = readTextValue(response.description);
        if (description.isEmpty()) {
            description = readTextValue(response.firstSentence);
        }
        if (description.isEmpty()) {
            description = buildSubjectSummary(response.subjects);
        }
        return description
                .replaceAll("\\[[^\\]]+]\\([^)]*\\)\\.{0,3}", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String readTextValue(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return "";
        }
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            return value.getAsString().trim();
        }
        if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            JsonElement nestedValue = object.get("value");
            if (nestedValue != null && nestedValue.isJsonPrimitive()) {
                return nestedValue.getAsString().trim();
            }
        }
        return "";
    }

    private String buildSubjectSummary(List<String> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return "";
        }
        List<String> values = new ArrayList<>();
        int limit = Math.min(subjects.size(), 6);
        for (int index = 0; index < limit; index++) {
            String subject = subjects.get(index);
            if (subject != null && !subject.trim().isEmpty()) {
                values.add(subject.trim());
            }
        }
        return values.isEmpty() ? "" : "Subjects include " + String.join(", ", values) + ".";
    }

    private static final class WorkResponse {
        JsonElement description;

        @SerializedName("first_sentence")
        JsonElement firstSentence;

        List<String> subjects;
    }
}
