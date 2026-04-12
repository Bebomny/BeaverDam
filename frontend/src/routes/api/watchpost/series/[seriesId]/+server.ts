import {error, json, type RequestHandler} from "@sveltejs/kit";

const IS_DOCKER = process.env.NODE_ENV === 'production';
const BACKEND_URL = IS_DOCKER ? 'http://beaverdam:8080' : 'http://localhost:8080';

export const PATCH: RequestHandler = async ({ request, locals, fetch, params }) => {
    if (!locals.isAdmin) {
        throw error(403, 'Unauthorized');
    }

    const seriesId = params.seriesId;

    try {
        const body = await request.json();
        const userEmail = locals.userEmail || 'unknown@mail';

        const response = await fetch(`${BACKEND_URL}/api/watchpost/series/${seriesId}`, {
            method: 'PATCH',
            headers: {
                'X-User-Email': userEmail,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            throw error(response.status, 'Backend update failed');
        }

        return json({ success: true });
    } catch (err) {
        console.error("BFF patch error:", err);
        throw error(500, 'Internal Server Error');
    }
}