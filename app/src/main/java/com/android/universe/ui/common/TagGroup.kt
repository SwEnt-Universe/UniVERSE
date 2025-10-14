package com.android.universe.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagGroup(
	name: String,
	tagList: List<String>,
	selectedTags: List<String>,
	color: Color = Color(0xFF6650a4),
	onTagSelect: (String) -> Unit = {},
	onTagReSelect: (String) -> Unit = {},
	modifier: Modifier = Modifier
) {
	if (name.isNotEmpty()) {
		Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
	}
	FlowRow(modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
		tagList.forEach { tag ->
			val isSelected = selectedTags.contains(tag)
			val buttonColor by animateColorAsState(targetValue = if (isSelected) Color.Gray else color)
			Button(
				onClick = {
					if (isSelected) {
						onTagReSelect(tag)
					} else {
						onTagSelect(tag)
					}
				},
				modifier = Modifier.padding(4.dp),
				border = if (isSelected) BorderStroke(2.dp, Color(0xFF546E7A)) else null,
				colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text(tag)
					if (isSelected) {
						Spacer(modifier = Modifier.width(4.dp))
						Icon(
							imageVector = Icons.Default.Check,
							contentDescription = "Selected",
							tint = Color.White,
							modifier = Modifier.size(18.dp)
						)
					}
				}
			}
		}
	}
}