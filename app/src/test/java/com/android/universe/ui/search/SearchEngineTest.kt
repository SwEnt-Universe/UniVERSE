package com.android.universe.ui.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchEngineTest {

  @Test
  fun fuzzyMatch_exactMatchReturnsTrue() {
    val result = SearchEngine.fuzzyMatch("concert", "concert")
    assertEquals(true, result)
  }

  @Test
  fun fuzzyMatch_smallDifferencesReturnTrue() {
    val result = SearchEngine.fuzzyMatch("concert", "concerrt")
    assertEquals(true, result)
  }

  @Test
  fun fuzzyMatch_largeDifferencesReturnFalse() {
    val result = SearchEngine.fuzzyMatch("concert", "basketball")
    assertEquals(false, result)
  }
}
