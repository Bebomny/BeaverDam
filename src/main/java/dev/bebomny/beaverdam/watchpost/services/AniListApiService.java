package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.common.events.WatchpostMetadataNotFoundEvent;
import dev.bebomny.beaverdam.watchpost.dto.AniListDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher eventPublisher;

    private final RestClient restClient;

    private static final String TITLE_SEARCH_QUERY = """
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

    private static final String ID_SEARCH_QUERY = """
            query ($id: Int) {
              Media (id: $id, type: ANIME) {
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

    public AniListApiService(@Qualifier("vpnHttpClient") HttpClient vpnHttpClient, ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(vpnHttpClient);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("https://graphql.anilist.co")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Optional<AniListDto.Media> fetchAnimeMetadata(String cleanTitle, Long seriesId) {
        AniListDto.GraphQLRequest requestPayload = new AniListDto.GraphQLRequest(TITLE_SEARCH_QUERY, new AniListDto.SearchVariables(cleanTitle));

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
                eventPublisher.publishEvent(new WatchpostMetadataNotFoundEvent(cleanTitle, seriesId));
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

    public Optional<AniListDto.Media> fetchAnimeMetadataById(Long seriesId, Long aniListId) {
        AniListDto.GraphQLRequest requestPayload = new AniListDto.GraphQLRequest(ID_SEARCH_QUERY, new AniListDto.IdVariables(Math.toIntExact(aniListId)));

        try {
            AniListDto.Response response = restClient.post()
                    .body(requestPayload)
                    .retrieve()
                    .body(AniListDto.Response.class);

            if (response != null && response.data() != null && response.data().media() != null) {
                return Optional.of(response.data().media());
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.atWarn().log("AniList found no results for ID: {} while searching by id. Are you sure you got the ID correctly?", seriesId);
        } catch (Exception e) {
            log.atError().log("Failed to fetch data from AniList for ID {}", seriesId, e);
        }

        return Optional.empty();
    }
}
