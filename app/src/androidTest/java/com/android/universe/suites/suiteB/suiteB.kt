package com.android.universe.suites.suiteB

import com.android.universe.ui.profile.UserProfileScreenTest
import com.android.universe.ui.profileCreation.AddProfileScreenTest
import com.android.universe.ui.profileSettings.SettingsModalTest
import com.android.universe.ui.profileSettings.SettingsScreenTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    UserProfileScreenTest::class,
    AddProfileScreenTest::class,
    SettingsModalTest::class,
    SettingsScreenTest::class)
class ConnectedSuiteA
