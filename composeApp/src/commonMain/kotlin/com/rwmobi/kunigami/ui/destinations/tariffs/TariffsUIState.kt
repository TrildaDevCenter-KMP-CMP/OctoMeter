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

package com.rwmobi.kunigami.ui.destinations.tariffs

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rwmobi.kunigami.domain.exceptions.HttpException
import com.rwmobi.kunigami.domain.model.product.ProductDetails
import com.rwmobi.kunigami.domain.model.product.ProductSummary
import com.rwmobi.kunigami.ui.extensions.generateRandomLong
import com.rwmobi.kunigami.ui.extensions.getPlatformType
import com.rwmobi.kunigami.ui.extensions.mapFromPlatform
import com.rwmobi.kunigami.ui.model.ErrorMessage
import com.rwmobi.kunigami.ui.model.PlatformType
import com.rwmobi.kunigami.ui.model.ScreenSizeInfo
import com.rwmobi.kunigami.ui.model.SpecialErrorScreen
import io.ktor.util.network.UnresolvedAddressException
import kunigami.composeapp.generated.resources.Res
import kunigami.composeapp.generated.resources.account_error_load_account
import org.jetbrains.compose.resources.getString

@Immutable
data class TariffsUIState(
    val isLoading: Boolean = true,
    val queryPostCode: String? = null,
    val productSummaries: List<ProductSummary> = emptyList(),
    val productDetails: ProductDetails? = null,
    val requestedScreenType: TariffsScreenType? = null,
    val requestedLayout: TariffScreenLayoutStyle = TariffScreenLayoutStyle.Compact(useBottomSheet = true),
    val requestedWideListLayout: Boolean = false,
    val requestScrollToTop: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
) {
    private val windowWidthCompact: Dp = 599.dp

    fun shouldUseBottomSheet(): Boolean = with(requestedLayout) {
        (this is TariffScreenLayoutStyle.Wide && useBottomSheet) ||
            (this is TariffScreenLayoutStyle.Compact && useBottomSheet)
    }

    // Make it less intrusive when hopping among products
    fun shouldShowLoadingScreen() = isLoading && productSummaries.isEmpty()

    fun updateLayoutType(screenSizeInfo: ScreenSizeInfo, windowSizeClass: WindowSizeClass): TariffsUIState {
        val platform = windowSizeClass.getPlatformType()
        val requestedLayout = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> TariffScreenLayoutStyle.Compact(useBottomSheet = platform != PlatformType.DESKTOP)
            WindowWidthSizeClass.Medium -> TariffScreenLayoutStyle.Wide(useBottomSheet = platform != PlatformType.DESKTOP)
            else -> TariffScreenLayoutStyle.ListDetailPane
        }
        val requestedWideListLayout = when {
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact -> false
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium -> true
            (screenSizeInfo.widthDp / 2) > windowWidthCompact -> true // List pane width
            else -> false
        }

        return copy(
            requestedLayout = requestedLayout,
            requestedWideListLayout = requestedWideListLayout,
        ) // .updateScreenType()
    }

    fun updateScreenType(): TariffsUIState = copy(
        requestedScreenType = when {
            // Error Screen is kept until being told to dismiss
            isErrorScreen() -> requestedScreenType
            shouldShowTariffsList() -> TariffsScreenType.List
            hasProductDetailsLoaded() -> TariffsScreenType.FullScreenDetail
            else -> requestedScreenType // nothing triggered for a change, just keep it
        },
    )

    suspend fun filterErrorAndStopLoading(throwable: Throwable): TariffsUIState = when (val translatedThrowable = throwable.mapFromPlatform()) {
        is HttpException -> {
            copy(
                requestedScreenType = TariffsScreenType.Error(SpecialErrorScreen.HttpError(statusCode = translatedThrowable.httpStatusCode)),
                isLoading = false,
            )
        }

        is UnresolvedAddressException -> {
            copy(
                requestedScreenType = TariffsScreenType.Error(SpecialErrorScreen.NetworkError),
                isLoading = false,
            )
        }

        else -> {
            handleErrorAndStopLoading(message = throwable.message ?: getString(resource = Res.string.account_error_load_account))
        }
    }

    private fun handleErrorAndStopLoading(message: String): TariffsUIState {
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

    private fun shouldShowTariffsList(): Boolean = productDetails == null ||
        requestedLayout is TariffScreenLayoutStyle.ListDetailPane ||
        requestedLayout == TariffScreenLayoutStyle.Compact(useBottomSheet = true) ||
        requestedLayout == TariffScreenLayoutStyle.Wide(useBottomSheet = true)

    private fun isErrorScreen(): Boolean = requestedScreenType is TariffsScreenType.Error

    private fun hasProductDetailsLoaded(): Boolean = productDetails != null
}
