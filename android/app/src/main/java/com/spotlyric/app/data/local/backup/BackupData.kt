package com.spotlyric.app.data.local.backup

import com.google.gson.annotations.SerializedName

data class BackupData(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("bookmarks") val bookmarks: List<BackupBookmark> = emptyList(),
    @SerializedName("preferredSources") val preferredSources: List<BackupPreferredSource> = emptyList(),
    @SerializedName("settings") val settings: BackupSettings = BackupSettings()
)

data class BackupBookmark(
    @SerializedName("songName") val songName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("bookmarkedUrl") val bookmarkedUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("lyrics") val lyrics: BackupLyrics? = null
)

data class BackupLyrics(
    @SerializedName("originalLyrics") val originalLyrics: String,
    @SerializedName("translatedLyrics") val translatedLyrics: String,
    @SerializedName("aiRomanized") val aiRomanized: String? = null,
    @SerializedName("aiTranslation") val aiTranslation: String? = null,
    @SerializedName("sourceUrl") val sourceUrl: String? = null,
    @SerializedName("extractionStage") val extractionStage: String? = null,
    @SerializedName("confidenceScore") val confidenceScore: Float? = null,
    @SerializedName("originalLanguage") val originalLanguage: String? = null
)

data class BackupPreferredSource(
    @SerializedName("domain") val domain: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("enabled") val enabled: Boolean
)

data class BackupSettings(
    @SerializedName("relevanceFilterEnabled") val relevanceFilterEnabled: Boolean = true,
    @SerializedName("relevanceThreshold") val relevanceThreshold: Float = 0.2f,
    @SerializedName("spotifyCustomClientId") val spotifyCustomClientId: String? = null,
    @SerializedName("serpApiCustomApiKey") val serpApiCustomApiKey: String? = null,
    @SerializedName("geminiCustomApiKey") val geminiCustomApiKey: String? = null,
    @SerializedName("geminiCustomModel") val geminiCustomModel: String = "gemini-2.5-flash-lite"
)
