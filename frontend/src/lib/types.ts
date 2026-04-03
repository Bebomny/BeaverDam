export interface DiscordVoiceTimeResult {
    userId: string;
    username: string;
    totalSecondsSpent: number;
}

export interface DiscordGuildVoiceTimesResult {
    guildId: string;
    guildName: string;
    iconUrl: string;
    voiceTimes: DiscordVoiceTimeResult[];
}

export interface DiscordVoiceChatDataResult {
    userId: string;
    username: string;
    userIconUrl: string;
    totalSecondsSpent: number;
    selfMuteCount: number;
    selfDeafenCount: number;
    serverMuteCount: number;
    serverDeafenCount: number;
    streamCount: number;
    suppressCount: number;
}

export interface DiscordGuildVoiceChatDataResult {
    guildId: string;
    guildName: string;
    iconUrl: string;
    // voiceTimes: DiscordVoiceChatDataResult[] | DiscordVoiceTimeResult[];
    voiceChatData: DiscordVoiceChatDataResult[];
}

export interface MonitoredGuildResult {
    guildId: string;
    guildName: string;
    iconUrl: string;
}

//Watchpost
export interface AnimeItemDetailsResult {
    animeItemId: string;
    rawItemName: string;
    seriesName: string;
    episode: string;
    sourceFeed: string;
    fileLink: string;
    pubDate: string;
    localSaveTime: string;

    resolution: string;
    subtitles: string;
    fileSizeBytes: number;
    videoCategory: string;
    videoSource: string;
    sourceType: string;
    videoType: string;
    audioType: string;

    showSeriesId: string;
    isInteresting: boolean;
    isIgnored: boolean;
    autoDownload: boolean;
    downloaded: boolean;
    downloadedOn: string;

    animeOnlineId: string;
    malLink: string;
    coverImageUrl: string;
    localizedName: string;
    synopsis: string;
    genres: string;
    status: string;
}