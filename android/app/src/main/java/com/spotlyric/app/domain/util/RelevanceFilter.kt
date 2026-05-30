package com.spotlyric.app.domain.util

object RelevanceFilter {
    private val STOPWORDS = setOf(
        "the", "a", "an",
        "in", "on", "at", "to", "by", "from", "for", "of", "with",
        "and", "or", "but",
        "is", "are", "was", "were", "as",
        "lyrics", "translation", "remix", "cover", "live", "version", "featuring", "feat", "ft",
        "english", "hindi", "spanish", "arabic", "korean", "japanese", "chinese", "mandarin", "romanian"
    )

    fun tokenize(text: String): Set<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it.length > 1 }
            .filter { it !in STOPWORDS }
            .toSet()
    }

    fun calculateRelevanceScore(queryTokens: Set<String>, resultTokens: Set<String>): Float {
        if (queryTokens.isEmpty()) return 1.0f
        val intersection = queryTokens.intersect(resultTokens).size.toFloat()
        val union = queryTokens.union(resultTokens).size.toFloat()
        return if (union == 0f) 1.0f else intersection / union
    }

    fun isRelevant(
        songName: String,
        artistName: String,
        resultTitle: String,
        resultSnippet: String? = null,
        threshold: Float = 0.2f
    ): Boolean {
        val songTokens = tokenize(songName)
        val artistTokens = tokenize(artistName)
        val queryTokens = songTokens + artistTokens

        if (queryTokens.isEmpty()) return true

        val titleTokens = tokenize(resultTitle)
        val snippetTokens = tokenize(resultSnippet ?: "")
        val resultTokens = titleTokens + snippetTokens

        // At least one title token must match (if songTokens is not empty)
        val hasTitleMatch = songTokens.isEmpty() || songTokens.any { it in resultTokens }
        if (!hasTitleMatch) return false

        val score = calculateRelevanceScore(queryTokens, resultTokens)
        return score >= threshold
    }
}
