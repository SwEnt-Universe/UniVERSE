package com.android.universe.model.user

import com.android.universe.model.Tag
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test

class UserRepositoryProviderTest {

  @Test
  fun repositoryIsNotNull() {
    val repo = UserRepositoryProvider.repository
    assertNotNull("Repository should not be null", repo)
  }

  @Test
  fun repositoryCanBeReplaced() {
    val originalRepo = UserRepositoryProvider.repository

    val fakeRepo =
        object : UserRepository {
          override suspend fun getAllUsers() = emptyList<UserProfile>()

          override suspend fun getUser(username: String) =
              UserProfile("", "", "", "", dateOfBirth = LocalDate.now(), tags = emptyList<Tag>())

          override suspend fun addUser(userProfile: UserProfile) {}

          override suspend fun updateUser(username: String, newUserProfile: UserProfile) {}

          override suspend fun deleteUser(username: String) {}

          override suspend fun isUsernameUnique(username: String) = true
        }

    // Swap repository
    UserRepositoryProvider.repository = fakeRepo
    assertEquals("Repository should be replaced", fakeRepo, UserRepositoryProvider.repository)

    // Restore original
    UserRepositoryProvider.repository = originalRepo
  }
}
