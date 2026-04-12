import {error, json, type RequestHandler} from "@sveltejs/kit";

const IS_DOCKER = process.env.NODE_ENV === 'production';
const BACKEND_URL = IS_DOCKER ? 'http://beaverdam:8080' : 'http://localhost:8080';

export const POST: RequestHandler = async ({ request, locals, fetch }) => {
  if (!locals.isAdmin) {
      throw error(403)
  }

  const {seriesId, anilistId } = await request.json();

  if(!seriesId || !anilistId) {
      throw error(400, 'Missing seriesId or anilistId')
  }

  try {
      const userEmail = locals.userEmail || 'unknown@mail'

      const response = await fetch(`${BACKEND_URL}/api/watchpost/series/${seriesId}/metadata/anilist/${anilistId}`, {
          method: 'POST',
          headers: {
              'X-User-Email': userEmail,
              'Content-Type': 'application/json'
          }
      });

      if (!response.ok) {
          throw error(response.status, 'Backend failed to assign anilist ID');
      }

      return json({success: true});
  } catch (err) {
      console.error("BFF fetch error on manual assignment:", err);
      throw error(500, 'Internal Server Error connecting to backend');
  }
};