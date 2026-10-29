package pl.handsome.club.ketoscanner.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import pl.handsome.club.core.designsystem.component.BottomBar
import pl.handsome.club.core.designsystem.component.BottomBarItem
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.text.rememberUiTextResolver
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.ketoscanner.navigation.AppNavHost
import pl.handsome.club.ketoscanner.navigation.TopLevelTab
import pl.handsome.club.ketoscanner.navigation.topLevelTab

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun KetoScannerApp(appEvents: AppEvents) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resolver = rememberUiTextResolver()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val hideKeyboard = {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    ObserveEvents(appEvents.events) { event ->
        when (event) {
            is AppEvent.ShowMessage -> scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(resolver.resolve(event.text), duration = SnackbarDuration.Long)
            }

            AppEvent.HideKeyboard -> hideKeyboard()
        }
    }

    val currentEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentEntry?.destination?.id) { hideKeyboard() }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = AppTheme.colors.background.default,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { AppBottomBar(navController) },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentTab = currentEntry?.destination?.topLevelTab()
    var lastTab by rememberSaveable { mutableStateOf(TopLevelTab.HOME) }
    LaunchedEffect(currentTab) { currentTab?.let { lastTab = it } }
    val selectedTab = currentTab ?: lastTab
    val items = TopLevelTab.entries
        .map { BottomBarItem(stringResource(it.label), painterResource(it.icon)) }
        .toImmutableList()

    Ui.BottomBar(
        items = items,
        selectedIndex = selectedTab.ordinal,
        onItemClick = { index ->
            lastTab = TopLevelTab.entries[index]
            navController.navigate(TopLevelTab.entries[index].route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
    )
}
