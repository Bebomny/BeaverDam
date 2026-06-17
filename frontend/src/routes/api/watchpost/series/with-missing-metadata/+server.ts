import {error, json, type RequestHandler} from "@sveltejs/kit";
import type {ShowSeriesStateDetailsResult} from "$lib/types";

const IS_DOCKER = process.env.NODE_ENV === 'production';
const BACKEND_URL = IS_DOCKER ? 'http://beaverdam:8080' : 'http://localhost:8080';

export const GET: RequestHandler = async ({url, locals, fetch}) => {
    if (!locals.isAdmin) {
        throw error(403)
    }

    const limit = url.searchParams.get("limit") || "50";
    const page = url.searchParams.get("page") || "0";

    try {
        const userEmail = locals.userEmail || 'unknown@mail'

        const response = await fetch(`${BACKEND_URL}/api/watchpost/series/missing-metadata?page=${page}&limit=${limit}`, {
            method: 'GET',
            headers: {
                'X-User-Email': userEmail,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw error(response.status, "Backend failed to return showSeries data");
        }

        const data: ShowSeriesStateDetailsResult = await response.json();

        return json(data)
    } catch (err) {
        console.error("BFF fetch error", err);
        throw error(500, "Internal Server Error connecting to backend");
    }
}