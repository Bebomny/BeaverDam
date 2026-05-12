<script>
    import ShowSeriesContainer from "$lib/components/ShowSeriesContainer.svelte";
    import MissingMetadataContainer from "$lib/components/MissingMetadataContainer.svelte";
    import WatchpostStatisticsContainer from "$lib/components/WatchpostStatisticsContainer.svelte";
    import ShowSeriesSearchContainer from "$lib/components/ShowSeriesSearchContainer.svelte";

    let { data } = $props();
</script>

<main>
    <div class="feed-dashboard-header">
        <div class ="watchpost-series-search">
            <ShowSeriesSearchContainer latestShowSeries={data.latestShowSeries}/>
        </div>

        <div class="watchpost-stats-container">
            <WatchpostStatisticsContainer stats={data.stats} />
        </div>
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
        height: auto;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        margin-bottom: 0.5rem;
        gap: 0.5rem;
        align-items: stretch;
    }

    .watchpost-series-search {
        flex-grow: 1;
    }

    .dashboard-content {
        display: flex;
        gap: 1rem;
        flex-direction: row;
        flex-wrap: wrap;
        justify-content: space-between;
    }

    .new-show-series-container {
        flex-grow: 1;
    }

    .missing-metadata-container {
        flex-grow: 1;
    }
</style>