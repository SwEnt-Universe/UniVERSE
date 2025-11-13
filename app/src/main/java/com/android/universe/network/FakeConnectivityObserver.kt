package com.android.universe.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A fake implementation of [ConnectivityObserver] for testing purposes.
 *
 * @property isConnected A boolean indicating whether the network is connected or not.
 */
class FakeConnectivityObserver(private val isConnected: Boolean) : ConnectivityObserver {
  override fun observe(): Flow<ConnectivityObserver.Status> = flow {
    val status =
        if (isConnected) ConnectivityObserver.Status.Available
        else ConnectivityObserver.Status.Unavailable
    emit(status)
  }
}
