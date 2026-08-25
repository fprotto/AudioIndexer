package com.unitn.audioindexer.ui.screens.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.R

@Composable
fun QuickNavigateToSection(
    navController: NavController,
    currentSection: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationTab(
            label = stringResource(R.string.tracks_header),
            isSelected = currentSection == "Tracks",
            onClick = { navController.navigate("tracks") },
            modifier = Modifier.weight(1f)
        )
        NavigationTab(
            label = stringResource(R.string.artists_header),
            isSelected = currentSection == "Artists",
            onClick = { navController.navigate("artists") },
            modifier = Modifier.weight(1f)
        )
        NavigationTab(
            label = stringResource(R.string.albums_header),
            isSelected = currentSection == "Albums",
            onClick = { navController.navigate("albums") },
            modifier = Modifier.weight(1f)
        )
        NavigationTab(
            label = stringResource(R.string.playlists_header),
            isSelected = currentSection == "Playlists",
            onClick = { navController.navigate("playlists") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NavigationTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(contentColor)
            )
        }
    }
}
