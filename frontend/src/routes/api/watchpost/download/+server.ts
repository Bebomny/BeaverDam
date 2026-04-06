import {error, json} from "@sveltejs/kit";
import type {RequestHandler} from "../../../../../.svelte-kit/types/src/routes/api/leaderboard/$types";

const IS_DOCKER = process.env.NODE_ENV === 'production';
const BACKEND_URL = IS_DOCKER ? 'http://beaverdam:8080' : 'http://localhost:8080';

export const POST: RequestHandler = async ({ request, locals, fetch }) => {
    if (!locals.isAdmin) {
        throw error(403)
    }

    const { animeItemId } = await request.json();

    if(!animeItemId) {
        throw error(400, 'Missing item id')
    }

    try {
        const userEmail = locals.userEmail || 'unknown@mail'

        const response = await fetch(`${BACKEND_URL}/api/watchpost/download/${animeItemId}`, {
            method: 'POST',
            headers: {
                'X-User-Email': userEmail,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw error(response.status, 'Backend failed to download anime item');
        }

        return json({success: true});
    } catch (err) {
        console.error("BFF error on download request:", err);
        throw error(500, 'Internal Server Error connecting to backend');
    }
};