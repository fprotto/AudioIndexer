package com.unitn.audioindexer.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface AudioDbApi {
    @GET("api/v1/json/123/artist-mb.php")
    suspend fun getArtistByMbid(
        @Query("i") mbid: String
    ): AudioDbResponse
}

data class AudioDbResponse(
    val artists: List<AudioDbArtist>?
)

data class AudioDbArtist(
    val strArtistThumb: String?
)
