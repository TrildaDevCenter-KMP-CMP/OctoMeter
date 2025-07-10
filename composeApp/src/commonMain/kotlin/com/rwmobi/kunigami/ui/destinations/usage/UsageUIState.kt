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

package com.rwmobi.kunigami.ui.destinations.usage

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp
import com.rwmobi.kunigami.domain.exceptions.HttpException
import com.rwmobi.kunigami.domain.model.account.UserProfile
import com.rwmobi.kunigami.domain.model.product.Tariff
import com.rwmobi.kunigami.ui.extensions.generateRandomLong
import com.rwmobi.kunigami.ui.extensions.getPlatformType
import com.rwmobi.kunigami.ui.extensions.mapFromPlatform
import com.rwmobi.kunigami.ui.model.ErrorMessage
import com.rwmobi.kunigami.ui.model.PlatformType
import com.rwmobi.kunigami.ui.model.ScreenSizeInfo
import com.rwmobi.kunigami.ui.model.SpecialErrorScreen
import com.rwmobi.kunigami.ui.model.chart.BarChartData
import com.rwmobi.kunigami.ui.model.chart.RequestedChartLayout
import com.rwmobi.kunigami.ui.model.consumption.ConsumptionGroupedCells
import com.rwmobi.kunigami.ui.model.consumption.ConsumptionQueryFilter
import com.rwmobi.kunigami.ui.model.consumption.Insights
import io.ktor.util.network.UnresolvedAddressException
import kunigami.composeapp.generated.resources.Res
import kunigami.composeapp.generated.resources.account_error_load_tariff
import org.jetbrains.compose.resources.getString

@Immutable
data class UsageUIState(
    val isLoading: Boolean = true,
    val isDemoMode: Boolean? = null,
    val userProfile: UserProfile? = null,
    val tariff: Tariff? = null,
    val showToolTipOnClick: Boolean = false,
    val requestedScreenType: UsageScreenType = UsageScreenType.Chart,
    val requestedChartLayout: RequestedChartLayout = RequestedChartLayout.Portrait,
    val requestedAdaptiveLayout: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    val requestedUsageColumns: Int = 1,
    val consumptionQueryFilter: ConsumptionQueryFilter? = null,
    val consumptionGroupedCells: List<ConsumptionGroupedCells> = emptyList(),
    val consumptionRange: ClosedFloatingPointRange<Double> = 0.0..0.0,
    val barChartData: BarChartData? = null,
    val insights: Insights? = null,
    val requestScrollToTop: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
) {
    private val usageColumnWidth = 175.dp

    fun updateLayoutType(screenSizeInfo: ScreenSizeInfo, windowSizeClass: WindowSizeClass): UsageUIState {
        val showToolTipOnClick = windowSizeClass.getPlatformType() != PlatformType.DESKTOP
        val usageColumns = (screenSizeInfo.widthDp / usageColumnWidth).toInt()
        val requestedLayout = if (screenSizeInfo.isPortrait()) {
            RequestedChartLayout.Portrait
        } else {
            RequestedChartLayout.LandScape(
                requestedMaxHeight = screenSizeInfo.heightDp / 2,
            )
        }

        return copy(
            showToolTipOnClick = showToolTipOnClick,
            requestedAdaptiveLayout = windowSizeClass.widthSizeClass,
            requestedChartLayout = requestedLayout,
            requestedUsageColumns = usageColumns,
        )
    }

    fun clearDataFieldsAndStopLoading() = copy(
        userProfile = null,
        consumptionGroupedCells = listOf(),
        consumptionRange = 0.0..0.0,
        barChartData = null,
        insights = null,
        isLoading = false,
    )

    suspend fun filterErrorAndStopLoading(throwable: Throwable, defaultMessage: String? = null): UsageUIState = when (val translatedThrowable = throwable.mapFromPlatform()) {
        is HttpException -> {
            copy(
                requestedScreenType = UsageScreenType.Error(SpecialErrorScreen.HttpError(statusCode = translatedThrowable.httpStatusCode)),
                isLoading = false,
            )
        }

        is UnresolvedAddressException -> {
            copy(
                requestedScreenType = UsageScreenType.Error(SpecialErrorScreen.NetworkError),
                isLoading = false,
            )
        }

        else -> {
            handleErrorAndStopLoading(message = throwable.message ?: defaultMessage ?: getString(resource = Res.string.account_error_load_tariff))
        }
    }

    private fun handleErrorAndStopLoading(message: String): UsageUIState {
        val newErrorMessages = if (errorMessages.any { it.message == message }) {
            errorMessages
        } else {
            errorMessages + ErrorMessage(
                id = generateRandomLong(),
                message = message,
            )
        }
        return copy(
            errorMessages = newErrorMessages,
            isLoading = false,
        )
    }
}
