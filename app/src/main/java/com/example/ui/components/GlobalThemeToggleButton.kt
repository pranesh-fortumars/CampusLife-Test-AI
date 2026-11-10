package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WarningAmber

/**
 * Global Accessible Theme Toggle Button that switches between Dark and Light modes.
 * Complies with Material 3 accessibility guidelines with >=48dp touch targets
 * and dynamic high-contrast color schemes.
 */
@Composable
fun GlobalThemeToggleButton(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onHeroHeader: Boolean = false,
    showLabel: Boolean = false
) {
    val description = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode"

    val containerColor = if (onHeroHeader) {
        if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.2f)
    } else {
        if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val contentColor = if (onHeroHeader) {
        if (isDark) WarningAmber else Color.White
    } else {
        if (isDark) WarningAmber else MaterialTheme.colorScheme.primary
    }

    val borderColor = if (onHeroHeader) {
        Color.White.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    if (showLabel) {
        Surface(
            onClick = onToggle,
            shape = RoundedCornerShape(100),
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = modifier
                .heightIn(min = 48.dp)
                .semantics {
                    role = Role.Button
                    this.contentDescription = description
                }
                .testTag("global_theme_toggle_button")
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .defaultMinSize(minHeight = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedContent(
                    targetState = isDark,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "theme_icon_labeled"
                ) { dark ->
                    Icon(
                        imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (isDark) "Light Mode" else "Dark Mode",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (onHeroHeader) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = modifier
                .size(48.dp)
                .semantics {
                    role = Role.Button
                    this.contentDescription = description
                }
                .testTag("global_theme_toggle_button")
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = isDark,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "theme_icon"
                ) { dark ->
                    Icon(
                        imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
