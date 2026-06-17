import {error} from "@sveltejs/kit";
import type {AnimeItemDetailsResult} from "$lib/types";
import type {PageServerLoad} from "../../../../../.svelte-kit/types/src/routes/(admin)/watchpost/$types";

const IS_DOCKER = process.env.NODE_ENV === "production";
const BACKEND_URL = IS_DOCKER ? "http://beaverdam:8080" : "http://localhost:8080";

export const load: PageServerLoad = async ({ fetch, locals }) => {
    try {
        const userEmail = locals.userEmail || 'unknown@mail';

        const response = await fetch(`${BACKEND_URL}/api/watchpost/latest?interesting=true&page=0&size=50`, {
            method: "GET",
            headers: {
                'X-User-Email': userEmail,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw error(response.status, 'Failed to fetch latest anime episodes')
        }

        const animeItems: AnimeItemDetailsResult[] = await response.json();

        return {
            animeItems
        };
    } catch (err) {
        console.error("Error fetching anime data:", err);
        return { animeItems: [] };
    }
};