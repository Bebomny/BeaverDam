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