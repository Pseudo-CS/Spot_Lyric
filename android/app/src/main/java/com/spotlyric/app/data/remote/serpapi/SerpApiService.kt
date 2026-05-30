package com.spotlyric.app.data.remote.serpapi

import retrofit2.http.GET
import retrofit2.http.Query

interface SerpApiService {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("engine") engine: String = "google",
        @Query("api_key") apiKey: String,
        @Query("num") num: Int = 10,
        @Query("gl") gl: String = "in",
        @Query("hl") hl: String = "en"
    ): SerpApiResponse
}
