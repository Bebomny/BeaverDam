import {error, json, type RequestHandler} from "@sveltejs/kit";
import type {DiscordGuildVoiceChatDataResult} from "$lib/types";

const IS_DOCKER = process.env.NODE_ENV === "production";
const BACKEND_URL = IS_DOCKER ? "http://beaverdam:8080" : "http://localhost:8080";

export const GET: RequestHandler = async ({ url, fetch }) => {
    const guildId = url.searchParams.get("guildId")
    const sort = url.searchParams.get("sort") || "total";
    const limit = url.searchParams.get("limit") || "50";

    if (!guildId) {
        throw error(400, "guildId is required")
    }

    try {
        const response = await fetch(`${BACKEND_URL}/api/analytics/voicedata/${guildId}?sort=${sort}&limit=${limit}`)

        if (!response.ok) {
            throw error(response.status, "Backend failed to return voicechatdata data");
        }

        const data: DiscordGuildVoiceChatDataResult = await response.json();

        return json(data)
    } catch (err) {
        console.error("BFF fetch error", err);
        throw error(500, "Internal Server Error connecting to backend");
    }
}