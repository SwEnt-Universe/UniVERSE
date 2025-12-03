package com.android.universe.ui.map

/** Defines the interaction mode of the map UI.
*
* - `NORMAL`: Standard browsing mode where users can pan/zoom the map and view events.
* - `SELECT_LOCATION`: Special mode used when creating an event, allowing the user to pick
*   a specific location by clicking on the map.
*/
enum class MapMode {
  NORMAL,
  SELECT_LOCATION
}
