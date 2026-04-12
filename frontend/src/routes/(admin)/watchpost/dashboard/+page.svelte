<script>
    import ShowSeriesContainer from "$lib/components/ShowSeriesContainer.svelte";
    import MissingMetadataContainer from "$lib/components/MissingMetadataContainer.svelte";

    let { data } = $props();
</script>

<main>
    <div class="feed-dashboard-header">
        <span>bajo jajo</span>
    </div>
    <div class="dashboard-content">
        <div class="new-show-series-container">
            <ShowSeriesContainer showSeries={data.unsubmittedShowSeries} />
        </div>

        <div class="missing-metadata-container">
            <MissingMetadataContainer showSeries={data.showSeriesWithoutMetadata} />
        </div>
    </div>
</main>

<style>
    main {
        padding: 1rem;
        margin: 0 auto;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    }

    .feed-dashboard-header {
        height: 5rem;
    }

    .dashboard-content {
        display: flex;
        gap: 1rem;
        flex-direction: column;
    }
</style>