package com.android.universe.utils

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher

object CustomSemanticsMatcher {

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
}
