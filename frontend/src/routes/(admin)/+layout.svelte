<script lang="ts">
    let { children, data } = $props();
</script>

<nav class="admin-nav">
    <div class="logo">Watchpost [ADMIN]</div>
    <a href="/dashboard">Dashboard</a>
    <a href="/watchpost">Anime Manager</a>
    <a href="/settings">Settings</a>

    <span class="user-badge">Logged in as: {data.userEmail}</span>
</nav>

{@render children()}