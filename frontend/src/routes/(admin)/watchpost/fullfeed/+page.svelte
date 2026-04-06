<script lang="ts">
    import WatchpostAnimeGrid from "$lib/components/WatchpostAnimeGrid.svelte";

    //TODO: get statistics for today and display them at the top

    let {data} = $props();

</script>

<main>
    <header class="page-header">
        <h1>Latest Releases</h1>
        <p>The 50 most recent anime episodes captured by Watchpost.</p>
    </header>

    <WatchpostAnimeGrid items={data.animeItems} interestingOnly={false} />
</main>



<style>

    main {
        padding: 1rem;
        max-width: 95%;
        margin: 0 auto;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    }

    .page-header {
        margin-bottom: 2rem;
    }

    .page-header h1 {
        color: var(--text-color);
        margin: 0 0 0.5rem 0;
        font-size: 2rem;
    }

    .page-header p {
        color: var(--subtext-color);
        margin: 0;
    }
</style>