package com.android.universe.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val tagsInterest =
    listOf<String>(
        "Reading",
        "Writing",
        "Music",
        "Cinema",
        "Photography",
        "Video games",
        "Programming",
        "Artificial intelligence",
        "Astronomy",
        "Electronics",
        "Traveling",
        "Cooking",
        "Politics",
        "Philosophy",
        "Drawing",
        "Sculpture",
        "Poetry",
        "Fashion",
        "Board games",
        "Role-playing games",
        "Car")

val tagsSport =
    listOf<String>(
        "Running",
        "Fitness",
        "Swimming",
        "Cycling",
        "Mountain biking",
        "Hiking",
        "Yoga",
        "Meditation",
        "Pilates",
        "Judo",
        "Karate",
        "Boxing",
        "Football",
        "Basketball",
        "Volleyball",
        "Rugby",
        "Handball",
        "Tennis",
        "Badminton",
        "Table tennis",
        "Skiing",
        "Snowboarding",
        "Skating",
        "Surfing",
        "Golf",
        "Kayaking",
        "Dancing",
        "Horseback riding")

val tagsMusic =
    listOf<String>(
        "Jazz",
        "Pop",
        "Rock",
        "Rap",
        "Classical",
        "Blues",
        "Metal",
        "R&B",
        "Funk",
        "Reggae",
        "Electronic",
        "Country",
        "Indie",
        "Punk",
        "K-pop")

val tagsTransport = listOf<String>("Car", "Train", "Boat", "Bus", "Bicycle", "Foot", "Plane")

val tagsCanton =
    listOf<String>(
        "Aargau",
        "Appenzell Ausserrhoden",
        "Appenzell Innerrhoden",
        "Basel-Landschaft",
        "Basel-Stadt",
        "Bern",
        "Fribourg",
        "Geneva",
        "Glarus",
        "Graubünden",
        "Jura",
        "Lucerne",
        "Neuchâtel",
        "Nidwalden",
        "Obwalden",
        "Schaffhausen",
        "Schwyz",
        "Solothurn",
        "St. Gallen",
        "Thurgau",
        "Ticino",
        "Uri",
        "Valais",
        "Vaud",
        "Zug",
        "Zürich")

@Composable
private fun displayTags(
    name: String,
    listTag: List<String>,
    newtags: MutableState<List<String>>,
    onClick: (tag: String) -> Unit
) {
  if (listTag.isNotEmpty()) {
    Text(name)
    LazyRow() {
      items(listTag) { tag ->
        if (!newtags.value.contains(tag) || listTag == newtags.value) {
          Button(onClick = { onClick(tag) }) { Text(tag) }
        }
      }
    }
  }
}

@Composable
fun SelectTagScreen(modifier: Modifier = Modifier) {
  val newTags = remember { mutableStateOf(emptyList<String>()) }
  Box() {
    Column(modifier = Modifier.padding(10.dp)) {
      displayTags("Interest", tagsInterest, newTags, { tag -> newTags.value = newTags.value + tag })
      displayTags("Sport", tagsSport, newTags, { tag -> newTags.value = newTags.value + tag })
      displayTags("Music", tagsMusic, newTags, { tag -> newTags.value = newTags.value + tag })
      displayTags(
          "Transport", tagsTransport, newTags, { tag -> newTags.value = newTags.value + tag })
      displayTags("Canton", tagsCanton, newTags, { tag -> newTags.value = newTags.value + tag })
      displayTags(
          "Selected Tags", newTags.value, newTags, { tag -> newTags.value = newTags.value - tag })
    }
  }
}
