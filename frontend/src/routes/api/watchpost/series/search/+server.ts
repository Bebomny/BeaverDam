import {error, json, type RequestHandler} from "@sveltejs/kit";

const IS_DOCKER = process.env.NODE_ENV === 'production';
const BACKEND_URL = IS_DOCKER ? 'http://beaverdam:8080' : 'http://localhost:8080';

export const GET: RequestHandler = async ({url, locals, fetch}) => {
    if (!locals.isAdmin) {
        throw error(403);
    }

    const searchQuery = url.searchParams.get("query") || "";

    if (!searchQuery || searchQuery.trim().length < 2) {
        return json([]);
    }

    const userEmail = locals.userEmail || 'unknown@mail';

    try {
        const response = await fetch(`${BACKEND_URL}/api/watchpost/series/search?query=${encodeURIComponent(searchQuery.trim())}`, {
            method: 'GET',
            headers: {
                'X-User-Email': userEmail,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw error(response.status, "Backend search failed");
        }

        const results = await response.json();
        return  json(results);
    } catch (err) {
        console.error("BFF search proxy error: ", err);
        throw error(500, "Internal Server Error during search");
    }
};