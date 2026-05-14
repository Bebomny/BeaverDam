<script lang="ts">
    import WatchpostAnimeGrid from "$lib/components/WatchpostAnimeGrid.svelte";

    //TODO: get statistics for today and display them at the top

    let {data} = $props();

</script>

<main class="main-container">
    <header class="page-header">
        <h1>Latest Interesting Episodes</h1>
        <p>The 50 most recent interesting anime episodes captured by Watchpost.</p>
    </header>

    <div class="anime-grid-container">
        <WatchpostAnimeGrid items={data.animeItems} interestingOnly={true} />
    </div>

</main>



<style>

    .main-container {
        padding: 1rem;
        max-width: 95%;
        margin: 0 auto;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;

        display: flex;
        flex-direction: column;
        justify-content: center;
        align-content: center;
    }

    .page-header {
        margin-bottom: 2rem;
    }

    .page-header h1 {
        color: var(--text-color);
        margin: 0 0 0.5rem 0;
        font-size: 2rem;
        width: auto;
    }

    .page-header p {
        color: var(--subtext-color);
        margin: 0;
    }
</style>