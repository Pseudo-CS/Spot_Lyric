package com.spotlyric.app.data.remote.spotify

import com.spotlyric.app.data.remote.spotify.dto.CurrentlyPlayingDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface SpotifyApiService {

    @GET("me/player/currently-playing")
    suspend fun getCurrentlyPlaying(
        @Header("Authorization") authorization: String
    ): Response<CurrentlyPlayingDto?>
}
