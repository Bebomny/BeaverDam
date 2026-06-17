<script lang="ts">
import {toaster} from "$lib/state/toaster.svelte";
import { fly, fade } from 'svelte/transition';
import { flip } from 'svelte/animate';
</script>

<div class="toast-wrapper">
    {#each toaster.toasts as toast (toast.id)}
        <div
                class="toast {toast.type}"
                in:fly={{x: 50, duration: 300 }}
                out:fade={{ duration: 200 }}
                animate:flip={{ duration: 300 }}
                role="alert"
        >
            <span>{toast.message}</span>
            <button class="close-btn" onclick={() => toaster.remove(toast.id)}>X</button>
        </div>
    {/each}
</div>

<style>
    .toast-wrapper {
        position: fixed;
        bottom: 1.5rem;
        right: 1.5rem;
        z-index: 9999;
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
        pointer-events: none;
    }

    .toast {
        pointer-events: auto;
        min-width: 250px;
        padding: 1rem 1.25rem;
        border-radius: 6px;
        /*box-shadow: 0 10px 25px rgba(0,0,0,0.4);*/
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
        color: white;
        font-weight: 500;
        font-size: 0.95rem;
        border: 1px solid rgba(255, 255, 255, 0.1);
        background: var(--secondary-color-darker);
    }

    /*.toast.info { background: rgba(30, 41, 59, 0.95); border-left: 4px solid #3b82f6; }*/
    /*.toast.success { background: rgba(20, 83, 45, 0.95); border-left: 4px solid #22c55e; }*/
    /*.toast.error { background: rgba(127, 29, 29, 0.95); border-left: 4px solid #ef4444; }*/

    .close-btn {
        background: transparent;
        border: none;
        color: rgba(255, 255, 255, 0.8);
        cursor: pointer;
        padding: 0;
        font-size: 1rem;
    }

    .close-btn:hover {
        color: white;
    }

</style>