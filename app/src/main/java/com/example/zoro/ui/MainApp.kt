package com.example.zoro.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zoro.ui.screens.ZoroArchScreen
import com.example.zoro.ui.screens.ZoroBenchmarkScreen
import com.example.zoro.ui.screens.ZoroChatScreen
import com.example.zoro.ui.screens.ZoroSettingsScreen
import com.example.ui.theme.ZoroCyan
import com.example.ui.theme.ZoroEmerald

enum class NavDestination(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
) {
  CHAT("Chat", Icons.Filled.Forum, Icons.Outlined.Forum, "nav_tab_chat"),
  ARCH("Architecture", Icons.Filled.Architecture, Icons.Outlined.Architecture, "nav_tab_arch"),
  BENCHMARK("Benchmark", Icons.Filled.Speed, Icons.Outlined.Speed, "nav_tab_benchmark"),
  SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_tab_settings")
}

@Composable
fun MainApp(
  viewModel: ZoroViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  var currentDestination by remember { mutableStateOf(NavDestination.CHAT) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
      ) {
        NavDestination.values().forEach { destination ->
          val isSelected = currentDestination == destination
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentDestination = destination },
            icon = {
              Icon(
                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = destination.label,
                modifier = Modifier.size(24.dp)
              )
            },
            label = { Text(destination.label) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = ZoroCyan,
              selectedTextColor = ZoroCyan,
              indicatorColor = ZoroCyan.copy(alpha = 0.18f)
            ),
            modifier = Modifier.testTag(destination.testTag)
          )
        }
      }
    }
  ) { innerPadding ->
    when (currentDestination) {
      NavDestination.CHAT -> {
        ZoroChatScreen(
          viewModel = viewModel,
          uiState = uiState,
          modifier = Modifier.padding(innerPadding)
        )
      }
      NavDestination.ARCH -> {
        ZoroArchScreen(
          modifier = Modifier.padding(innerPadding)
        )
      }
      NavDestination.BENCHMARK -> {
        ZoroBenchmarkScreen(
          viewModel = viewModel,
          uiState = uiState,
          modifier = Modifier.padding(innerPadding)
        )
      }
      NavDestination.SETTINGS -> {
        ZoroSettingsScreen(
          viewModel = viewModel,
          uiState = uiState,
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}
