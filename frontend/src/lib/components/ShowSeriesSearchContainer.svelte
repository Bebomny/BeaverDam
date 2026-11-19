<script lang="ts">
    import type {ShowSeriesDetailsFullResult} from "$lib/types";
    import {toaster} from "$lib/state/toaster.svelte";

    let {latestShowSeries = []}: { latestShowSeries: ShowSeriesDetailsFullResult[] } = $props();

    let searchQuery = $state('');
    let searchResults = $state<ShowSeriesDetailsFullResult[]>([]);

    let displayResults = $derived(
        searchQuery.trim().length >= 2
            ? searchResults
            : latestShowSeries
    );

    let selectedSeries = $state<ShowSeriesDetailsFullResult | null>(null);
    let isLoading = $state(false);
    let debounceTimer: ReturnType<typeof setTimeout>;

    $effect(() => {
        clearTimeout(debounceTimer);

        if (searchQuery.trim().length < 2) {
            searchResults = [];
            isLoading = false;
            return;
        }

        isLoading = true;
        debounceTimer = setTimeout(async () => {
            try {
                const res = await fetch(`/api/watchpost/series/search?query=${encodeURIComponent(searchQuery)}`);
                if (res.ok) {
                    searchResults = await res.json();
                }
            } catch (error) {
                console.error("Search fetch failed", error);
            } finally {
                isLoading = false;
            }
        }, 300);
    });

    function openModal(series: ShowSeriesDetailsFullResult) {
        selectedSeries = {...series};
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
                const index = searchResults.findIndex(s => s.showSeriesId === selectedSeries!.showSeriesId);
                if (index !== -1) {
                    searchResults[index] = {...selectedSeries};
                }
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

<div class="series-search-container">
    <div class="search-header">
        <h4 class="title-header">Search</h4>
        <input
                type="text"
                class="search-input"
                placeholder="Search series..."
                bind:value={searchQuery}
        />
        {#if isLoading}
            <span class="loading-indicator">Searching...</span>
        {/if}
    </div>

    <div class="search-results">
        {#each displayResults as series}
            <div
                    class="result-row-container"
                    role="button"
                    tabindex="0"
                    onclick={() => openModal(series)}
                    onkeydown={(e) => e.key === 'Enter' && openModal(series)}
            >
                <div class="name-id-container">
                    <h4 class="series-id">
                        ID: {series.showSeriesId}
                    </h4>

                    <h4 class="name-title" title={series.showMetadata?.localizedName || series.showSeriesName}>
                        {series.showMetadata?.localizedName || series.showSeriesName}
                    </h4>
                </div>
            </div>
        {:else}
            <div class="result-row-container">

            </div>
        {/each}
    </div>
</div>

{#if selectedSeries != null}
    <div
            class="modal-backdrop"
            role="dialog"
            onclick={() => selectedSeries = null}
            onkeydown={(e) => e.key === 'Escape' && (selectedSeries = null)}
            tabindex="-1"
    >
        <div
                class="modal-content"
                role="dialog"
                aria-modal="true"
                tabindex="0"
                onclick={(e) => e.stopPropagation()}
                onkeydown={(e) => e.key === 'Escape' && e.stopPropagation()}
        >
            <div class="modal-header">
                <div class="title-header-section">
                    <h3 class="title-header">{selectedSeries.showMetadata?.localizedName || selectedSeries.showSeriesName}</h3>
                    <span class="subtitle-header">{selectedSeries.showSeriesName}</span>
                </div>
                <button class="close-btn" type="button" aria-label="Close" tabindex="0"
                        onclick={() => selectedSeries = null}>Close
                </button>
            </div>

            <div class="modal-body">
                <div class="metadata-section">
                    <div class="cover-synopsis-container">
                        {#if selectedSeries.showMetadata?.coverImageUrl}
                            <img src={selectedSeries.showMetadata.coverImageUrl} alt="Cover" class="modal-cover"/>
                        {:else}
                            <div class="placeholder-cover">No Image</div>
                        {/if}

                        <div class="synopsis-box">
                            <span>{selectedSeries.showMetadata?.synopsis.replaceAll('<br>', '\n') || 'No synopsis available.'}</span>
                        </div>
                    </div>

                    <div class="additional-data-section">
                        <span class="additional-data-value">{selectedSeries.showMetadata?.genres || 'No genres available'}</span>
                        <span class="additional-data-value">{selectedSeries.showMetadata?.status || 'Status unavailable'}</span>
                    </div>
                </div>

                <div class="parameters-section">
                    <div class="param-row">
                        <span>Interesting</span>
                        <button class="btn series-param-btn" onclick={() => updateSeries('interesting')}>
                            {selectedSeries.interesting ? 'true' : 'false'}
                        </button>
                    </div>

                    <div class="param-row">
                        <span>Ignored</span>
                        <button class="btn series-param-btn" onclick={() => updateSeries('ignored')}>
                            {selectedSeries.ignored ? 'true' : 'false'}
                        </button>
                    </div>

                    <div class="param-row">
                        <span>Auto Download</span>
                        <button class="btn series-param-btn" onclick={() => updateSeries('autoDownload')}>
                            {selectedSeries.autoDownload ? 'true' : 'false'}
                        </button>
                    </div>

                    <div class="param-row">
                        <span>Custom Share Ratio</span>
                        <input
                                type="number"
                                step="0.1"
                                min="0"
                                class="custom-ratio-input"
                                bind:value={selectedSeries.customShareRatio}
                        />
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn cancel-btn" onclick={() => selectedSeries = null}>Cancel</button>
                <button class="btn save-btn" onclick={submitSeries}>Save Changes</button>
            </div>
        </div>
    </div>
{/if}

<style>
    .series-search-container {
        display: flex;
        flex-direction: column;
        align-items: flex-start;

        background: var(--static11);
        border-radius: 4px;
        gap: 0.1rem;
        max-width: 700px;
        height: 100%;
    }

    .search-header {
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
        gap: 0.5rem;
        width: 95%;
        padding: 0.2rem 1rem;
    }

    .title-header {
        color: var(--text-color);
        margin: 0.5rem 0 0.5rem 0;
        flex-grow: 0;
    }

    .search-input {
        padding: 0.4rem;
        border: 1px solid var(--static11);
        border-radius: 4px;
        background: var(--bg-color30);
        color: var(--text-color);

        flex-grow: 1;
    }

    .search-input:focus {
        outline: none;
        border: 1px solid var(--primary-color);
    }

    .loading-indicator {
        margin: 0;
        align-self: center;
        color: var(--subtext-color);
        /*flex-grow: 0;*/
        /*position: fixed;*/
        /*right: 0;*/
    }

    .result-row-container {
        padding: 0 1rem;
        display: flex;
        flex-direction: column;
        border-left: 3px solid transparent;
        cursor: pointer;
        justify-content: space-between;
    }

    .result-row-container:hover {
        background: var(--bg-color30);
        border-left: 3px solid var(--primary-color);
    }

    .search-results {
        border-top: 1px solid var(--primary-color);
        width: 100%;
    }

    .name-id-container {
        display: flex;
        flex-direction: row;
        gap: 0.6rem;
        align-items: center;

    }

    .series-id {
        color: var(--subtext-color);
        margin: 0.5rem 0;
    }

    .name-title {
        color: var(--text-color);
        text-wrap: wrap;
        margin: 0.5rem 0;
    }


    /* Modal */
    .modal-backdrop {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        background: rgba(0, 0, 0, 0.75);
        backdrop-filter: blur(4px);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }

    .modal-content {
        background: var(--bg-color-rgb-light);
        width: 80%;
        max-width: 700px;
        max-height: 90vh;
        border-radius: 4px;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }

    .modal-header, .modal-footer {
        padding: 0.6rem 1.5rem;
        background: var(--static11);
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .title-header-section {
        display: flex;
        flex-direction: column;
    }

    .subtitle-header {
        color: var(--subtext-color);

    }

    .modal-body {
        background: var(--bg-color30);
        padding: 1rem;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .metadata-section {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .cover-synopsis-container {
        display: flex;
        gap: 1rem;
    }

    .modal-cover {
        width: 150px;
        border-radius: 4px;
        object-fit: cover;
    }

    .synopsis-box {
        flex: 1;
        font-size: 1rem;
        line-height: 1.3;
        color: var(--subtext-color);
    }

    .additional-data-section {
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
        gap: 2rem;
        width: 100%;
    }

    .additional-data-value {
        color: var(--subtext-color);
    }

    .parameters-section {
        background: var(--static11);
        padding: 0 0.5rem;
        border-radius: 4px;
    }

    .param-row {
        display: flex;
        gap: 0.5rem;
        align-items: center;
        padding: 0.5rem 0;
    }

    .param-row span {
        color: var(--subtext-color);
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
        cursor: pointer;
    }

    .series-param-btn {
        background: var(--primary-color);
    }

    .series-param-btn:hover {
        background: var(--primary-color80);
    }

    .save-btn {
        background: var(--primary-color);
        color: var(--text-color);
    }

    .save-btn:hover {
        background: var(--primary-color80);
    }

    .cancel-btn {
        background: var(--cancel-color);
        color: var(--text-color);
    }

    .cancel-btn:hover {
        background: var(--cancel-color-hover);
    }

    .close-btn {
        background: transparent;
        border: none;
        font-size: 1rem;
        cursor: pointer;
        color: var(--cancel-color);
    }

    .close-btn:hover {
        color: var(--cancel-color-hover);
    }

    .custom-ratio-input {
        border: 1px solid var(--bg-color70);
        background: transparent;
        color: var(--text-color);
        padding: 0.5rem;
        border-radius: 3px;
        font-size: 0.8rem;
    }

    .custom-ratio-input:focus {
        outline: none;
        border-color: var(--primary-color);
    }
</style>