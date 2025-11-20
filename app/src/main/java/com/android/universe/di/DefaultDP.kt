package com.android.universe.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DefaultDP : DispatcherProvider {
  override val main: CoroutineDispatcher
    get() = Dispatchers.Main

  override val default: CoroutineDispatcher
    get() = Dispatchers.Default

  override val io: CoroutineDispatcher
    get() = Dispatchers.IO

  override val unconfined: CoroutineDispatcher
    get() = Dispatchers.Unconfined
}
