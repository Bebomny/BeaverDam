<script lang="ts">
    import type {AnimeItemDetailsResult} from "$lib/types";
    import {invalidateAll} from "$app/navigation";

    let { items = [], interestingOnly = false }: { items: AnimeItemDetailsResult[], interestingOnly: boolean } = $props();

    let selectedAnime = $state<AnimeItemDetailsResult | null>(null)

    //Anilist id assignment
    let manualAniListId = $state<string>('');
    let isUpdating = $state<boolean>(false);
    let updateMessage = $state<string>('');

    $effect(() => {
        if (selectedAnime) {
            manualAniListId = '';
            updateMessage = '';
        }
    });

    //Auto refreshes
    $effect(() => {
        const sse = new EventSource('api/watchpost/stream')

        let timer: ReturnType<typeof setTimeout>;

        sse.onmessage = (event) => {
            const isInterestingPing = event.data === 'INTERESTING';

            if (interestingOnly && !isInterestingPing) {
                return;
            }

            console.log("Event received: ", event.data);
            clearInterval(timer);

            timer = setTimeout(() => {
                console.log("Fetching fresh data...")
                invalidateAll();
            }, 500);
        };

        sse.onerror = (error) => {
            console.error("SSE connection interrupted. Browser will attempt reconnect.", error);
        }

        return () => {
            console.log("Closing SSE connection...");
            sse.close();
        };
    });

    function formatBytes(bytes: number | undefined) {
        if (!bytes) return 'Unknown Size';
        const mb = bytes / (1024 * 1024);
        if (mb > 1024) return (mb / 1024).toFixed(2) + ' GB';
        return mb.toFixed(2) + ' MB';
    }

    function closeModal() {
        selectedAnime = null;
    }

    function formatDate(dateString: string) {
        if (!dateString) return 'Unknown Date';
        const date = new Date(dateString);
        return date.toLocaleDateString(undefined, {month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'});
    }

    function containSubs(item: AnimeItemDetailsResult, subsToCheck: string) {
        if (!item) return false;
        if (!subsToCheck) return item.subtitles.includes('us');
        return item.subtitles.includes(subsToCheck);
    }

    async function manuallyUpdateMetadata() {
        if (!manualAniListId || !selectedAnime?.showSeriesId) return;

        isUpdating = true;
        updateMessage = 'Updating...';

        try {
            const res = await fetch(`/api/watchpost/update-anilist`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    seriesId: selectedAnime.showSeriesId,
                    anilistId: parseInt(manualAniListId)
                })
            });

            if (res.ok) {
                updateMessage = 'Success. Refresh the page to see changes.';
                manualAniListId = '';

                await invalidateAll();

                setTimeout(() => closeModal(), 1000);
            } else {
                updateMessage = 'Failed to update.'
            }
        } catch (err) {
            updateMessage = 'Failed to update.';
        } finally {
            isUpdating = false;
        }
    }
</script>

