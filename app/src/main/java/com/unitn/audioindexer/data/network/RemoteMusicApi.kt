package com.unitn.audioindexer.data.network

import retrofit2.http.GET
import retrofit2.http.Path

interface RemoteMusicApi {
    @GET("api/browse")
    suspend fun browseRoot(): BrowseResponse

    @GET("api/browse/{path}")
    suspend fun browsePath(@Path("path", encoded = true) path: String): BrowseResponse
}
