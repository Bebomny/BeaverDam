package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.watchpost.dto.AniListDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.Optional;

@Slf4j
@Service
public class AniListApiService {

    private final RestClient restClient;

    private static final String SEARCH_QUERY = """
            query ($search: String) {
              Media (search: $search, type: ANIME) {
                id
                idMal
                title {
                  english
                  romaji
                }
                coverImage {
                  large
                }
                description(asHtml: false)
                genres
                status
              }
            }
            """;

    public AniListApiService(@Qualifier("vpnHttpClient") HttpClient vpnHttpClient) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(vpnHttpClient);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("https://graphql.anilist.co")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Optional<AniListDto.Media> fetchAnimeMetadata(String cleanTitle) {
        AniListDto.GraphQLRequest requestPayload = new AniListDto.GraphQLRequest(SEARCH_QUERY, new AniListDto.Variables(cleanTitle));

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                AniListDto.Response response = restClient.post()
                        .body(requestPayload)
                        .retrieve()
                        .body(AniListDto.Response.class);

                if (response != null && response.data() != null && response.data().media() != null) {
                    return Optional.of(response.data().media());
                }
                break;
            } catch (HttpClientErrorException.NotFound e) {
                log.atWarn().log("AniList found no results for: {}", cleanTitle);
                //TODO: make this an alert, and make the user assign this manually
                break;
            } catch (HttpClientErrorException.TooManyRequests e) {
                log.atWarn().log("AniList Rate Limit hit on attempt {}/{} for {}", attempt, maxRetries, cleanTitle);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return Optional.empty();
                }
            } catch (Exception e) {
                log.atError().log("Failed to fetch data from AniList for {}", cleanTitle, e);
                break;
            }
        }

        return Optional.empty();
    }
}
