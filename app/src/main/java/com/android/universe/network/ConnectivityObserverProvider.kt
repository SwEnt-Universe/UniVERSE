package com.android.universe.network

/** An object to provide a global instance of [ConnectivityObserver]. */
object ConnectivityObserverProvider {
  lateinit var observer: ConnectivityObserver
}
