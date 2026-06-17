package dev.bebomny.beaverdam.watchpost.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AniListDto {
    public record GraphQLRequest(String query, Object variables) {}
    public record SearchVariables(String search) {}
    public record IdVariables(Integer id) {}

    public record Response(Data data) {}
    public record Data(@JsonProperty("Media") Media media) {}

    public record Title(String english, String romaji) {}
    public record CoverImage(String large) {}

    public record Media(
            Long id,
            Long idMal,
            Title title,
            CoverImage coverImage,
            String description,
            List<String> genres,
            String status
    ) {
        public String getBestTitle() {
            if (title == null) return "Unknown Title";
            if (title.english() != null && !title.english().isBlank()) return title.english();
            if (title.romaji() != null && !title.romaji().isBlank()) return title.romaji();
            return "Unknown Title";
        }

        public String getGenresAsString() {
            if (genres == null || genres.isEmpty()) return null;
            return String.join(", ", genres);
        }
    }
}
