package com.android.universe.ui.eventCreation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.universe.model.ai.gemini.EventProposal
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.components.LiquidSearchBar

@Composable
fun MagicFillDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    isGenerating: Boolean,
    proposal: EventProposal?,
    error: String?,
    onGenerate: (String) -> Unit,
    onAccept: () -> Unit
) {
  var prompt by remember { mutableStateOf("") }

  LaunchedEffect(isVisible) { if (isVisible && proposal == null) prompt = "" }

  if (isVisible) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss),
        contentAlignment = Alignment.Center) {
          LiquidBox(
              modifier =
                  Modifier.padding(horizontal = 8.dp)
                      .fillMaxWidth()
                      .clickable(
                          interactionSource = remember { MutableInteractionSource() },
                          indication = null,
                          onClick = {}),
              shape = RoundedCornerShape(32.dp),
              blurRadius = 24.dp,
              refractionAmount = 10.dp) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      Icon(
                          imageVector = Icons.Default.AutoAwesome,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.onSurface,
                          modifier = Modifier.padding(bottom = 8.dp))
                      Text(
                          text = if (proposal == null) "Magic Fill" else "Review Proposal",
                          style = MaterialTheme.typography.titleLarge,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onSurface)

                      Spacer(modifier = Modifier.height(24.dp))

                      if (proposal == null) {
                        Text(
                            text = "Describe your event idea...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(24.dp))

                        LiquidSearchBar(
                            query = prompt,
                            onQueryChange = { prompt = it },
                            placeholder = "e.g. Tennis match in Vidy...",
                            modifier = Modifier.fillMaxWidth())

                        if (isGenerating) {
                          Spacer(modifier = Modifier.height(24.dp))
                          LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        if (error != null) {
                          Spacer(modifier = Modifier.height(16.dp))
                          Text(
                              text = error,
                              color = MaterialTheme.colorScheme.error,
                              style = MaterialTheme.typography.labelSmall,
                              textAlign = TextAlign.Center)
                        }
                      } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start) {
                              Text(
                                  text = "TITLE",
                                  style = MaterialTheme.typography.labelSmall,
                                  color = MaterialTheme.colorScheme.onSurface,
                                  fontWeight = FontWeight.Bold)
                              Text(
                                  text = proposal.title,
                                  style = MaterialTheme.typography.titleMedium,
                                  fontWeight = FontWeight.SemiBold)

                              Spacer(modifier = Modifier.height(16.dp))

                              Text(
                                  text = "DESCRIPTION",
                                  style = MaterialTheme.typography.labelSmall,
                                  color = MaterialTheme.colorScheme.onSurface,
                                  fontWeight = FontWeight.Bold)
                              Text(
                                  text = proposal.description,
                                  style = MaterialTheme.typography.bodyMedium)
                            }
                      }

                      Spacer(modifier = Modifier.height(24.dp))

                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceEvenly) {
                            LiquidButton(onClick = onDismiss, width = 130f, height = 44f) {
                              Icon(Icons.Default.Close, null)
                              Text("Cancel")
                            }

                            if (proposal == null) {
                              LiquidButton(
                                  onClick = { onGenerate(prompt) },
                                  enabled = prompt.isNotBlank() && !isGenerating,
                                  width = 130f,
                                  height = 44f) {
                                    Icon(Icons.Default.AutoAwesome, null)
                                    Text("Generate")
                                  }
                            } else {
                              LiquidButton(onClick = onAccept, width = 130f, height = 44f) {
                                Icon(Icons.Default.Check, null)
                                Text("Accept")
                              }
                            }
                          }
                    }
              }
        }
  }
}
