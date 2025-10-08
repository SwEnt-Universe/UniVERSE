package com.android.universe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.universe.ui.map.MapActivity
import com.android.universe.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = "main_screen_container" },
            color = MaterialTheme.colorScheme.background) {
              HomeScreen(onOpenMap = { startActivity(Intent(this, MapActivity::class.java)) })
            }
      }
    }
  }
}

@Composable
private fun HomeScreen(onOpenMap: () -> Unit) {
  Box(modifier = Modifier.fillMaxSize().semantics { testTag = "home_screen" }) {
    Button(
        onClick = onOpenMap,
        modifier =
            Modifier.align(Alignment.Center).padding(16.dp).semantics {
              testTag = "open_map_button"
            }) {
          Text("Open Map")
        }
  }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
  SampleAppTheme { HomeScreen(onOpenMap = {}) }
}
