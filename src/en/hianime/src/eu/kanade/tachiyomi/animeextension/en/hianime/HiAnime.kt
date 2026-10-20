package eu.kanade.tachiyomi.animeextension.en.hianime

import eu.kanade.tachiyomi.animesource.AnimeFilter
import eu.kanade.tachiyomi.animesource.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder

/**
 * Initial HiAnime integration.
 *
 * Site markup and video-provider behaviour change frequently. Search, details and episode
 * parsing are implemented defensively; stream extraction is intentionally reported as an
 * explicit unsupported state rather than pretending an embed page is a playable video URL.
 */
class HiAnime : AnimeHttpSource() {
    override val name = "HiAnime"
    override val baseUrl = "https://hianime.to"
    override val lang = "en"
    override val supportsLatest = false

    override fun popularAnimeRequest(page: Int): Request =
        GET("$baseUrl/most-popular?page=$page", headers)

    override fun popularAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()
        val entries = document.select(".flw-item, .film_list-wrap .item").mapNotNull { it.toAnime() }
        val hasNext = document.selectFirst("a[rel=next], .pagination .page-item.active + .page-item a") != null
        return AnimesPage(entries, hasNext)
    }

    override fun searchAnimeRequest(page: Int, query: String, filters: List<AnimeFilter>): Request {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return GET("$baseUrl/search?keyword=$encoded&page=$page", headers)
    }

    override fun searchAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()
        val entries = document.select(".flw-item, .film_list-wrap .item").mapNotNull { it.toAnime() }
        val hasNext = document.selectFirst("a[rel=next], .pagination .page-item.active + .page-item a") != null
        return AnimesPage(entries, hasNext)
    }

    override fun getFilterList() = AnimeFilterList()

    override fun animeDetailsRequest(anime: SAnime): Request =
        GET(if (anime.url.startsWith("http")) anime.url else baseUrl + anime.url, headers)

    override fun animeDetailsParse(response: Response): SAnime {
        val document = response.asJsoup()
        val anime = SAnime.create()
        anime.url = response.request.url.encodedPath
        anime.title = document.selectFirst(".anisc-detail .film-name, .film-name")?.text()
            ?: document.selectFirst("h1")?.text().orEmpty()
        anime.thumbnail_url = document.selectFirst(".film-poster img, .film-poster .film-poster-img")
            ?.let { it.absUrl("src").ifBlank { it.absUrl("data-src") } }
        anime.description = document.selectFirst(".film-description, .film-des")?.text()
        anime.genre = document.select(".item-list a[href*=/genre/]").joinToString(", ") { it.text() }
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
        val animeId = document.selectFirst("[data-id]")?.attr("data-id")
            ?: document.selectFirst("div.film-poster")?.attr("data-id")
        if (animeId.isNullOrBlank()) {
            throw UnsupportedOperationException("HiAnime episode ID was not found; the site markup may have changed.")
        }

        val ajaxUrl = "$baseUrl/ajax/v2/episode/list/$animeId"
        val ajaxResponse = client.newCall(GET(ajaxUrl, headers)).execute()
        val payload = ajaxResponse.use { it.body.string() }
        val html = runCatching { org.json.JSONObject(payload).optString("html") }.getOrDefault("")
        if (html.isBlank()) return emptyList()

        return Jsoup.parse(html).select(".ep-item").map { element ->
            SEpisode.create().apply {
                url = element.attr("href").ifBlank { element.attr("data-id") }
                name = element.attr("title").ifBlank { element.text().trim() }
                episode_number = Regex("""(?:Episode\s*)?(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
                    .find(name)?.groupValues?.getOrNull(1)?.toFloatOrNull() ?: 0f
            }
        }.reversed()
    }

    override fun videoListRequest(episode: SEpisode): Request =
        GET(if (episode.url.startsWith("http")) episode.url else baseUrl + episode.url, headers)

    override fun videoListParse(response: Response): List<Video> {
        // HiAnime commonly uses protected third-party embed providers. An iframe URL is not
        // itself a playable media URL. Until a maintained extractor is integrated and tested,
        // fail clearly instead of returning a misleading non-playable video entry.
        val document = response.asJsoup()
        val iframe = document.selectFirst("#player iframe, iframe[src*=/embed/]")
        val embed = iframe?.absUrl("src").orEmpty()
        if (embed.isNotBlank()) {
            throw UnsupportedOperationException(
                "HiAnime embed found but provider extraction is not implemented yet: $embed",
            )
        }
        throw UnsupportedOperationException("HiAnime player embed was not found; the site may have changed.")
    }

    override fun videoFromElement(element: Element): Video? = null

    private fun Element.toAnime(): SAnime? {
        val link = selectFirst(".film-name a, a[href*=/watch/], a[href*=/anime/]") ?: return null
        val title = link.attr("title").ifBlank { link.text().trim() }
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
        private val headers = okhttp3.Headers.Builder()
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/130.0.0.0 Safari/537.36")
            .add("Referer", "https://hianime.to/")
            .build()
    }
}
