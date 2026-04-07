import {error, type RequestHandler} from "@sveltejs/kit";

const IS_DOCKER = process.env.NODE_ENV === 'production';
const BACKEND_URL = IS_DOCKER ? 'http://beaverdam:8080' : 'http://localhost:8080';

export const GET: RequestHandler = async ({ fetch, locals, request }) => {
    const userEmail = locals.userEmail || 'unknown@mail';

    try {
        const response = await fetch(`${BACKEND_URL}/api/watchpost/stream`, {
            method: 'GET',
            headers: {
                'X-User-Email': userEmail,
                'Accept': 'text/event-stream'
            },
            signal: request.signal
        });

        if (!response.ok) {
            throw error(response.status, 'Backend stream failed.');
        }

        return new Response(response.body, {
            headers: {
                'Content-Type': 'text/event-stream',
                'Cache-Control': 'no-cache',
                'Connection': 'keep-alive'
            }
        });
    } catch (err: any) {
        if (err.name === 'AbortError') {
            console.log("Client disconnected, closing backend stream.")
            return new Response(null, { status: 204 });
        }

        console.error("Error connecting to sse stream: ", err);
        throw error(500, 'Could not establish event stream');
    }
}