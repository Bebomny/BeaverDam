<script lang="ts">
    let { children } = $props();
</script>

<nav class="public-nav">
    <div class="logo"> Bajo jjao</div>
    <a href="/leaderboard">Leaderboard</a>
</nav>

{@render children()}