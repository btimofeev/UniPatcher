/*
 Copyright (c) 2026 Boris Timofeev

 This file is part of UniPatcher.

 UniPatcher is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 UniPatcher is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with UniPatcher.  If not, see <http://www.gnu.org/licenses/>.

 */

package org.emunix.unipatcher.ui.main

import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.emunix.unipatcher.BuildConfig
import org.emunix.unipatcher.FLAVOR_FREE
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.ui.help.HelpScreen
import org.emunix.unipatcher.ui.settings.SettingsScreen
import org.emunix.unipatcher.utils.PendingPatch
import org.emunix.unipatcher.viewmodels.ActionIsRunningViewModel
import org.emunix.unipatcher.viewmodels.ApplyPatchViewModel
import org.emunix.unipatcher.viewmodels.CreatePatchViewModel
import org.emunix.unipatcher.viewmodels.SmdFixChecksumViewModel
import org.emunix.unipatcher.viewmodels.SnesSmcHeaderViewModel
import kotlin.random.Random

private const val DOUBLE_BACK_EXIT_DELAY_MS = 2000L
private const val DIALOG_SHOW_DELAY_MS = 300L
private const val DIALOG_MIN_VISIBLE_MS = 600L

@Composable
fun MainScreen(
    settings: Settings,
    appVersion: String,
    pendingPatch: PendingPatch,
    onVisitSiteClick: () -> Unit,
    onChangelogClick: () -> Unit,
    onRate: () -> Unit,
    onShare: () -> Unit,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val actionIsRunningViewModel: ActionIsRunningViewModel = viewModel()
    val actionIsRunning by actionIsRunningViewModel.actionIsRunning.collectAsStateWithLifecycle()
    val actionStatus by actionIsRunningViewModel.status.collectAsStateWithLifecycle()
    val actionResult by actionIsRunningViewModel.result.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var dialogShownAt by remember { mutableStateOf(0L) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var resultIsError by remember { mutableStateOf(false) }
    var lastActionStatus by remember { mutableStateOf<Int?>(null) }

    val closeActionDialog: () -> Unit = {
        showDialog = false
        resultMessage = null
        actionIsRunningViewModel.clearResult()
    }

    LaunchedEffect(actionIsRunning) {
        if (actionIsRunning) {
            delay(DIALOG_SHOW_DELAY_MS)
            if (actionIsRunning && resultMessage == null) {
                dialogShownAt = SystemClock.uptimeMillis()
                showDialog = true
            }
        } else {
            if (resultMessage == null && showDialog) {
                val remaining = DIALOG_MIN_VISIBLE_MS -
                    (SystemClock.uptimeMillis() - dialogShownAt)
                if (remaining > 0) delay(remaining)
                showDialog = false
            }
        }
    }

    LaunchedEffect(actionStatus) {
        actionStatus?.let { lastActionStatus = it }
    }

    LaunchedEffect(actionResult) {
        val result = actionResult
        if (result != null) {
            resultMessage = result.message
            resultIsError = result.isError
            showDialog = true
        }
    }

    val runActions = remember { mutableStateMapOf<String, () -> Unit>() }
    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val registerRunAction: (String, () -> Unit) -> Unit = { route, action ->
        runActions[route] = action
    }

    val navigateToAction: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
        scope.launch { drawerState.close() }
    }

    val pendingPatchUri by pendingPatch.patch.collectAsStateWithLifecycle()
    LaunchedEffect(pendingPatchUri, currentRoute) {
        if (pendingPatchUri != null && currentRoute != null && currentRoute != MainRoutes.APPLY_PATCH) {
            navigateToAction(MainRoutes.APPLY_PATCH)
        }
    }

    val navigateToSecondary: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
        }
        scope.launch { drawerState.close() }
    }

    val donateSnackbarText = stringResource(R.string.main_activity_donate_snackbar_text)
    val donateSnackbarButton = stringResource(R.string.main_activity_donate_snackbar_button)
    val doubleBackMessage = stringResource(R.string.main_activity_double_back_to_exit_message)

    BackHandler {
        when {
            showDialog && resultMessage != null -> closeActionDialog()
            drawerState.isOpen -> scope.launch { drawerState.close() }
            isSecondaryRoute(currentRoute) -> navController.popBackStack()
            !actionIsRunning || doubleBackToExitPressedOnce -> {
                (context as? android.app.Activity)?.finish()
            }
            else -> {
                doubleBackToExitPressedOnce = true
                Toast.makeText(
                    context,
                    doubleBackMessage,
                    Toast.LENGTH_SHORT,
                ).show()
                scope.launch {
                    delay(DOUBLE_BACK_EXIT_DELAY_MS)
                    doubleBackToExitPressedOnce = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (BuildConfig.FLAVOR == FLAVOR_FREE) {
            if (canShowDonateSnackbar(settings)) {
                val result = snackbarHostState.showSnackbar(
                    message = donateSnackbarText,
                    actionLabel = donateSnackbarButton,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> navigateToSecondary(MainRoutes.DONATE)
                    SnackbarResult.Dismissed -> {
                        settings.setDontShowDonateSnackbarCount(30)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentRoute) {
            MainRoutes.SETTINGS -> SettingsScreen(
                viewModel = hiltViewModel(),
                onBackPressed = { navController.popBackStack() },
            )

            MainRoutes.HELP -> HelpScreen(
                appVersion = appVersion,
                onBackPressed = { navController.popBackStack() },
                onVisitSiteClick = onVisitSiteClick,
                onChangelogClick = onChangelogClick,
            )

            MainRoutes.DONATE -> DonateScreenRoute(
                onBackPressed = { navController.popBackStack() },
            )

            else -> {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = !actionIsRunning,
                    drawerContent = {
                        DrawerContent(
                            currentRoute = currentRoute,
                            onActionClick = navigateToAction,
                            onSecondaryClick = navigateToSecondary,
                            onCloseDrawer = { scope.launch { drawerState.close() } },
                            onRate = onRate,
                            onShare = onShare,
                        )
                    },
                ) {
                    Scaffold(
                        topBar = {
                            TopBar(
                                title = stringResource(titleForRoute(currentRoute)),
                                onMenuClick = { scope.launch { drawerState.open() } },
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = {
                                    currentRoute?.let { runActions[it]?.invoke() }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_save),
                                    contentDescription = null,
                                )
                            }
                        },
                    ) { padding ->
                        NavHost(
                            navController = navController,
                            startDestination = MainRoutes.APPLY_PATCH,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                        ) {
                            composable(MainRoutes.APPLY_PATCH) {
                                val viewModel = hiltViewModel<ApplyPatchViewModel>()
                                ApplyPatchScreen(
                                    viewModel = viewModel,
                                    actionIsRunningViewModel = actionIsRunningViewModel,
                                    pendingPatch = pendingPatch,
                                    registerRunAction = registerRunAction,
                                    onShowHelp = { navigateToSecondary(MainRoutes.HELP) },
                                )
                            }
                            composable(MainRoutes.CREATE_PATCH) {
                                val viewModel = hiltViewModel<CreatePatchViewModel>()
                                CreatePatchScreen(
                                    viewModel = viewModel,
                                    actionIsRunningViewModel = actionIsRunningViewModel,
                                    registerRunAction = registerRunAction,
                                )
                            }
                            composable(MainRoutes.SMD_FIX_CHECKSUM) {
                                val viewModel = hiltViewModel<SmdFixChecksumViewModel>()
                                SmdFixChecksumScreen(
                                    viewModel = viewModel,
                                    actionIsRunningViewModel = actionIsRunningViewModel,
                                    registerRunAction = registerRunAction,
                                )
                            }
                            composable(MainRoutes.SNES_SMC_HEADER) {
                                val viewModel = hiltViewModel<SnesSmcHeaderViewModel>()
                                SnesSmcHeaderScreen(
                                    viewModel = viewModel,
                                    actionIsRunningViewModel = actionIsRunningViewModel,
                                    registerRunAction = registerRunAction,
                                )
                            }
                            composable(MainRoutes.SETTINGS) {}
                            composable(MainRoutes.HELP) {}
                            composable(MainRoutes.DONATE) {}
                        }
                    }
                }

                if (showDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Card(
                            modifier = Modifier.width(280.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            if (resultMessage != null) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = stringResource(
                                            if (resultIsError) {
                                                R.string.notify_error
                                            } else {
                                                R.string.notify_success
                                            }
                                        ),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (resultIsError) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (resultIsError) {
                                                Icons.Filled.Clear
                                            } else {
                                                Icons.Filled.Done
                                            },
                                            contentDescription = null,
                                            tint = if (resultIsError) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(32.dp),
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = resultMessage.orEmpty(),
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(
                                        onClick = closeActionDialog,
                                        modifier = Modifier.align(Alignment.End),
                                    ) {
                                        Text(text = stringResource(R.string.action_dialog_close))
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp,
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = stringResource(
                                            lastActionStatus ?: R.string.status_working
                                        ),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    onMenuClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.nav_drawer_open),
                )
            }
        },
    )
}

@Composable
private fun DrawerContent(
    currentRoute: String?,
    onActionClick: (String) -> Unit,
    onSecondaryClick: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onRate: () -> Unit,
    onShare: () -> Unit,
) {
    val itemShape = RoundedCornerShape(
        topEnd = 32.dp,
        bottomEnd = 32.dp,
    )

    ModalDrawerSheet(
        windowInsets = WindowInsets(0.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.app_name),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                fontSize = 30.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_apply_patch)) },
                selected = currentRoute == MainRoutes.APPLY_PATCH,
                onClick = { onActionClick(MainRoutes.APPLY_PATCH) },
                icon = { Icon(painterResource(R.drawable.ic_healing), contentDescription = null) },
                shape = itemShape,
                modifier = Modifier.padding(end = 8.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_create_patch)) },
                selected = currentRoute == MainRoutes.CREATE_PATCH,
                onClick = { onActionClick(MainRoutes.CREATE_PATCH) },
                icon = { Icon(painterResource(R.drawable.ic_add_box), contentDescription = null) },
                shape = itemShape,
                modifier = Modifier.padding(end = 8.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_smd_fix_checksum)) },
                selected = currentRoute == MainRoutes.SMD_FIX_CHECKSUM,
                onClick = { onActionClick(MainRoutes.SMD_FIX_CHECKSUM) },
                icon = { Icon(painterResource(R.drawable.ic_fingerprint), contentDescription = null) },
                shape = itemShape,
                modifier = Modifier.padding(end = 8.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_snes_add_del_smc_header)) },
                selected = currentRoute == MainRoutes.SNES_SMC_HEADER,
                onClick = { onActionClick(MainRoutes.SNES_SMC_HEADER) },
                icon = { Icon(painterResource(R.drawable.ic_content_cut), contentDescription = null) },
                shape = itemShape,
                modifier = Modifier.padding(end = 8.dp),
            )

            Spacer(Modifier.height(32.dp))

            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_settings)) },
                selected = currentRoute == MainRoutes.SETTINGS,
                onClick = {
                    onCloseDrawer()
                    onSecondaryClick(MainRoutes.SETTINGS)
                },
                icon = { Icon(painterResource(R.drawable.ic_settings), contentDescription = null) },
                shape = itemShape,
                modifier = Modifier.padding(end = 8.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_rate)) },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    onRate()
                },
                icon = { Icon(painterResource(R.drawable.ic_thumb_up), contentDescription = null) },
                shape = itemShape,
                modifier = Modifier.padding(end = 8.dp),
            )
            if (BuildConfig.FLAVOR == FLAVOR_FREE) {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_donate)) },
                    selected = currentRoute == MainRoutes.DONATE,
                    onClick = {
                        onCloseDrawer()
                        onSecondaryClick(MainRoutes.DONATE)
                    },
                    icon = { Icon(painterResource(R.drawable.ic_gift), contentDescription = null) },
                        shape = itemShape,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_share)) },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    onShare()
                },
                icon = { Icon(painterResource(R.drawable.ic_share), contentDescription = null) },
                shape = itemShape,
                modifier = Modifier.padding(end = 8.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_help)) },
                selected = currentRoute == MainRoutes.HELP,
                onClick = {
                    onCloseDrawer()
                    onSecondaryClick(MainRoutes.HELP)
                },
                icon = { Icon(painterResource(R.drawable.ic_help), contentDescription = null) },
                shape = itemShape,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

private fun titleForRoute(route: String?): Int {
    return when (route) {
        MainRoutes.CREATE_PATCH -> R.string.nav_create_patch
        MainRoutes.SMD_FIX_CHECKSUM -> R.string.nav_smd_fix_checksum
        MainRoutes.SNES_SMC_HEADER -> R.string.nav_snes_add_del_smc_header
        else -> R.string.nav_apply_patch
    }
}

private fun isSecondaryRoute(route: String?): Boolean {
    return route == MainRoutes.SETTINGS ||
        route == MainRoutes.HELP ||
        route == MainRoutes.DONATE
}

private fun canShowDonateSnackbar(settings: Settings): Boolean {
    return settings.getPatchingSuccessful() &&
        isShowDonateSnackbarDelayOver(settings) &&
        isShowDonateSnackbarRandom()
}

private fun isShowDonateSnackbarDelayOver(settings: Settings): Boolean {
    var count = settings.getDontShowDonateSnackbarCount()
    if (count > 0) {
        settings.setDontShowDonateSnackbarCount(--count)
        return false
    } else {
        return true
    }
}

private fun isShowDonateSnackbarRandom() = (Random.nextInt(6) == 0)