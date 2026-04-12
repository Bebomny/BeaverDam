<script lang="ts">
    import type {ShowSeriesStateDetailsResult} from "$lib/types";
    import {toaster} from "$lib/state/toaster.svelte";

    let {showSeries = []}: { showSeries: ShowSeriesStateDetailsResult[] } = $props();
    let localSeries = $state<ShowSeriesStateDetailsResult[]>([]);
    let isExpanded = $state(true);

    $effect(() => {
        localSeries = showSeries;
    });

    async function setOnlineId(seriesId: string, onlineId: string) {
        if (!seriesId || !onlineId) {
            return;
        }

        try {
            const res = await fetch(`/api/watchpost/series/update-anilist`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    seriesId: seriesId,
                    anilistId: parseInt(onlineId),
                })
            });

            if (res.ok) {
                localSeries = localSeries.filter(s => s.id !== seriesId);

                toaster.add("Online id '" + onlineId + "' assigned to series id '" + seriesId + "'");
            } else {
                toaster.add("Failed to assign online Id '" + onlineId + "' to series id '" + seriesId + "'");
            }
        } catch (error) {
            console.error("Submission failed:", error);
            toaster.add('Network error during submission', 'error');
        }
    }
</script>

<div class="missing-metadata-container">
    <div class="missing-metadata-header">
        <button class="title-header toggle-btn"
                type="button"
                onclick={() => isExpanded = !isExpanded}>
            {isExpanded ? '▼' : '▶'}
        </button>
        <h2 class="title-header">Series Without Metadata</h2>
    </div>
    {#if isExpanded}
        {#each localSeries as series}
            <div class="series-row-container">
                <div class="name-id-container">
                    <h3 class="series-id">
                        ID: {series.id}
                    </h3>

                    <h3 class="name-title" title={series.name}>
                        {series.name}
                    </h3>
                </div>

                <div class="set-onlineid-container">
                    <input class="set-onlineid-input"

                           placeholder="e.g. 142342"
                           bind:value={series.onlineId}
                    >
                    <button class="set-onlineid-btn"
                            type="button"
                            onclick={() => setOnlineId(series.id, series.onlineId)}>
                        Set
                    </button>
                </div>
            </div>
        {:else}
            <div class="empty-state">
                <h3>All series have assigned metadata! woo hoo!</h3>
            </div>
        {/each}
    {/if}

</div>

<style>
    .missing-metadata-container {
        display: flex;
        flex-direction: column;
        gap: 0.1rem;
        /*background: var(--bg-color30);*/
        background: var(--static11);
        border-radius: 4px;
    }

    .missing-metadata-header {
        padding: 0 1rem;
        border-bottom: 1px solid var(--primary-color);
        display: flex;
        justify-content: flex-start;
        align-items: flex-start;
        gap: 1rem;
    }

    .title-header {
        color: var(--text-color);
    }

    .toggle-btn {
        align-self: center;
        cursor: pointer;
        background: transparent;
        border: none;
    }

    .series-row-container {
        /*border-bottom: 1px solid var(--primary-color);*/
        padding: 0 1rem;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        border-left: 3px solid transparent;
    }

    .series-row-container:hover {
        background: var(--bg-color30);
        border-left: 3px solid var(--primary-color);
    }

    .name-id-container {
        display: flex;
        flex-direction: row;
        gap: 1rem;
        align-items: center;
    }

    .name-title {
        color: var(--text-color);
        text-wrap: wrap;
    }

    .series-id {
        color: var(--subtext-color);
        text-wrap: nowrap;
    }

    .set-onlineid-container {
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
        gap: 0.25rem;
        align-items: center;
    }

    .set-onlineid-input {
        border: 1px solid var(--bg-color70);
        background: transparent;
        color: var(--text-color);
        border-radius: 3px;
        font-size: 0.8rem;
    }

    .set-onlineid-input:focus {
        outline: none;
        border-color: var(--primary-color);
    }

    .set-onlineid-btn {
        text-decoration: none;
        padding: 0.3rem 0.6rem;
        border: 0;
        border-radius: 6px;
        font-size: 0.85rem;
        font-weight: 600;
        transition: background 0.2s;
        color: var(--text-color);
        background: var(--primary-color);
    }

    .set-onlineid-btn:hover {
        background: var(--primary-color80);
    }

    .empty-state {
        color: var(--subtext-color);
        padding: 0 1rem 0 1rem;
    }
</style>