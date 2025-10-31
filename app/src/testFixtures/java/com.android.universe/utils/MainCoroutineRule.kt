package com.android.universe.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Installs a Test dispatcher on Dispatchers.Main for JVM unit tests.
 *
 * Usage: @get:Rule val mainDispatcherRule = MainDispatcherRule()
 * runTest(mainDispatcherRule.dispatcher) { ... }
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {

  override fun starting(description: Description) {
    Dispatchers.setMain(dispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}
