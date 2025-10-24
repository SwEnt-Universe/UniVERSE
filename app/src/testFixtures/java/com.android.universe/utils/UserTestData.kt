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
          description = "",
          dateOfBirth = DummyDate,
          tags = manyTags)
  val SomeTagsUser =
      UserProfile(
          uid = "23",
          username = "desc",
          firstName = "Desc",
          lastName = "Ription",
          country = "FR",
          description = "",
          dateOfBirth = DummyDate,
          tags = someTags)

  val NoTagsUser =
      UserProfile(
          uid = "23",
          username = "desc",
          firstName = "Desc",
          lastName = "Ription",
          country = "FR",
          description = "",
          dateOfBirth = DummyDate,
          tags = noTag)
}
