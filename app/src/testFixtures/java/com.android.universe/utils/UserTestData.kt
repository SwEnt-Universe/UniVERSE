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
  private val BaseUser =
      UserProfile(
          uid = "0",
          username = "Test",
          firstName = "Test",
          lastName = "User",
          country = "Switzerland",
          description = "Just a test user",
          dateOfBirth = DummyDate,
          tags = twoTags)

  val Alice =
      BaseUser.copy(
          uid = "1",
          username = "Alice",
          firstName = "second",
          lastName = "User2",
          country = "France",
          description = "a second user",
          dateOfBirth = LocalDate.of(2005, 12, 15),
          tags = setOf(Tag.TENNIS))

  val Bob =
      BaseUser.copy(
          username = "Bob",
          dateOfBirth = LocalDate.of(1990, 1, 1),
          tags = setOf(Tag.MUSIC, Tag.METAL))

  val Rocky =
      BaseUser.copy(
          uid = "2",
          username = "Rocky",
          firstName = "third",
          lastName = "User3",
          country = "Portugal",
          description = "a third user",
          dateOfBirth = LocalDate.of(2012, 9, 12),
          tags = setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE))

  val FullDescription = BaseUser.copy(uid = "20")

  val EmptyDescription = BaseUser.copy(uid = "20", description = "")

  val NullDescription = BaseUser.copy(uid = "21", description = null)
  val ManyTagsUser = BaseUser.copy(uid = "22", tags = manyTags)
  val SomeTagsUser = BaseUser.copy(uid = "23", tags = someTags)

  val NoTagsUser = BaseUser.copy(uid = "23", tags = noTag)
}
