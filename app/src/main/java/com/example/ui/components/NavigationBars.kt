package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.SubScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusLifeTopBar(
    subtitle: String,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onExecutiveDeskClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onExecutiveDeskClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CampusLife",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("top_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("top_notifications_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Red badge dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Error)
                )
            }
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("top_profile_btn")
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAkl376-ud6ID6QH98fsaDb8X6znuuCw9iiOt47DQxwDGSNT0F_kDt0XWaxxVf96SATPnae_AsXLkVSxEoQRTIL00B6_DcKZRsCRuS6mMA442kw53tcNx0iHQtod4ASxQbTPe5wPdNA9DJR4hKHlgnQNUqOovvdv54mTKRv-bMP2p1vlmGxnn0eZgFlCxPZcRZ7wYksT1MF3_z4zPyJOMs0dvCsf64WVS3cGXkpMsPRNsmQsAPlEX6XKA",
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    )
}

@Composable
fun CampusLifeBottomNav(
    currentTab: AppDestination,
    onTabSelected: (AppDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 6.dp
    ) {
        val items = listOf(
            Triple(AppDestination.HOME, Icons.Default.Home, Icons.Outlined.Home),
            Triple(AppDestination.TRACK, Icons.Default.EventAvailable, Icons.Outlined.EventAvailable),
            Triple(AppDestination.COMMUNITY, Icons.Default.Groups, Icons.Outlined.Groups),
            Triple(AppDestination.GROWTH, Icons.Default.TrendingUp, Icons.Outlined.TrendingUp),
            Triple(AppDestination.ME, Icons.Default.AccountCircle, Icons.Outlined.AccountCircle)
        )

        items.forEach { (destination, selectedIcon, unselectedIcon) ->
            val isSelected = currentTab == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = PrimaryFixed.copy(alpha = 0.6f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
            )
        }
    }
}
