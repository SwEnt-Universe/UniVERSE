package com.android.universe.utils

import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate

object UserTestData {
  val noTag = emptySet<Tag>()
  val singleTag = setOf(Tag.METAL)
  val twoTags = setOf(Tag.ROCK, Tag.POP)
  val someTags = setOf(Tag.ROCK, Tag.POP, Tag.METAL, Tag.JAZZ, Tag.BLUES, Tag.COUNTRY)
  val manyTags =
      (Tag.getTagsForCategory(Tag.Category.INTEREST) + Tag.getTagsForCategory(Tag.Category.CANTON))
          .toSet()

  private val DummyDate = LocalDate.of(2000, 8, 11)

  val Alice =
      UserProfile(
          uid = "1",
          username = "Alice",
          firstName = "second",
          lastName = "User2",
          country = "France",
          description = "a second user",
          dateOfBirth = LocalDate.of(2005, 12, 15),
          tags = setOf(Tag.TENNIS))

  val Bob =
      UserProfile(
          uid = "0",
          username = "Bob",
          firstName = "Test",
          lastName = "User",
          country = "Switzerland",
          description = "Just a test user",
          dateOfBirth = LocalDate.of(1990, 1, 1),
          tags = setOf(Tag.MUSIC, Tag.METAL))

  val Rocky =
      UserProfile(
          uid = "2",
          username = "Rocky",
          firstName = "third",
          lastName = "User3",
          country = "Portugal",
          description = "a third user",
          dateOfBirth = LocalDate.of(2012, 9, 12),
          tags = setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE))

  val FullDescription =
      UserProfile(
          uid = "20",
          username = "desc",
          firstName = "Desc",
          lastName = "Ription",
          country = "FR",
          description = "Maxime the goat",
          dateOfBirth = DummyDate,
          tags = noTag)

  val EmptyDescription =
      UserProfile(
          uid = "20",
          username = "desc",
          firstName = "Desc",
          lastName = "Ription",
          country = "FR",
          description = "",
          dateOfBirth = DummyDate,
          tags = noTag)

  val NullDescription =
      UserProfile(
          uid = "21",
          username = "desc",
          firstName = "Desc",
          lastName = "Ription",
          country = "FR",
          description = null,
          dateOfBirth = DummyDate,
          tags = noTag)
  val ManyTagsUser =
      UserProfile(
          uid = "22",
          username = "desc",
          firstName = "Desc",
          lastName = "Ription",
          country = "FR",
          description = "desc",
          dateOfBirth = DummyDate,
          tags = manyTags)
  val SomeTagsUser =
      UserProfile(
          uid = "23",
          username = "desc",
          firstName = "Desc",
          lastName = "Ription",
          country = "FR",
          description = "desc",
          dateOfBirth = DummyDate,
          tags = someTags)

  val NoTagsUser =
      UserProfile(
          uid = "23",
          username = "desc",
          firstName = "Desc",
          lastName = "Ription",
          country = "FR",
          description = "desc",
          dateOfBirth = DummyDate,
          tags = noTag)
}
