<script lang="ts">
    import type {AnimeItemDetailsResult} from "$lib/types";

    let {data} = $props();

    let selectedAnime = $state<AnimeItemDetailsResult | null>(null)

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
</script>

<main>
    <header class="page-header">
        <h1>Latest Releases</h1>
        <p>The 50 most recent anime episodes captured by Watchpost.</p>
    </header>

    <div class="anime-grid">
        {#each data.animeItems as item}
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
</main>

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
                        <span class="info-value">{formatDate(selectedAnime.pubDate) || 'Unknown'} / {formatDate(selectedAnime.localSaveTime) || 'Unknown'}</span>
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
                        <img src={selectedAnime.coverImageUrl} alt={selectedAnime.seriesName || 'Anime Cover'} loading="lazy"/>
                    {:else}
                        <div class="placeholder-image">
                            <span>{(selectedAnime.seriesName || selectedAnime.rawItemName || '?').charAt(0)}</span>
                        </div>
                    {/if}
                </div>
            </div>
        </div>
    </div>
{/if}

<style>
    /*Transfer to a global file later on*/
    :root {
        --text-color: #0a0e0a;
        --bg-color: #f8faf8;
        --primary-color: #63a769;
        --secondary-color: #9dcea1;
        --accent-color: #74c27b;
    }

    main {
        padding: 2rem;
        max-width: 90%;
        margin: 0 auto;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    }

    .page-header {
        margin-bottom: 2rem;
    }

    .page-header h1 {
        margin: 0 0 0.5rem 0;
        font-size: 2rem;
    }

    .page-header p {
        color: #a0aec0;
        margin: 0;
    }

    .anime-grid {
        display: flex;
        gap: 0.6rem;
        flex-wrap: wrap;
    }

    .anime-card {
        background: #1a1a24;
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
        padding: 0.5rem;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }

    .series-title {
        margin: 0;
        color: #fff;
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
        color: #a0aec0;
    }

    .episode-value {
        font-size: 1rem;
        color: #e2eae0;
    }

    .tags-container {
        color: #fff;
        align-self: flex-start;
    }

    .card-image-container {
        position: relative;
        padding-inline: 1rem;
        padding-top: 0.4rem;
        padding-bottom: 0.4rem;
        width: auto;
        min-width: 4rem;
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
        color: #fff;
        align-self: center;
        font-size: 0.8rem;
    }

    /*.card-image-container {*/
    /*    position: relative;*/
    /*    order: 1;*/
    /*    width: auto;*/
    /*    height: 100%; !* Standard poster aspect ratio *!*/
    /*    background-color: #121215;*/
    /*}*/

    .card-image-container img {
        width: 100%;
        height: 100%;
        object-fit: cover;
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
        color: white;
    }

    .tag-encoding {
        background: #805ad5;
        color: white;
    }

    .tag-source {
        background: #4a5568;
        color: white;
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
        color: #000;
    }

    .download-btn:hover {
        background: #22c55e;
    }

    .info-btn {
        background: #3182ce;
        color: #fff;
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
        color: #a0aec0;
        background: #1a1a24;
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
        gap:0.25rem;
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
        flex-grow: 1;
        padding: 1rem;
        object-fit: cover;
        width: 50%;
        height: auto;
        max-height: 12rem;
    }
</style>