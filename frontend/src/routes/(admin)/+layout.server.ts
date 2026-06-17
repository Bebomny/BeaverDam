import type { LayoutServerLoad } from './$types';
import {redirect} from "@sveltejs/kit";

export const load: LayoutServerLoad = async ({ locals }) => {
    if(!locals.isAdmin) {
        throw redirect(303, '/leaderboard')
    }

    const userEmail = locals.userEmail || 'unknown@mail';

    return {
        userEmail: userEmail
    };
}