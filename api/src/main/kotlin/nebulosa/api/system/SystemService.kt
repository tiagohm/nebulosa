package nebulosa.api.system

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class SystemService(
    private val httpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
) {

    fun latestRelease(): GitHubLatestRelease? {
        val request = Request.Builder()
            .get()
            .url(GITHUB_LATEST_RELEASE_URL)
            .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .header(GITHUB_API_VERSION_HEADER_KEY, GITHUB_API_VERSION_HEADER_VALUE)
            .build()

        val response = httpClient.newCall(request).execute()

        return response.body?.byteStream()
            ?.use { objectMapper.readValue(it, GitHubLatestRelease::class.java) }
    }

    companion object {

        const val GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/tiagohm/nebulosa/releases/latest"
        const val GITHUB_API_VERSION_HEADER_KEY = "X-GitHub-Api-Version"
        const val GITHUB_API_VERSION_HEADER_VALUE = "2022-11-28"
    }
}
