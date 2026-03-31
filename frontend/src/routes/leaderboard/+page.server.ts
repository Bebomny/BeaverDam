import type { PageServerLoad } from './$types';
import {error} from "@sveltejs/kit";
import type {MonitoredGuildResult} from "$lib/types";

const IS_DOCKER = process.env.NODE_ENV === "production";
const BACKEND_URL = IS_DOCKER ? "http://beaverdam:8080" : "http://localhost:8080";

export const load: PageServerLoad = async ({ fetch }) => {
    try {
        const response = await fetch(`${BACKEND_URL}/api/discord/monitoredguilds`);

        if (!response.ok) {
            throw error(response.status, 'Failed to fetch guilds');
        }

        const monitoredGuilds: MonitoredGuildResult[] = await response.json();

        return {
            monitoredGuilds
        };
    } catch (err) {
        console.error(err);
        return { monitoredGuilds: [] };
    }
}