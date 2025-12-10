package com.android.universe.ui.search

import com.android.universe.model.tag.Tag
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchEngineTest {
  companion object {
    val musicAndSport = setOf(Tag.Category.MUSIC, Tag.Category.SPORT)
    val onlyMusic = setOf(Tag.Category.MUSIC)
    val sportTag = setOf(Tag.RUNNING)
    val musicAndSportsTag = setOf(Tag.RUNNING, Tag.METAL)
  }

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

  @Test
  fun tagMatch_emptyQueryReturnsTrue() {
    assertEquals(true, SearchEngine.tagMatch(emptySet(), emptySet()))
  }

  @Test
  fun tagMatch_wrongTagIsFiltered() {
    assertEquals(false, SearchEngine.tagMatch(sportTag, onlyMusic))
  }

  @Test
  fun categoryCoverageComparator_sortsCorrectly() {
    val comparator = SearchEngine.categoryCoverageComparator<Set<Tag>>(musicAndSport) { it }
    val list = listOf(sportTag, musicAndSportsTag)
    val sortedList = list.sortedWith(comparator)
    assertEquals(musicAndSportsTag, sortedList.last())
  }
}
