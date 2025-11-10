package com.android.universe.utils

import com.android.universe.model.tag.Tag
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
          country = "CH",
          description = "Just a test user",
          dateOfBirth = DummyDate,
          tags = twoTags)

  val Alice =
      BaseUser.copy(
          uid = "1",
          username = "Alice",
          firstName = "second",
          lastName = "Usering",
          country = "FR",
          description = "a second user",
          dateOfBirth = LocalDate.of(2005, 12, 15),
          tags = noTag)
  const val aliceEmail = "faketest@epfl.ch"
  const val alicePassword = "test-password-123"

  val Bob =
      BaseUser.copy(
          username = "Bob",
          dateOfBirth = LocalDate.of(1990, 1, 1),
          tags = setOf(Tag.MUSIC, Tag.METAL))
  const val bobEmail = "fakebob@epfl.ch"
  const val bobPassword = "fake-pass123"

  val Rocky =
      BaseUser.copy(
          uid = "2",
          username = "Rocky",
          firstName = "third",
          lastName = "User",
          country = "PT",
          description = "a third user",
          dateOfBirth = LocalDate.of(2012, 9, 12),
          tags = setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE))

  val Arthur =
      BaseUser.copy(
          country = "CH",
          profileImageUri =
              "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 UWg//9k= ")

  val FullDescription = BaseUser.copy(uid = "20")

  val EmptyDescription = BaseUser.copy(uid = "20", description = "")

  val NullDescription = BaseUser.copy(uid = "21", description = null)
  val ManyTagsUser = BaseUser.copy(uid = "22", tags = manyTags)
  val SomeTagsUser = BaseUser.copy(uid = "23", tags = someTags)

  val NoTagsUser = BaseUser.copy(uid = "23", tags = noTag)
}
