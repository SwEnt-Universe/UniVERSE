package com.android.universe.network

import kotlinx.coroutines.flow.Flow

/** An interface for observing network connectivity status. */
interface ConnectivityObserver {
  fun observe(): Flow<Status>

  enum class Status {
    Available,
    Unavailable,
    Losing,
    Lost
  }
}
