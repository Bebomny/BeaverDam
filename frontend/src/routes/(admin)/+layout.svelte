<script lang="ts">
    import ToastContainer from "$lib/components/ToastContainer.svelte";
    import {toaster} from "$lib/state/toaster.svelte";

    let { children, data } = $props();

    $effect(() => {
        const sse = new EventSource('/api/watchpost/stream')

       sse.onmessage = (event: MessageEvent) => {
           if (!event.data.startsWith("TOAST")) return;

           const payload = event.data;

           switch (payload) {
               case "TOAST_DOWNLOAD_SUCCESS": {
                   toaster.add("Episode download finished!")
               }
           }
       }
    });
</script>

<div class="app-layout">
    <aside class="sidebar-nav">
        <div class="app-title">
            <span class="logo">BeaverDam</span>
            <span class="badge">Super User</span>
        </div>

        <nav class="nav-links">
            <a href="/dashboard">Dashboard</a>
            <a href="/watchpost/dashboard">Feed Dashboard</a>
            <a href="/watchpost/animefeed">Anime Feed</a>
            <a href="/watchpost/fullfeed">Full Feed</a>
            <a href="/settings">Settings</a>
        </nav>

        <div class="user-profile">
            <span class="user-email">{data.userEmail}</span>
        </div>

    </aside>

    <main class="page-content">
        {@render children()}
    </main>

    <ToastContainer />
</div>

<style>
    :root {
        /*V4*/
        --text-color: hsl(120, 17%, 98%);
        --subtext-color: hsl(120, 17%, 98%, 75%);
        --bg-color: hsl(120, 17%, 5%);
        --bg-color90: hsl(120, 17%, 5%, 90%);
        --bg-color80: hsl(120, 17%, 5%, 80%);
        --bg-color70: hsl(120, 17%, 5%, 70%);
        --bg-color60: hsl(120, 17%, 5%, 60%);
        --bg-color50: hsl(120, 17%, 5%, 50%);
        --bg-color40: hsl(120, 17%, 5%, 40%);
        --bg-color30: hsl(120, 17%, 5%, 30%);
        --primary-color: hsl(125, 28%, 52%);
        --primary-color80: hsl(125, 28%, 52%, 80%);
        --secondary-color: hsl(120, 46%, 75%);
        --secondary-color30: hsl(120, 46%, 75%, 10%);
        --secondary-color-darker: hsl(120, 14%, 19%);
        --accent-color: hsl(120, 54%, 62%);
        --accent-color80: hsl(120, 54%, 62%, 80%);

        --static11: rgba(255, 255, 255, 0.03);
    }

    .app-layout {

        font: 400 14px "Helvetica Neue", Helvetica, Arial, sans-serif;
        display: flex;
        min-height: 100vh;
        background-color: var(--bg-color90);
    }

    .sidebar-nav {
        width: 220px;
        /*border-right: 1px solid var(--primary-color);*/
        /*border-top: 1px solid var(--primary-color);*/
        /*background-color: var(--bg-color70);*/
        background-color: var(--static11);
        border-radius: 8px 8px 3px 3px;
        display: flex;
        flex-direction: column;
        margin-top: 10px;
        margin-bottom: 10px;
        margin-left: 10px;

        position: sticky;
        top: 0;
        height: 90vh;
        overflow-y: auto;
    }

    .app-title {
        padding: 1.5rem;
        border-bottom: 1px solid var(--primary-color);
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
    }

    .logo {
        font-size: 1.25rem;
        font-weight: bold;
        color: var(--text-color);
    }

    .badge {
        font-size: 0.7rem;
        background: var(--accent-color);
        color: var(--text-color);
        padding: 0.1rem 0.4rem;
        border-radius: 3px;
        width: fit-content;
        font-weight: bold;
        letter-spacing: 1px;
    }

    .nav-links {
        display: flex;
        flex-direction: column;
        padding: 1rem 0;
        flex-grow: 1;
    }

    .nav-links a {
        padding: 0.75rem 1.5rem;
        color: var(--text-color);
        text-decoration: none;
        font-weight: 500;
        transition: all 0.2s;
        border-left: 3px solid transparent;
    }

    .nav-links a:hover {
        background-color: var(--secondary-color);
        color: var(--primary-color);
        border-left-color: var(--accent-color);
    }

    .user-profile {
        padding: 1.5rem;
        border-top: 1px solid var(--primary-color);
    }

    .user-email {
        font-size: 0.8rem;
        color: var(--text-color);
        word-break: break-all;
    }

    .page-content {
        flex: 1;
        min-width: 0;
    }
</style>