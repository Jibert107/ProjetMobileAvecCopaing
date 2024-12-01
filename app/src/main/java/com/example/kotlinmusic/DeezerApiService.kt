import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class DeezerTrack(val title: String, val artist: Artist, val album: Album, val preview: String)
data class Artist(val name: String)
data class Album(val cover: String)
data class DeezerResponse(val data: List<DeezerTrack>)

interface DeezerApiService {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerResponse

    companion object {
        private const val BASE_URL = "https://api.deezer.com/"

        fun create(): DeezerApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DeezerApiService::class.java)
        }
    }
}