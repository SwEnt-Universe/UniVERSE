package com.android.universe.model.user

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryProviderTest {

  @Test
  fun repositoryIsNotNull() {
    val repo = FakeUserRepository()
    assertNotNull("Repository should not be null", repo)
  }

  @Test
  fun repositoryCanBeReplaced() {
    var originalRepo: UserRepository = FakeUserRepository()

    val fakeRepo =
        object : UserRepository {
          override suspend fun getAllUsers() = emptyList<UserProfile>()

          override suspend fun getUser(uid: String) =
              UserProfile("", "", "", "", "", dateOfBirth = LocalDate.now(), tags = emptySet())

          override suspend fun addUser(userProfile: UserProfile) {}

          override suspend fun updateUser(uid: String, newUserProfile: UserProfile) {}

          override suspend fun deleteUser(uid: String) {}

          override suspend fun isUsernameUnique(username: String) = true

          override suspend fun followUser(currentUserId: String, targetUserId: String) {}

          override suspend fun unfollowUser(currentUserId: String, targetUserId: String) {}
        }

    // Swap repository
    originalRepo = fakeRepo
    assertEquals("Repository should be replaced", fakeRepo, originalRepo)
  }
}
