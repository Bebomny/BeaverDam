import type {PageServerLoad} from "../../../../../.svelte-kit/types/src/routes/(admin)/watchpost/$types";
import {error} from "@sveltejs/kit";
import type {ShowSeriesDetailsFullResult, ShowSeriesStateDetailsResult} from "$lib/types";

const IS_DOCKER = process.env.NODE_ENV === "production";
const BACKEND_URL = IS_DOCKER ? "http://beaverdam:8080" : "http://localhost:8080";

export const load: PageServerLoad = async ({ fetch, locals }) => {
    let unsubmittedShowSeries: ShowSeriesDetailsFullResult[] = [];
    let showSeriesWithoutMetadata: ShowSeriesStateDetailsResult[] = [];

    try {
        const userEmail = locals.userEmail || 'unknown@mail';

        const unsubmittedSeriesResponse = await fetch(`${BACKEND_URL}/api/watchpost/series/latest?unsubmitted=true&page=0&size=50`, {
            method: "GET",
            headers: {
                'X-User-Email': userEmail,
                'Content-Type': 'application/json'
            }
        });

        if (!unsubmittedSeriesResponse.ok) {
            throw error(unsubmittedSeriesResponse.status, 'Failed to fetch unsubmitted series.')
        }

        unsubmittedShowSeries = await unsubmittedSeriesResponse.json();

        const missingMetadataResponse = await fetch(`${BACKEND_URL}/api/watchpost/series/missing-metadata?page=0&size=50`, {
            method: "GET",
            headers: {
                'X-User-Email': userEmail,
                'Content-Type': 'application/json'
            }
        });

        if (!missingMetadataResponse.ok) {
            throw error(missingMetadataResponse.status, 'Failed to fetch series with missing metadata.')
        }

        showSeriesWithoutMetadata = await missingMetadataResponse.json();

        return {
            unsubmittedShowSeries,
            showSeriesWithoutMetadata
        };
    } catch (err) {
        console.error("Error fetching series data:", err);
        return {
            unsubmittedShowSeries: [],
            showSeriesWithoutMetadata: []
        };
    }
};