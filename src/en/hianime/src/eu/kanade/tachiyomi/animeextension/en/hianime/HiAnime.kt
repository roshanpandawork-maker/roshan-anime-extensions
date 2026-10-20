package eu.kanade.tachiyomi.animeextension.en.hianime

import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import eu.kanade.tachiyomi.network.GET
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder

/**
 * Initial HiAnime integration.
 *
 * Search, details and episode parsing use the site's current public page patterns. HiAnime
 * frequently changes markup and protects third-party video embeds. Playback must be tested
 * against a maintained extractor before this source can be called production-ready.
 */
class HiAnime : AnimeHttpSource() {
    override val name = "HiAnime"
    override val baseUrl = "https://hianime.to"
    override val lang = "en"
    override val supportsLatest = true

    override fun popularAnimeRequest(page: Int): Request =
        GET("$baseUrl/most-popular?page=$page", HEADERS)

    override fun popularAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()
        val entries = document.select(".flw-item, .film_list-wrap .item").mapNotNull { it.toAnime() }
        val hasNext = document.selectFirst("a[rel=next], .pagination .page-item.active + .page-item a") != null
        return AnimesPage(entries, hasNext)
    }

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/recently-updated?page=$page", HEADERS)

    override fun latestUpdatesParse(response: Response): AnimesPage {
        val document = response.asJsoup()
        val entries = document.select(".flw-item, .film_list-wrap .item").mapNotNull { it.toAnime() }
        val hasNext = document.selectFirst("a[rel=next], .pagination .page-item.active + .page-item a") != null
        return AnimesPage(entries, hasNext)
    }

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return GET("$baseUrl/search?keyword=$encoded&page=$page", HEADERS)
    }

    override fun searchAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()
        val entries = document.select(".flw-item, .film_list-wrap .item").mapNotNull { it.toAnime() }
        val hasNext = document.selectFirst("a[rel=next], .pagination .page-item.active + .page-item a") != null
        return AnimesPage(entries, hasNext)
    }

    override fun getFilterList() = AnimeFilterList()

    override fun animeDetailsRequest(anime: SAnime): Request =
        GET(if (anime.url.startsWith("http")) anime.url else baseUrl + anime.url, HEADERS)

    override fun animeDetailsParse(response: Response): SAnime {
        val document = response.asJsoup()
        val anime = SAnime.create()
        anime.url = response.request.url.encodedPath
        anime.title = document.selectFirst(".anisc-detail .film-name, .film-name")?.text()
            ?: document.selectFirst("h1")?.text().orEmpty()
        anime.thumbnail_url = document.selectFirst(".film-poster img, .film-poster .film-poster-img")
            ?.let { it.absUrl("src").ifBlank { it.absUrl("data-src") } }
        anime.description = document.selectFirst(".film-description, .film-des")?.text()
        anime.genre = document.select(".item-list a[href*=/genre/]").joinToString { it.text() }
        anime.status = when {
            document.text().contains("Currently Airing", true) -> SAnime.ONGOING
            document.text().contains("Finished Airing", true) -> SAnime.COMPLETED
            else -> SAnime.UNKNOWN
        }
        anime.initialized = true
        return anime
    }

    override fun episodeListRequest(anime: SAnime): Request = animeDetailsRequest(anime)

    override fun episodeListParse(response: Response): List<SEpisode> {
        val document = response.asJsoup()
        val syncData = document.selectFirst("script#syncData")?.data().orEmpty()
        val animeId = runCatching { org.json.JSONObject(syncData).optString("anime_id") }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: document.selectFirst(".film-poster[data-id], .anisc-detail [data-id]")?.attr("data-id")
        if (animeId.isNullOrBlank()) {
            throw UnsupportedOperationException("HiAnime episode ID was not found; the site markup may have changed.")
        }

        val ajaxUrl = "$baseUrl/ajax/v2/episode/list/$animeId"
        val payload = client.newCall(GET(ajaxUrl, HEADERS)).execute().use { it.body.string() }
        val html = runCatching { org.json.JSONObject(payload).optString("html") }.getOrDefault("")
        if (html.isBlank()) return emptyList()

        return Jsoup.parseBodyFragment(html, baseUrl).select("a.ssl-item, .ep-item").map { element ->
            SEpisode.create().apply {
                url = element.attr("href").ifBlank { element.attr("data-id") }
                name = element.selectFirst(".ep-name")?.text().orEmpty()
                    .ifBlank { element.attr("title").ifBlank { element.text() } }
                episode_number = EPISODE_NUMBER.find(name)?.groupValues?.getOrNull(1)?.toFloatOrNull() ?: 0f
            }
        }.reversed()
    }

    override fun videoListRequest(episode: SEpisode): Request =
        GET(if (episode.url.startsWith("http")) episode.url else baseUrl + episode.url, HEADERS)

    override fun videoListParse(response: Response): List<Video> {
        // An iframe URL is not itself a playable media URL. Do not return it as a video.
        val document = response.asJsoup()
        val iframe = document.selectFirst("#player iframe, iframe[src*=/embed/]")
        val embed = iframe?.absUrl("src").orEmpty()
        if (embed.isNotBlank()) {
            throw UnsupportedOperationException(
                "HiAnime embed found, but provider stream extraction is not integrated yet.",
            )
        }
        throw UnsupportedOperationException("HiAnime player embed was not found; the site may have changed.")
    }

    private fun Element.toAnime(): SAnime? {
        val link = selectFirst(".film-name a, a[href*=/watch/], a[href*=/anime/]") ?: return null
        val title = link.attr("title").ifBlank { link.text() }
        val href = link.attr("href")
        if (title.isBlank() || href.isBlank()) return null
        return SAnime.create().apply {
            url = href
            this.title = title
            thumbnail_url = selectFirst("img")?.let {
                it.absUrl("data-src").ifBlank { it.absUrl("src") }
            }
        }
    }

    private fun Response.asJsoup(): Document = Jsoup.parse(body.string(), request.url.toString())

    companion object {
        private val EPISODE_NUMBER = Regex("""(?:Episode\s*)?(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
        private val HEADERS = okhttp3.Headers.Builder()
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/130.0.0.0 Safari/537.36")
            .add("Referer", "https://hianime.to/")
            .build()
    }
}
