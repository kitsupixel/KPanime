package pt.kitsupixel.kpanime.network

import kotlinx.coroutines.Deferred
import pt.kitsupixel.kpanime.network.DTObjects.NetworkEpisodeContainer
import pt.kitsupixel.kpanime.network.DTObjects.NetworkLinkContainer
import pt.kitsupixel.kpanime.network.DTObjects.NetworkShowContainer
import retrofit2.http.GET
import retrofit2.http.Path

interface KPService {
    @GET("shows")
    fun getShows(): Deferred<NetworkShowContainer>

    @GET("shows/{show}/episodes")
    fun getEpisodes(@Path("show") show: Long): Deferred<NetworkEpisodeContainer>

    @GET("shows/{show}/episodes/{episode}")
    fun getLinks(@Path("show") showId: Long, @Path("episode") episodeId: Long): Deferred<NetworkLinkContainer>

    @GET("shows/latest")
    fun getLatestEpisodes(): Deferred<NetworkEpisodeContainer>

    @GET("shows/current")
    fun getCurrentShows(): Deferred<NetworkShowContainer>
}