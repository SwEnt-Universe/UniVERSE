package com.android.universe.ui.search

import com.android.universe.model.tag.Tag

/**
 * A simple fuzzy-search utility used to filter based on approximate matches.
 *
 * ## Overview
 * This engine uses the **Levenshtein distance** algorithm to determine how many single-character
 * edits (insertions, deletions, substitutions) are required to transform one string into another.
 * If the distance is less than or equal to [threshold], the two strings are considered a match.
 *
 * ## Usage
 * `fuzzyMatch("football", "fotbal")` → true (distance = 2, default threshold = 2)
 *
 * @param text The original full text to compare against (e.g., event title).
 * @param query The user-entered query string.
 * @param threshold Maximum allowed edit distance before strings are considered too different.
 *   Default is 2, meaning up to two character edits are tolerated.
 * @return `true` if the Levenshtein distance between [text] and [query] is within the threshold.
 */
object SearchEngine {

  fun fuzzyMatch(text: String, query: String, threshold: Int = 2): Boolean {
    val distance = levenshteinDistance(text.lowercase(), query.lowercase())
    return distance <= threshold
  }

  /**
   * Computes the Levenshtein edit distance between two strings.
   *
   * @return The minimum number of insertions, deletions, or substitutions required to turn [a] into
   *   [b].
   */
  private fun levenshteinDistance(a: String, b: String): Int {
    val dp = Array(a.length + 1) { IntArray(b.length + 1) }

    for (i in 0..a.length) dp[i][0] = i
    for (j in 0..b.length) dp[0][j] = j

    for (i in 1..a.length) {
      for (j in 1..b.length) {
        val cost = if (a[i - 1] == b[j - 1]) 0 else 1
        dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
      }
    }
    return dp[a.length][b.length]
  }

  /**
   * Checks if a set of tags contains at least one tag belonging to *any* category in a given set of
   * categories.
   *
   * OR behavior:
   * - If at least one category in [query] is represented by at least one tag in [tags], the
   *   function returns `true`.
   * - If none of the categories in [query] appear in [tags], it returns `false`.
   *
   * Special case:
   * - If [query] is empty, the function returns `true` (no filter applied).
   *
   * @param tags The set of tags to check.
   * @param query The set of categories to match.
   * @return `true` if at least one category in [query] is present in [tags].
   */
  fun tagMatch(tags: Iterable<Tag>, query: Set<Tag.Category>): Boolean {
    if (query.isEmpty()) return true
    return query.any { cat -> tags.any { it.category == cat } }
  }

  /**
   * Creates a comparator for comparing two objects of type [T] by the number of required categories
   * covered by their tag sets.
   *
   * Coverage rule (OR):
   * - An object "covers" a category if at least one of its tags belongs to that category.
   *
   * Higher coverage → "greater".
   *
   * @param requiredCategories Categories to check for.
   * @param tagExtractor Function extracting a Set<Tag> from T.
   */
  fun <T> categoryCoverageComparator(
      requiredCategories: Set<Tag.Category>,
      tagExtractor: (T) -> Iterable<Tag>
  ): Comparator<T> {

    return Comparator { a, b ->
      val tagsA = tagExtractor(a)
      val tagsB = tagExtractor(b)

      val countA = requiredCategories.count { cat -> tagsA.any { it.category == cat } }

      val countB = requiredCategories.count { cat -> tagsB.any { it.category == cat } }

      countA.compareTo(countB) // higher → greater so make sure to .reversed() in UI if you want
      // descending order
    }
  }
}