<div class="anime-grid">
    {#each items as item}
        <div class="anime-card">
            <div class="card-container">
                <div class="card-info-container">
                    <h2 class="series-title" title={item.localizedName || item.seriesName || item.rawItemName}>
                        {item.localizedName || item.seriesName || item.rawItemName}
                    </h2>

                    <div class="episode-container">
                        <span class="episode-text">Episode</span>
                        <span class="episode-value">{item.episode || 'Unknown'}</span>
                    </div>

                    <div class="tags-container">
                        {#if item.resolution}
                            <span class="tag tag-resolution">{item.resolution}</span>
                        {/if}
                        {#if item.videoType}
                            <span class="tag tag-encoding">{item.videoType}</span>
                        {/if}
                        {#if item.videoSource}
                            <span class="tag tag-source">{item.videoSource}</span>
                        {/if}
                        {#if !containSubs(item, 'us')}
                            <span class="tag tag-no-subs">No EN Subs</span>
                        {/if}
                    </div>
                </div>
                <div class="card-image-container">
                    {#if item.coverImageUrl}
                        <img src={item.coverImageUrl} alt={item.seriesName || 'Anime Cover'} loading="lazy"/>
                    {:else}
                        <div class="placeholder-image">
                            <span>{(item.seriesName || item.rawItemName || '?').charAt(0)}</span>
                        </div>
                    {/if}
                </div>
            </div>
            <div class="card-footer">
                <span class="date-text">{formatDate(item.pubDate)}</span>

                <div class="actions">
                    <button type="button" class="btn info-btn" onclick={() => selectedAnime = item}>
                        Info
                    </button>
                    {#if item.fileLink}
                        <a href={item.fileLink} target="_blank" rel="noopener noreferrer" class="btn download-btn">
                            Download
                        </a>
                    {/if}
                </div>
            </div>
        </div>
    {:else}
        <div class="empty-state">
            <p>No new episodes found in the database</p>
        </div>
    {/each}
</div>

{#if selectedAnime}
    <div
            class="modal-backdrop"
            role="dialog"
            tabindex="-1"
            onclick={closeModal}
            onkeydown={(e) => e.key === 'Escape' && closeModal}
    >
        <div
                class="modal-content"
                role="dialog"
                aria-modal="true"
                tabindex="0"
                onclick={(e) => e.stopPropagation()}
                onkeydown={(e) => e.stopPropagation()}
        >
            <header class="modal-header">
                <h2>{selectedAnime.localizedName || selectedAnime.seriesName}</h2>
                <button type="button" class="close-btn" aria-label="Close" tabindex="0"
                        onclick={(e) => {e.stopPropagation(); closeModal();}}>
                    Close
                </button>
            </header>

            <div class="modal-body">
                <div class="modal-data">
                    <div class="info-group">
                        <span class="info-label">Item Name</span>
                        <span class="info-value">{selectedAnime.seriesName || 'Unknown'}</span>
                    </div>

                    <div class="info-group">
                        <span class="info-label">Episode</span>
                        <span class="info-value">{selectedAnime.episode || 'N/A'}</span>
                    </div>
                    <div class="info-group-stack">
                        <span class="info-label">Raw Item Name</span>
                        <span class="info-value raw-text">{selectedAnime.rawItemName}</span>
                    </div>

                    <div class="info-group">
                        <span class="info-label">File Size</span>
                        <span class="info-value">{formatBytes(selectedAnime.fileSizeBytes)}</span>
                    </div>

                    <div class="info-group">
                        <span class="info-label">Source Feed</span>
                        <span class="info-value">{selectedAnime.sourceFeed || 'Unknown'}</span>
                    </div>

                    <div class="info-group">
                        <span class="info-label">Subtitles</span>
                        <span class="info-value">{selectedAnime.subtitles || 'None/Embedded'}</span>
                    </div>

                    <div class="info-group">
                        <span class="info-label">Pub date/Local save date</span>
                        <span class="info-value">{formatDate(selectedAnime.pubDate) || 'Unknown'}
                            / {formatDate(selectedAnime.localSaveTime) || 'Unknown'}</span>
                    </div>

                    <div class="info-group">
                        <span class="info-label">Resolution</span>
                        <span class="info-value">{selectedAnime.resolution || 'Unknown'}</span>
                    </div>

                    <div class="info-group-stack">
                        <span class="info-label">Info MKV</span>
                        <span class="info-value">
                            {selectedAnime.videoType || 'Unknown'}
                            {selectedAnime.audioType || 'Unknown'}
                            {selectedAnime.videoSource || 'Unknown'}
                            {selectedAnime.sourceType || 'Unknown'}
                        </span>
                    </div>

                    <div class="info-group-stack">
                        <span class="info-label">ShowSeries Flags</span>
                        <span class="info-value">Interesting: {selectedAnime.isInteresting}</span>
                        <span class="info-value">Ignored: {selectedAnime.isIgnored}</span>
                        <span class="info-value">AutoDownload: {selectedAnime.autoDownload}</span>
                        <span class="info-value">CustomShareRatio: {selectedAnime.customShareRatio || 'Default'}</span>
                        <span class="info-value">Downloaded: {formatDate(selectedAnime.downloadedOn) || 'Not yet downloaded'}</span>
                    </div>

                    <div class="info-group-stack">
                        <span class="info-label">Online Id Details</span>
                        <span class="info-value">Online Id: {selectedAnime.animeOnlineId || 'Unknown'}</span>
                        <span class="info-value">MAL Link: {selectedAnime.malLink || 'Unknown'}</span>
                        <span class="info-value">Cover url: {selectedAnime.coverImageUrl || 'Unknown'}</span>
                        <span class="info-value">Localized Name: {selectedAnime.localizedName || 'Unknown'}</span>
                    </div>
                </div>

                <div class="modal-image-container">
                    {#if selectedAnime.coverImageUrl}
                        <img src={selectedAnime.coverImageUrl} alt={selectedAnime.seriesName || 'Anime Cover'}
                             loading="lazy"/>
                    {:else}
                        <div class="placeholder-image">
                            <span>{(selectedAnime.seriesName || selectedAnime.rawItemName || '?').charAt(0)}</span>
                        </div>
                    {/if}

                    {#if selectedAnime.animeOnlineId}
                        <div class="info-group-stack">
                            <span class="info-label">Status</span>
                            <span class="info-value">{selectedAnime.status || 'Unknown'}</span>
                        </div>

                        <div class="info-group-stack">
                            <span class="info-label">Genres</span>
                            <span class="info-value">{selectedAnime.genres || 'Unknown'}</span>
                        </div>

                        <div class="info-group">
                            <span class="info-value">{selectedAnime.synopsis || 'Unknown'}</span>
                        </div>
                    {:else}
                        <!--                        TODO: update metadata button here-->
                        <div class="anilist-override-container">
                            <span class="info-label">Manually Assign AniListId</span>
                            <div class="anilist-input-row">
                                <input type="number"
                                       placeholder="e.g. 113415"
                                       bind:value={manualAniListId}
                                       disabled={isUpdating}
                                       class="anilist-input"
                                />
                                <button class="anilist-submit-btn"
                                        onclick={manuallyUpdateMetadata}
                                        disabled={isUpdating || !manualAniListId}>
                                    {isUpdating ? 'Wait...' : 'Submit'}
                                </button>
                            </div>
                            {#if updateMessage}
                                <span class="info-label">{updateMessage}</span>
                            {/if}
                        </div>
                    {/if}
                </div>
            </div>
        </div>
    </div>
{/if}

<style>
    .anime-grid {
        display: flex;
        gap: 0.6rem;
        flex-wrap: wrap;
    }

    .anime-card {
        /*background: #1a1a24;*/
        /*background: var(--bg-color70);*/
        /*background: var(--static11);*/
        background: var(--secondary-color30);
        display: flex;
        flex-direction: column;
        border-radius: 6px;
        width: 24rem;
        justify-content: space-between;
        /*min-width: 20rem;*/
        /*max-width: 30rem;*/
        /*flex-grow: 1;*/
        /*align-self: flex-end;*/
        align-self: stretch;
    }

    .card-container {
        display: flex;
        flex-direction: row;
        height: 100%;
    }

    .card-info-container {
        width: 100%;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding: 0.5rem 0.5rem 0;
    }

    .series-title {
        margin: 0;
        /*color: #fff;*/
        color: var(--text-color);
        font-size: 1.1rem;
        text-wrap: wrap;
        max-width: 16rem;
        flex-grow: 2;
    }

    .episode-container {
        flex-grow: 0;
        padding-bottom: 5px;
        display: flex;
        flex-direction: row;
        gap: 0.25rem;
        margin-top: 0.5rem;
    }

    .episode-text {
        font-size: 1rem;
        /*color: #a0aec0;*/
        color: var(--subtext-color);
    }

    .episode-value {
        font-size: 1rem;
        /*color: #e2eae0;*/
        color: var(--text-color);
    }

    .tags-container {
        /*color: #fff;*/
        color: var(--text-color);
        align-self: flex-start;
        display: flex;
        align-content: flex-start;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 0.2rem;
    }

    .card-image-container {
        position: relative;
        padding-inline: 0.7rem;
        padding-top: 0.7rem;
        padding-bottom: 0.7rem;
        width: auto;
        min-width: 7rem;
        height: auto;
    }

    .card-footer {
        /*border-top: 1px solid #2d2d3d;*/
        margin-top: 0;
        display: flex;
        justify-content: space-between;
        padding-inline: 0.5rem;
        padding-bottom: 0.3rem;
    }

    .date-text {
        /*color: #fff;*/
        color: var(--text-color);
        align-self: center;
        font-size: 0.8rem;
    }

    .card-image-container img {
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

    .tag {
        font-size: 0.75rem;
        padding: 0.2rem 0.5rem;
        border-radius: 4px;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .tag-resolution {
        background: #3182ce;
        /*color: white;*/
        color: var(--text-color);
    }

    .tag-encoding {
        background: #805ad5;
        /*color: white;*/
        color: var(--text-color);
    }

    .tag-source {
        background: #4a5568;
        /*color: white;*/
        color: var(--text-color);
    }

    .tag-no-subs {
        background: #ef4444;
        /*color: white;*/
        color: var(--text-color);
    }

    .btn {
        text-decoration: none;
        padding: 0.3rem 0.6rem;
        border: 0;
        border-radius: 6px;
        font-size: 0.85rem;
        font-weight: 600;
        transition: background 0.2s;
    }

    .download-btn {
        background: #4ade80;
        /*color: #000;*/
        color: var(--text-color);
    }

    .download-btn:hover {
        background: #22c55e;
    }

    .info-btn {
        background: #3182ce;
        /*color: #fff;*/
        color: var(--text-color);
        text-decoration: none;
        padding: 0.3rem 0.6rem;
        border-radius: 6px;
        font-size: 0.85rem;
        font-weight: 600;
        transition: background 0.2s;
    }

    .info-btn:hover {
        background: #1f67ed;
    }

    .empty-state {
        grid-column: 1 / -1;
        text-align: center;
        padding: 4rem;
        /*color: #a0aec0;*/
        color: var(--text-color);
        /*background: #1a1a24;*/
        background: var(--bg-color70);
        border-radius: 12px;
    }

    /*modal*/
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
        font-family: sans-serif;
        background: #1a1a24;
        border: 1px solid #2d2d3d;
        border-radius: 6px;
        min-width: 60%;
        max-width: 80%;
        max-height: 90vh;
        overflow-y: auto;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem;
        border-bottom: 1px solid #2d2d3d;
        background: #1e1e28;
    }

    .modal-header h2 {
        margin: 0;
        color: #fff;
        font-size: 1.25rem;
    }

    .close-btn {
        background: transparent;
        border: none;
        color: #a0aec0;
        font-size: 1rem;
        cursor: pointer;
        transition: color 0.2s;
    }

    .close-btn:hover {
        color: #ef4444;
    }

    .modal-body {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
    }

    .modal-data {
        padding: 1rem;
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
        flex-grow: 2;
    }

    .info-group {
        display: flex;
        flex-direction: row;
        gap: 0.25rem;
    }

    .info-group-stack {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
        text-wrap: wrap;
    }

    .info-label {
        font-size: 1rem;
        /*text-transform: uppercase;*/
        color: #a0aec0;
        /*font-weight: 600;*/
        /*letter-spacing: 0.5px;*/
    }

    .info-value {
        font-size: 1rem;
        color: #e2eae0;
        text-wrap: wrap;
    }

    .modal-image-container {
        position: relative;
        flex-grow: 1;
        padding: 1rem 1rem 1rem 1rem;
        width: 30%;
        height: auto;
        display: flex;
        flex-direction: column;
        gap: 0.6rem;
        max-width: 20rem;
        /*max-height: 40vw;*/
    }

    .modal-image-container img {
        border-radius: 3px;
        /*max-height: 12rem;*/
        object-fit: cover;


    }

    .anilist-override-container {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        margin-top: 0.5rem;
    }

    .anilist-input-row {
        display: flex;
        gap: 0.5rem;
        flex-wrap: wrap;
    }

    .anilist-input {
        flex-grow: 1;
        border: 1px solid #2d2d3d;
        background: transparent;
        color: #e2e8f0;
        padding: 0.5rem;
        border-radius: 3px;
        font-size: 0.8rem;
    }

    .anilist-input:focus {
        outline: none;
        border-color: #4ade80;
    }

    .anilist-submit-btn {
        background: #4ade80;
        color: white;
        border: none;
        padding: 0.5rem 1rem;
        border-radius: 3px;
        font-weight: bold;
        cursor: pointer;
        transition: background 0.2s;
    }

    .anilist-submit-btn:hover:not(:disabled) {
        background: #22c55e;
    }

    .anilist-submit-btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }
</style>