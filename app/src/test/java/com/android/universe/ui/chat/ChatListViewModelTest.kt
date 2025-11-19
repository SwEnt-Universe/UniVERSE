package com.android.universe.ui.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.ChatManager
import com.android.universe.model.chat.ChatRepository
import com.android.universe.model.chat.Utils.getNewSampleChat
import com.android.universe.model.event.EventRepository
import com.android.universe.utils.EventTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ChatListViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockChatRepository: ChatRepository

  private val userId = "user_123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockkObject(ChatManager)
    mockEventRepository = mockk(relaxed = true)
    mockChatRepository = mockk(relaxed = true)
    every { mockChatRepository.setMessageListener(any(), any(), any(), any()) } returns Unit
    every { mockChatRepository.setLastMessageListener(any(), any()) } returns Unit
    every { mockChatRepository.removeMessageListener(any()) } returns Unit
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `loads only events where user participates and builds chat previews`() =
      testScope.runTest {
        // Setup test events
        val e1 = EventTestData.dummyEvent1.copy(participants = setOf(userId, "x"))
        val e2 = EventTestData.dummyEvent2.copy(participants = setOf("y", userId))
        val e3 = EventTestData.dummyEvent3.copy(participants = setOf("nobody"))

        coEvery { mockEventRepository.getAllEvents() } returns listOf(e1, e2, e3)

        coEvery { ChatManager.loadChat(any()) } answers
            {
              val id = arg<String>(0)
              getNewSampleChat(id, mockChatRepository)
            }

        coEvery { ChatManager.createChat(any(), any()) } answers
            {
              val id = arg<String>(0)
              getNewSampleChat(id, mockChatRepository)
            }

        // Act
        val viewModel = ChatListViewModel(userId, mockEventRepository)
        advanceUntilIdle()

        val previews = viewModel.chatPreviews.value
        println(previews)
        println(previews.size)

        // Assert: 2 events included
        assertEquals(2, previews.size)

        val ids = previews.map { it.chatID }
        val titles = previews.map { it.chatName }
        print(ids)

        assertTrue(ids.contains(e1.id))
        assertTrue(ids.contains(e2.id))
        assertTrue(titles.contains(e1.title))
        assertTrue(titles.contains(e2.title))
      }

  @Test
  fun `creates chat when loadChat throws NoSuchElementException`() =
      testScope.runTest {
        val event = EventTestData.dummyEvent1.copy(participants = setOf(userId))

        coEvery { mockEventRepository.getAllEvents() } returns listOf(event)

        // Stub loadChat to throw
        coEvery { ChatManager.loadChat(event.id) } throws NoSuchElementException()

        // Stub createChat to return mockChat
        coEvery { ChatManager.createChat(any(), any()) } answers
            {
              val id = arg<String>(0)
              getNewSampleChat(id, mockChatRepository)
            }

        // Act
        val viewModel = ChatListViewModel(userId, mockEventRepository)
        advanceUntilIdle()

        val preview = viewModel.chatPreviews.first().first()

        assertEquals(event.title, preview.chatName)
        assertEquals(event.id, preview.chatID)
      }

  @Test
  fun `ignores dummy events where user is not a participant`() =
      testScope.runTest {
        val e1 = EventTestData.dummyEvent1.copy(participants = setOf("a", "b"))
        val e2 = EventTestData.dummyEvent2.copy(participants = setOf("c"))
        val e3 = EventTestData.dummyEvent3.copy(participants = setOf("d"))

        coEvery { mockEventRepository.getAllEvents() } returns listOf(e1, e2, e3)

        val viewModel = ChatListViewModel(userId, mockEventRepository)
        advanceUntilIdle()

        assertTrue(viewModel.chatPreviews.first().isEmpty())
      }
}
