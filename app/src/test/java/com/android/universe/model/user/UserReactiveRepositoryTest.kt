package com.android.universe.model.user

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserReactiveRepositoryTest {
  private lateinit var mockFirestore: FirebaseFirestore
  private lateinit var repository: UserReactiveRepository
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setup() {
    mockFirestore = mockk(relaxed = true)

    repository = UserReactiveRepository(mockFirestore, testDispatcher)
  }

  @After
  fun tearDown() {
    repository.close()
  }

  @Test
  fun `getUserFlow returns system user for OpenAI uid`() = runTest {
    val flow = repository.getUserFlow("system_openai")

    val result = flow.first()

    assertEquals("system_openai", result?.uid)
    assertEquals("OpenAI", result?.username)
  }
}
