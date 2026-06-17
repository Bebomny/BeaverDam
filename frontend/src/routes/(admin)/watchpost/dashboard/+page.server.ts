import type {PageServerLoad} from "../../../../../.svelte-kit/types/src/routes/(admin)/watchpost/$types";

const IS_DOCKER = process.env.NODE_ENV === "production";
const BACKEND_URL = IS_DOCKER ? "http://beaverdam:8080" : "http://localhost:8080";

export const load: PageServerLoad = async ({fetch, locals}) => {
    const userEmail = locals.userEmail || 'unknown@mail';

    const requestOptions = {
        method: "GET",
        headers: {
            'X-User-Email': userEmail,
            'Content-Type': 'application/json'
        }
    };

    try {
        const [unsubmittedSeriesResponse, missingMetadataResponse, statsResponse, latestSeriesResponse] = await Promise.all([
            fetch(`${BACKEND_URL}/api/watchpost/series/latest?unsubmitted=true&page=0&size=50`, requestOptions),
            fetch(`${BACKEND_URL}/api/watchpost/series/missing-metadata?page=0&size=50`, requestOptions),
            fetch(`${BACKEND_URL}/api/watchpost/stats`, requestOptions),
            fetch(`${BACKEND_URL}/api/watchpost/series/latest?unsubmitted=false&page=0&size=5`, requestOptions)
        ]);

        if (!unsubmittedSeriesResponse.ok || !missingMetadataResponse.ok || !statsResponse.ok || !latestSeriesResponse.ok) {
            throw new Error(`Failed to fetch dashboard data. Statuses: 
                ${unsubmittedSeriesResponse.status}, ${missingMetadataResponse.status}, ${statsResponse.status}, ${latestSeriesResponse.status}`);
        }

        const [unsubmittedShowSeries, showSeriesWithoutMetadata, stats, latestShowSeries] = await Promise.all([
            unsubmittedSeriesResponse.json(),
            missingMetadataResponse.json(),
            statsResponse.json(),
            latestSeriesResponse.json()
        ]);

        return {
            unsubmittedShowSeries,
            showSeriesWithoutMetadata,
            stats,
            latestShowSeries
        };
    } catch (err) {
        console.error("Anime Dashboard load error:", err);

        return {
            unsubmittedShowSeries: [],
            showSeriesWithoutMetadata: [],
            stats: {
                interestingEpisodesPastWeek: -1,
                newEpisodesThisSeason: -1,
                totalEpisodesToday: -1,
                newSeriesThisSeason: -1,
                totalEpisodes: -1,
                totalInterestingEpisodes: -1,
                totalShowSeries: -1,
                totalInterestingShowSeries: 1,
            },
            latestShowSeries: []
        };
    }
};