package com.example.commitech.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CircleIconButton(
    icon: ImageVector,
    background: Color,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = if (enabled) background else Color.LightGray.copy(alpha = 0.4f),
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(50.dp))
            .clickable(enabled = enabled) { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) tint else Color.Gray,
            modifier = Modifier.padding(6.dp)
        )
    }
}
