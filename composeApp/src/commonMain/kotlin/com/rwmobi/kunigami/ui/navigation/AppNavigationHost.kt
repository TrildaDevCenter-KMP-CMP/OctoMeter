/*
 * Copyright (c) 2024. RW MobiMedia UK Limited
 *
 * Contributions made by other developers remain the property of their respective authors but are licensed
 * to RW MobiMedia UK Limited and others under the same licence terms as the main project, as outlined in
 * the LICENSE file.
 *
 * RW MobiMedia UK Limited reserves the exclusive right to distribute this application on app stores.
 * Reuse of this source code, with or without modifications, requires proper attribution to
 * RW MobiMedia UK Limited.  Commercial distribution of this code or its derivatives without prior written
 * permission from RW MobiMedia UK Limited is prohibited.
 *
 * Please refer to the LICENSE file for the full terms and conditions.
 */

package com.rwmobi.kunigami.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rwmobi.kunigami.ui.composehelper.collectAsStateMultiplatform
import com.rwmobi.kunigami.ui.composehelper.getScreenSizeInfo
import com.rwmobi.kunigami.ui.destinations.account.AccountScreen
import com.rwmobi.kunigami.ui.destinations.account.AccountUIEvent
import com.rwmobi.kunigami.ui.destinations.agile.AgileScreen
import com.rwmobi.kunigami.ui.destinations.agile.AgileUIEvent
import com.rwmobi.kunigami.ui.destinations.tariffs.TariffsScreen
import com.rwmobi.kunigami.ui.destinations.tariffs.TariffsUIEvent
import com.rwmobi.kunigami.ui.destinations.usage.UsageScreen
import com.rwmobi.kunigami.ui.destinations.usage.UsageUIEvent
import com.rwmobi.kunigami.ui.extensions.calculateWindowSizeClass
import com.rwmobi.kunigami.ui.viewmodels.AccountViewModel
import com.rwmobi.kunigami.ui.viewmodels.AgileViewModel
import com.rwmobi.kunigami.ui.viewmodels.TariffsViewModel
import com.rwmobi.kunigami.ui.viewmodels.UsageViewModel
import org.koin.mp.KoinPlatform.getKoin

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    lastDoubleTappedNavItem: AppDestination?,
    onShowSnackbar: suspend (String) -> Unit,
    onScrolledToTop: (AppDestination) -> Unit,
) {
    val navigateToAccountTab = {
        navController.navigate(AppDestination.ACCOUNT.name) {
            navController.graph.startDestinationRoute?.let {
                popUpTo(it) {
                    inclusive = true
                }
            }
            launchSingleTop = true
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AppDestination.getStartDestination().name,
    ) {
        composable(route = AppDestination.USAGE.name) {
            // Workaround: passing through parameters not working on iOS, so we do it here
            val screenSizeInfo = getScreenSizeInfo()
            val windowSizeClass = calculateWindowSizeClass()
            val viewModel: UsageViewModel = viewModel { getKoin().get() }
            val uiState by viewModel.uiState.collectAsStateMultiplatform()

            viewModel.notifyScreenSizeChanged(
                screenSizeInfo = screenSizeInfo,
                windowSizeClass = windowSizeClass,
            )

            LaunchedEffect(lastDoubleTappedNavItem) {
                val enabled = lastDoubleTappedNavItem?.equals(AppDestination.USAGE) ?: false
                viewModel.requestScrollToTop(enabled = enabled)
            }

            UsageScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                uiEvent = UsageUIEvent(
                    onInitialLoad = viewModel::initialLoad,
                    onSwitchPresentationStyle = viewModel::onSwitchPresentationStyle,
                    onPreviousTimeFrame = viewModel::onPreviousTimeFrame,
                    onNextTimeFrame = viewModel::onNextTimeFrame,
                    onErrorShown = viewModel::errorShown,
                    onScrolledToTop = { onScrolledToTop(AppDestination.USAGE) },
                    onShowSnackbar = onShowSnackbar,
                    onNavigateToAccountTab = navigateToAccountTab,
                ),
            )
        }

        composable(route = AppDestination.AGILE.name) {
            // workaround: Issue with iOS we have to do it here
            val screenSizeInfo = getScreenSizeInfo()
            val windowSizeClass = calculateWindowSizeClass()
            val viewModel: AgileViewModel = viewModel { getKoin().get() }
            val uiState by viewModel.uiState.collectAsStateMultiplatform()

            viewModel.notifyScreenSizeChanged(
                screenSizeInfo = screenSizeInfo,
                windowSizeClass = windowSizeClass,
            )

            LaunchedEffect(lastDoubleTappedNavItem) {
                val enabled = lastDoubleTappedNavItem?.equals(AppDestination.AGILE) ?: false
                viewModel.requestScrollToTop(enabled = enabled)
            }

            AgileScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                uiEvent = AgileUIEvent(
                    onRefresh = viewModel::refresh,
                    onStartLiveConsumptionUpdates = viewModel::startLiveConsumptionUpdates,
                    onStopLiveConsumptionUpdates = viewModel::stopLiveConsumptionUpdates,
                    onErrorShown = viewModel::errorShown,
                    onScrolledToTop = { onScrolledToTop(AppDestination.AGILE) },
                    onShowSnackbar = onShowSnackbar,
                    onNavigateToAccountTab = navigateToAccountTab,
                ),
            )
        }

        composable(route = AppDestination.TARIFFS.name) {
            // workaround: Issue with iOS we have to do it here
            val screenSizeInfo = getScreenSizeInfo()
            val windowSizeClass = calculateWindowSizeClass()
            val viewModel: TariffsViewModel = viewModel { getKoin().get() }
            val uiState by viewModel.uiState.collectAsStateMultiplatform()

            viewModel.notifyScreenSizeChanged(
                screenSizeInfo = screenSizeInfo,
                windowSizeClass = windowSizeClass,
            )

            LaunchedEffect(lastDoubleTappedNavItem) {
                val enabled = lastDoubleTappedNavItem?.equals(AppDestination.TARIFFS) ?: false
                viewModel.requestScrollToTop(enabled = enabled)
            }

            TariffsScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                uiEvent = TariffsUIEvent(
                    onRefresh = viewModel::refresh,
                    onQueryPostcode = { viewModel.onQueryPostcode(it) },
                    onProductItemClick = { productCode, postcode -> viewModel.getProductDetails(productCode = productCode, postcode = postcode) },
                    onErrorShown = viewModel::errorShown,
                    onScrolledToTop = { onScrolledToTop(AppDestination.TARIFFS) },
                    onShowSnackbar = onShowSnackbar,
                    onProductDetailsDismissed = viewModel::onProductDetailsDismissed,
                    onSpecialErrorScreenShown = viewModel::onSpecialErrorScreenShown,
                ),
            )
        }

        composable(route = AppDestination.ACCOUNT.name) {
            val windowSizeClass = calculateWindowSizeClass()
            val viewModel: AccountViewModel = viewModel { getKoin().get() }
            val uiState by viewModel.uiState.collectAsStateMultiplatform()

            viewModel.notifyWindowSizeClassChanged(windowSizeClass = windowSizeClass)

            LaunchedEffect(lastDoubleTappedNavItem) {
                val enabled = lastDoubleTappedNavItem?.equals(AppDestination.ACCOUNT) ?: false
                viewModel.requestScrollToTop(enabled = enabled)
            }

            AccountScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                uiEvent = AccountUIEvent(
                    onRefresh = viewModel::refresh,
                    onClearCredentialButtonClicked = viewModel::clearCredentials,
                    onSubmitCredentials = viewModel::submitCredentials,
                    onMeterSerialNumberSelected = viewModel::updateMeterSerialNumber,
                    onErrorShown = viewModel::errorShown,
                    onScrolledToTop = { onScrolledToTop(AppDestination.ACCOUNT) },
                    onClearCache = viewModel::onClearCache,
                    onShowSnackbar = onShowSnackbar,
                    onSpecialErrorScreenShown = viewModel::onSpecialErrorScreenShown,
                ),
            )
        }
    }
}
