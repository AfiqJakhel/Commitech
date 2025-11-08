package com.example.commitech.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SharedBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.primary)
            .height(80.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Home
            SharedPillNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            // About Us
            SharedPillNavItem(
                icon = Icons.Default.Info,
                label = "About",
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )

            // Profile
            SharedPillNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
    }
}

@Composable
fun SharedPillNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colorScheme.surface else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "backgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else colorScheme.onPrimary.copy(alpha = 0.7f),
        animationSpec = tween(300),
        label = "contentColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = if (selected) 20.dp else 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(200)) + expandHorizontally(),
                exit = fadeOut(tween(150)) + shrinkHorizontally()
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}
