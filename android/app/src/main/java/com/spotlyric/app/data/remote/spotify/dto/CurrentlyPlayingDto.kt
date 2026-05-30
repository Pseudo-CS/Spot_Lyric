package com.spotlyric.app.data.remote.spotify.dto

import com.google.gson.annotations.SerializedName

data class CurrentlyPlayingDto(
    @SerializedName("is_playing") val isPlaying: Boolean?,
    @SerializedName("item") val item: TrackDto?
)

data class TrackDto(
    @SerializedName("name") val name: String?,
    @SerializedName("artists") val artists: List<ArtistDto>?,
    @SerializedName("album") val album: AlbumDto?
)

data class ArtistDto(
    @SerializedName("name") val name: String?
)

data class AlbumDto(
    @SerializedName("images") val images: List<ImageDto>?
)

data class ImageDto(
    @SerializedName("url") val url: String?,
    @SerializedName("height") val height: Int?,
    @SerializedName("width") val width: Int?
)
