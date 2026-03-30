import type { PageServerLoad } from './$types';
import {error} from "@sveltejs/kit";
import type {MonitoredGuildResult} from "$lib/types";

export const load: PageServerLoad = async ({ fetch }) => {
    try {
        const response = await fetch('http://localhost:8080/api/discord/monitoredguilds');

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