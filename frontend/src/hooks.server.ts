import type {Handle} from "@sveltejs/kit";
import { WEB_ADMIN_EMAIL } from '$env/static/private';
import { dev } from '$app/environment';

const ADMIN_EMAIL: string = WEB_ADMIN_EMAIL || '';

export const handle: Handle = async ({event, resolve}) => {
    const cfEmail = event.request.headers.get('Cf-Access-Authenticated-User-Email');

    event.locals.userEmail = cfEmail;
    event.locals.isAdmin = cfEmail !== null && ADMIN_EMAIL.includes(cfEmail);

    if (dev) { //process.env.NODE_ENV !== 'production'
        event.locals.isAdmin = true;
        event.locals.userEmail = ADMIN_EMAIL;
    }

    return resolve(event);
};