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
    customShareRatio: string;
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

export interface ShowSeriesStateDetailsResult {
    id: string;
    name: string;
    interesting: boolean;
    ignored: boolean;
    autoDownload: boolean;
    submitted: boolean;
    customShareRatio: string;
    lastSeen: string;
    addedOn: string;
    onlineId: string;
}

export interface ShowMetadataFullResult {
    metadataId: string;
    malId: string;
    anilistId: string;
    localizedName: string;
    coverImageUrl: string;
    synopsis: string;
    genres: string;
    status: string;
}

export interface ShowSeriesDetailsFullResult {
    showSeriesId: string;
    showSeriesName: string;
    interesting: boolean;
    ignored: boolean;
    autoDownload: boolean;
    submitted: boolean;
    customShareRatio: string;
    lastSeen: string;
    addedOn: string;

    showMetadata: ShowMetadataFullResult;
}