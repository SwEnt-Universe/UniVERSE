package com.android.universe.suites.suiteA

import com.android.universe.ui.event.EventScreenTest
import com.android.universe.ui.map.MapScreenTest
import com.android.universe.ui.navigation.UniverseAppNavigationTest
import com.android.universe.ui.selectedTagScreen.SelectTagScreenTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    UniverseAppNavigationTest::class,
    MapScreenTest::class,
    EventScreenTest::class,
    SelectTagScreenTest::class)
class ConnectedSuiteA
