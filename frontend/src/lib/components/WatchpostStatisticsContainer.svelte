<script lang="ts">
    import type {WatchpostStatsResult} from "$lib/types";

    let {stats}: {stats: WatchpostStatsResult} = $props();
</script>

{#snippet statRow(label: string, value: number | string)}
    <div class="stats-row-container">
        <p class="stats-info">
            {label}
        </p>
        <p class="stats-value">
            {value}
        </p>
    </div>
{/snippet}

<div class="statistics-container">
    <div class="stats-header">
        <h4 class="title-header">Statistics</h4>
    </div>
    <div class="stats-body">
        {@render statRow("Interesting Episodes past Week:", stats.interestingEpisodesPastWeek)}
        {@render statRow("New Episodes this Season:", stats.newEpisodesThisSeason)}
        {@render statRow("Total Episodes Today:", stats.totalEpisodesToday)}
        {@render statRow("New Series this Season:", stats.newSeriesThisSeason)}
        {@render statRow("Total Episodes:", stats.totalEpisodes)}
        {@render statRow("Total Interesting Episodes:", stats.totalInterestingEpisodes)}
        {@render statRow("Total Known Series:", stats.totalShowSeries)}
        {@render statRow("Total Known Interesting Series:", stats.totalInterestingShowSeries)}
    </div>
</div>

<style>
    .statistics-container {
        display: flex;
        flex-direction: column;
        align-items: flex-start;

        background: var(--static11);
        border-radius: 4px;
        padding: 0.4rem 0 0.4rem 0;
    }

    .stats-header {
        padding: 0 0 0 0.5rem;
        display: flex;
        justify-content: flex-start;
        align-items: flex-start;
        gap: 1rem;
    }

    .title-header {
        color: var(--text-color);
        margin: 0 0 0.5rem 0;
    }

    .stats-body {
        padding: 0 0.5rem;
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }

    .stats-row-container {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        gap: 1rem;
        width: 100%;
    }

    .stats-info {
        padding: 0 0.1rem;
        margin: 0 0 0 0;
        color: var(--subtext-color);
    }

    .stats-value {
        font-weight: bold;
        margin: 0;
        color: var(--text-color);
    }
</style>