import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import retrofit2.HttpException

class DeezerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val query = inputData.getString("query") ?: return Result.failure()
        val apiService = DeezerApiService.create()

        return try {
            val response = apiService.searchTracks(query)
            val track = response.data.firstOrNull()
            if (track != null) {
                val trackJson = Gson().toJson(track)
                val outputData = workDataOf("track" to trackJson)
                Result.success(outputData)
            } else {
                Result.failure()
            }
        } catch (e: HttpException) {
            Result.failure()
        }
    }
}