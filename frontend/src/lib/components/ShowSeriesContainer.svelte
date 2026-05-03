<script lang="ts">
    import type {ShowSeriesDetailsFullResult} from "$lib/types";
    import {toaster} from "$lib/state/toaster.svelte";

    let {showSeries = []}: { showSeries: ShowSeriesDetailsFullResult[] } = $props();

    let localSeries = $state<ShowSeriesDetailsFullResult[]>([]);

    $effect(() => {
        localSeries = showSeries;
    });

    let selectedSeries = $state<ShowSeriesDetailsFullResult | null>(null);

    let isExpanded = $state(true);

    function selectShowSeries(series: ShowSeriesDetailsFullResult) {
        if (!series) return;

        // if (selectedSeries !== null && series.showSeriesId === selectedSeries.showSeriesId) {
        //     selectedSeries = null;
        //     return;
        // }

        selectedSeries = series;
    }

    function updateSeries(param: 'interesting' | 'ignored' | 'autoDownload') {
        if (!selectedSeries) return;

        selectedSeries[param] = !selectedSeries[param];

        return;
    }

    async function submitSeries() {
        if (!selectedSeries) return;

        try {
            const response = await fetch(`/api/watchpost/series/${selectedSeries.showSeriesId}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    interesting: selectedSeries.interesting,
                    ignored: selectedSeries.ignored,
                    autoDownload: selectedSeries.autoDownload,
                    customShareRatio: selectedSeries.customShareRatio
                })
            });

            if (response.ok) {
                localSeries = localSeries.filter(s => s.showSeriesId !== selectedSeries!.showSeriesId);
                selectedSeries = null;

                toaster.add('Series submitted!')
            } else {
                toaster.add('Failed to submit series.!')
            }
        } catch (error) {
            console.error("Submission failed:", error);
            toaster.add('Network error during submission', 'error');
        }
    }
</script>

<div class="new-show-series-container">
    <div class="new-show-series-header">
        <button class="title-header toggle-btn"
                type="button"
                onclick={() => isExpanded = !isExpanded}>
            {isExpanded ? '▼' : '▶'}
        </button>
        <h3 class="title-header">Unsubmitted Show Series</h3>
    </div>
    {#if isExpanded}
        {#each localSeries as series}
            <div class="series-row-container"
                 role="dialog"
                 tabindex="0"
                 onclick={() => selectShowSeries(series)}
                 onkeydown={(e) => e.key === 'Enter' || e.key === 'ArrowDown' && selectShowSeries(series)}
            >
                <div class="name-id-container">
                    <h4 class="series-id">
                        ID: {series.showSeriesId}
                    </h4>

                    <h4 class="name-title" title={series.showSeriesName}>
                        {series.showSeriesName}
                    </h4>

                    {#if selectedSeries !== null && selectedSeries.showSeriesId === series.showSeriesId}
                        <button class="btn series-submit-btn"
                                type="button"
                                onclick={(e) => {e.stopPropagation(); submitSeries()}}
                        >
                            Submit
                        </button>
                    {/if}
                </div>

                {#if selectedSeries !== null && selectedSeries.showSeriesId === series.showSeriesId}
                    <div class="series-details-container">
                        <div class="series-info-container">
                            <div class="series-param-container">
                                <p class="series-param-info">
                                    Interesting
                                </p>
                                <p class="series-param-value">
                                    {selectedSeries.interesting}
                                </p>
                                <button class="btn series-param-btn"
                                        type="button"
                                        onclick={(e) => {e.stopPropagation(); updateSeries('interesting')}}
                                >
                                    Change
                                </button>
                            </div>
                            <div class="series-param-container">
                                <p class="series-param-info">
                                    Ignored
                                </p>
                                <p class="series-param-value">
                                    {selectedSeries.ignored}
                                </p>
                                <button class="btn series-param-btn"
                                        type="button"
                                        onclick={(e) => {e.stopPropagation(); updateSeries('ignored')}}
                                >
                                    Change
                                </button>
                            </div>
                            <div class="series-param-container">
                                <p class="series-param-info">
                                    Auto Download
                                </p>
                                <p class="series-param-value">
                                    {selectedSeries.autoDownload}
                                </p>
                                <button class="btn series-param-btn"
                                        type="button"
                                        onclick={(e) => {e.stopPropagation(); updateSeries('autoDownload')}}
                                >
                                    Change
                                </button>
                            </div>
                            <div class="series-param-container">
                                <p class="series-param-info">
                                    Custom Share Ratio
                                </p>
                                <input class="custom-ratio-input"
                                       type="text"
                                       placeholder={selectedSeries.customShareRatio}
                                       bind:value={selectedSeries.customShareRatio}
                                >
                            </div>
                        </div>

                        {#if selectedSeries.showMetadata !== null}
                            <div class="series-synopsis-container">
                                <p class="series-synopsis-title">
                                    Synopsis
                                </p>
                                <span class="series-synopsis">
                                {selectedSeries.showMetadata.synopsis}
                            </span>
                            </div>
                        {/if}

                        <div class="series-image-container">
                            {#if series.showMetadata !== null && series.showMetadata.coverImageUrl}
                                <img src={series.showMetadata.coverImageUrl}
                                     alt={series.showSeriesName || 'Anime Cover'}
                                     loading="lazy"/>
                            {:else}
                                <div class="placeholder-image">
                                    <span>{(series.showSeriesName || '?').charAt(0)}</span>
                                </div>
                            {/if}
                        </div>
                    </div>
                {/if}
            </div>
        {:else}
            <div class="empty-state">
                <p>All series were submitted, woo hoo!</p>
            </div>
        {/each}
    {/if}
</div>

<style>
    .new-show-series-container {
        display: flex;
        flex-direction: column;
        gap: 0.1rem;
        /*background: var(--bg-color30);*/
        background: var(--static11);
        border-radius: 4px;
    }

    .new-show-series-header {
        padding: 0 1rem;
        border-bottom: 1px solid var(--primary-color);
        display: flex;
        justify-content: flex-start;
        align-items: flex-start;
        gap: 1rem;
    }

    .title-header {
        color: var(--text-color);
        margin: 0.7rem 0;
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
        flex-direction: column;
        border-left: 3px solid transparent;
    }

    .series-row-container:hover {
        background: var(--bg-color30);
        border-left: 3px solid var(--primary-color);
    }

    .name-id-container {
        display: flex;
        flex-direction: row;
        gap: 0.6rem;
        align-items: center;

    }

    .name-title {
        color: var(--text-color);
        text-wrap: wrap;
        margin: 0.5rem 0;
    }

    .series-id {
        color: var(--subtext-color);
        margin: 0.5rem 0;
    }

    .series-details-container {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        max-height: 16rem;
    }

    .series-info-container {
        /*flex-grow: 1;*/
        margin-top: 0.5rem;
        display: flex;
        flex-direction: column;

        gap: 1rem;
    }

    .series-synopsis-container {
        padding: 1rem;
        text-wrap: wrap;
        flex-shrink: 99999;
        color: var(--subtext-color);
    }

    .series-image-container {
        position: relative;
        padding-inline: 0.7rem;
        padding-top: 0.7rem;
        padding-bottom: 0.7rem;
        width: auto;
        height: 200px;
    }

    .series-image-container img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 6px;
    }

    .placeholder-image {
        width: 100%;
        height: 100%;
        background: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 5rem;
        font-weight: bold;
        color: #0a0a0a20;
        border-radius: 6px;
    }

    .series-param-container {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 0.5rem
    }

    .series-param-info {
        padding: 0 0.1rem;
        margin: 0 0 0 0;

        color: var(--subtext-color);
    }

    .series-param-value {
        color: var(--text-color);
        font-weight: bold;
        margin: 0;
    }

    .btn {
        text-decoration: none;
        padding: 0.3rem 0.6rem;
        border: 0;
        border-radius: 6px;
        font-size: 0.8rem;
        font-weight: 600;
        transition: background 0.2s;
        color: var(--text-color);

    }

    .series-param-btn {
        background: var(--primary-color);
    }

    .series-param-btn:hover {
        background: var(--primary-color80);
    }

    .series-submit-btn {
        background: var(--primary-color);
    }

    .series-submit-btn:hover {
        background: var(--primary-color80);
    }

    .custom-ratio-input {
        /*flex-grow: 1;*/
        border: 1px solid var(--bg-color70);
        background: transparent;
        color: #e2e8f0;
        padding: 0.5rem;
        border-radius: 3px;
        font-size: 0.8rem;
    }

    .custom-ratio-input:focus {
        outline: none;
        border-color: var(--primary-color);
    }

    .empty-state {
        color: var(--subtext-color);
    }

    .empty-state h3 {
        margin: 0.7rem 0;
    }
</style>