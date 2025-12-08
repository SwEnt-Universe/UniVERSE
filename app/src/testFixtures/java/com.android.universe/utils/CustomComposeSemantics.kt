package com.android.universe.utils

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction

object CustomComposeSemantics {

  /**
   * Returns a [SemanticsMatcher] that checks if a node's [SemanticsProperties.TestTag] starts with
   * the given [prefix].
   */
  fun hasTestTagPrefix(prefix: String): SemanticsMatcher {
    return SemanticsMatcher("${SemanticsProperties.TestTag.name} starts with '$prefix'") { node ->
      // Get the test tag from the node's configuration
      val testTag = node.config.getOrNull(SemanticsProperties.TestTag) ?: ""

      // Return true if the test tag starts with the prefix
      testTag.startsWith(prefix)
    }
  }

    fun SemanticsNodeInteraction.hasText(
        value: String,
        substring: Boolean = false,
        ignoreCase: Boolean = false,
    ): Boolean = androidx.compose.ui.test.hasText(value, substring = substring, ignoreCase = ignoreCase).matches(this.fetchSemanticsNode())
}
