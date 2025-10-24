package com.android.universe.utils

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/**
 * An object to manage the connection to Firebase Emulators for Android tests.
 *
 * This object will automatically use the emulators if they are running when the tests start.
 */
object FirebaseEmulator {

  val auth: FirebaseAuth
    get() = Firebase.auth

  val firestore: FirebaseFirestore
    get() = Firebase.firestore

  init {
    auth.useEmulator("10.0.2.2", 9099)
    firestore.useEmulator("10.0.2.2", 8080)
  }
}
