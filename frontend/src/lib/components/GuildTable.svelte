<script lang="ts">
    import type {DiscordGuildVoiceChatDataResult} from "$lib/types";

    let { guildId } = $props<{ guildId: string }>();

    let currentGuildData = $state<DiscordGuildVoiceChatDataResult | null>(null);
    let isLoading = $state(true);

    type SortField = 'total' | 'self_mute' | 'self_deafen' | 'server_mute' | 'server_deafen' | 'stream' | 'suppress';
    let currentSortField = $state<SortField>('total');

    $effect(() => {
        isLoading = true;
        fetch(`/api/leaderboard?guildId=${guildId}&sort=${currentSortField}&limit=50`)
            .then(res => res.json())
            .then((data: DiscordGuildVoiceChatDataResult) => {
                currentGuildData = data;
                isLoading = false;
            })
            .catch(err => {
                console.error("Failed to update leaderboard: ", err);
                isLoading = false;
            });
    });

    function formatTime(totalSeconds: number) {
        if (!totalSeconds) return "-1h -1m";
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        return `${hours}h ${minutes}m`;
    }

    function getSortHeader(field: SortField, label: String) {
        const arrow = (currentSortField === field) ? ' ▲' : ' ▽';
        return label + arrow;
    }
</script>

<section class="guild-table-wrapper">
    {#if isLoading}
        <div class="loading-overlay">Fetching leaderboard...</div>
    {/if}

    {#if currentGuildData}
        <header class="guild-header">
            {#if currentGuildData.iconUrl}
                <img src={currentGuildData.iconUrl} alt="Guild Icon" class="guild-icon">
            {/if}
            <h1>{currentGuildData.guildName} Voice Chat Leaderboard</h1>
        </header>

        <table class="full-width-table">
            <thead>
            <tr>
                <th class="rank-col">Rank</th>
                <th>Username</th>
                <th class="sortable" onclick="{() => currentSortField = 'total'}">
                    {getSortHeader('total', 'Total Voice')}
                </th>
                <th class="sortable" onclick="{() => currentSortField = 'stream'}">
                    {getSortHeader('stream', 'Live Streams')}
                </th>
                <th class="sortable" onclick={() => currentSortField = 'suppress'}>
                     {getSortHeader('suppress', 'Suppressed')}
                </th>
                <th class="sortable" onclick={() => currentSortField = 'self_mute'}>
                    {getSortHeader('self_mute', 'Self Mutes')}
                </th>
                <th class="sortable" onclick={() => currentSortField = 'self_deafen'}>
                    {getSortHeader('self_deafen', 'Self Deafens')}
                </th>
                <th class="sortable" onclick={() => currentSortField = 'server_mute'}>
                    {getSortHeader('server_mute', 'Server Mutes')}
                </th>
                <th class="sortable" onclick={() => currentSortField = 'server_deafen'}>
                    {getSortHeader('server_deafen', 'Server Deafens')}
                </th>
            </tr>
            </thead>
            <tbody>
            {#each currentGuildData.voiceChatData as user, index}
                <tr>
                    <td class="rank-col">#{index + 1}</td>
                    <td class="username-col">
                        <img src={user.userIconUrl} alt="{user.username}'s avatar" class="user-avatar"
                             loading="lazy"/>
                        {user.username}
                    </td>
                    <td class="time-main">{formatTime(user.totalSecondsSpent)}</td>
                    <td class="time-detail">{user.streamCount}</td>
                    <td class="time-detail">{user.suppressCount}</td>
                    <td class="time-detail">{user.selfMuteCount}</td>
                    <td class="time-detail">{user.selfDeafenCount}</td>
                    <td class="time-detail">{user.serverMuteCount}</td>
                    <td class="time-detail">{user.serverDeafenCount}</td>
                </tr>
            {:else}
                <tr>
                    <td colspan="9" class="no-data">No Voice data recorded for this server yet.</td>
                </tr>
            {/each}
            </tbody>
        </table>
    {:else}
        <p>Could not load guild data.</p>
    {/if}
</section>

<style>
    .loading-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.7);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 1000;
        font-size: 1.2rem;
        font-weight: bold;
    }

    /* Guild Header Section */
    .guild-header {
        display: flex;
        align-items: center;
        gap: 1.5rem;
        padding-bottom: 2rem;
        border-bottom: 1px solid #2d2d3d;
        margin-bottom: 2rem;
    }

    .guild-icon {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        background: #333; /* Placeholder background */
    }

    /* CSS TRICK 1 EXPLAINED: */
    /* If the screen gets too narrow, the table won't squash; it will show a scrollbar. */
    .guild-table-wrapper {
        width: 100%;
        overflow-x: auto; /* Allow horizontal scroll on mobile */
        border-radius: 12px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.4);
        background: #1a1a24;
    }

    /* CSS TRICK 2 EXPLAINED: The Table Itself */
    .full-width-table {
        width: 100%; /* Stretch to fit the container */
        border-collapse: collapse; /* Merges borders for that sleek, flat look */
        min-width: 800px; /* Guarantees table doesn't get ridiculously squashed */
    }

    /* Header styling */
    .full-width-table thead tr {
        background-color: #242431;
    }

    .full-width-table th {
        text-align: left;
        padding: 1rem;
        font-weight: 600;
        color: #fff;
        font-size: 0.95rem;
        border-bottom: 1px solid #2d2d3d;
    }

    /* Row styling */
    .full-width-table td {
        padding: 1rem;
        border-bottom: 1px solid #2d2d3d;
        vertical-align: middle;
    }

    /* Row hover effect - Classic Enterprise touch! */
    .full-width-table tbody tr:hover {
        background-color: rgba(74, 222, 128, 0.05); /* Svelte Green very faintly */
    }

    /* Interactive Sort Headers */
    .sortable {
        cursor: pointer;
        user-select: none; /* Stops header text from being accidentally selected */
        transition: color 0.2s;
    }

    .sortable:hover {
        color: #4ade80; /* Highlight green on hover */
    }

    /* Column Specific Styling (Using classes keeps it clean) */
    .rank-col {
        width: 60px;
        color: #888;
        font-weight: bold;
        text-align: center;
    }

    .username-col {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        font-weight: 500;
    }

    .user-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        object-fit: cover;
    }

    .time-main {
        font-weight: bold;
        color: #fff;
    }

    .time-detail {
        color: #a0aec0;
        font-size: 0.9rem;
    }

    .no-data {
        text-align: center;
        color: #888;
        padding: 3rem;
    }
</style>