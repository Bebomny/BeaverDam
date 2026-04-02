<script lang="ts">
    import GuildTable from "$lib/components/GuildTable.svelte";

    let { data } = $props();
</script>

<main>
    <h1>Voice Leaderboards</h1>

    {#each data.monitoredGuilds as guild}
        <GuildTable guildId={guild.guildId} />
    {/each}
</main>

<style>
    main {
        padding: 2rem;
        background-color: #0c0c0f;
        color: #e2e8f0;
        min-height: 100vh;
        font-family: sans-serif;
    }

    h1 {
        margin-bottom: 0.5rem;
    }
</style>