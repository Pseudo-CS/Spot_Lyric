package com.spotlyric.app.data.remote.serpapi

import com.google.gson.annotations.SerializedName

data class SerpApiResponse(
    @SerializedName("organic_results") val organicResults: List<OrganicResult>?
)

data class OrganicResult(
    @SerializedName("title") val title: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("snippet") val snippet: String?
)
