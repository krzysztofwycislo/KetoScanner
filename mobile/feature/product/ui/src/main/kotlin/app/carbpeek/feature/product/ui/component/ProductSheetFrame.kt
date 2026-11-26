package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R

@Composable
internal fun ProductSheetFrame(
    onDismiss: () -> Unit,
    scrollState: ScrollState = rememberScrollState(),
    header: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProductSheetScaffold(onDismiss = onDismiss, scrollState = scrollState) {
        ProductSheetBody(
            scrollState = scrollState,
            header = header,
            content = content,
        )
    }
}

@Composable
internal fun ProductSheetScaffold(
    onDismiss: () -> Unit,
    scrollState: ScrollState,
    body: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(weight = 1f, fill = false)) {
            Handle()
            body()
        }
        Footer(scrollState = scrollState, onDismiss = onDismiss)
    }
}

@Composable
internal fun ColumnScope.ProductSheetBody(
    scrollState: ScrollState,
    header: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    header?.invoke()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(weight = 1f, fill = false)
            .verticalScroll(scrollState)
            .padding(
                start = AppTheme.spacing.xxl,
                top = if (header == null) AppTheme.spacing.xs else 0.dp,
                end = AppTheme.spacing.xxl,
                bottom = AppTheme.spacing.xxl,
            ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
        content = content,
    )
}

@Composable
private fun Handle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppTheme.sizes.control.xs),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = AppTheme.sizes.icon.xl, height = AppTheme.spacing.xxs)
                .background(AppTheme.colors.border.strong, AppTheme.shapes.pill),
        )
    }
}

@Composable
private fun Footer(scrollState: ScrollState, onDismiss: () -> Unit) {
    val lineColor = AppTheme.colors.border.default
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppTheme.borders.hairline)
                .drawBehind {
                    if (scrollState.canScrollForward) {
                        drawRect(lineColor)
                    }
                },
        )
        Ui.PrimaryButton(
            text = stringResource(R.string.product_ingredient_got_it),
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppTheme.spacing.xxl,
                    top = AppTheme.spacing.s,
                    end = AppTheme.spacing.xxl,
                    bottom = AppTheme.spacing.xxl,
                ),
        )
    }
}

@Composable
internal fun ProductSheetPreview(content: @Composable () -> Unit) {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background.surface),
            propagateMinConstraints = true,
        ) {
            content()
        }
    }
}
