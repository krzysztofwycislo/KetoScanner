package pl.handsome.club.core.designsystem.text

import android.content.Context
import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.theme.AppTheme
import java.util.Locale
import java.util.TimeZone

class UiTextResolver internal constructor(
    private val resources: Resources,
    private val locale: Locale,
) {

    fun resolve(text: UiText): String = when (text) {
        is UiText.Res ->
            if (text.args.isEmpty()) resources.getString(text.id)
            else resources.getString(text.id, *formatArgs(text.args))
        is UiText.Plural -> resources.getQuantityString(text.id, text.count, *formatArgs(text.args))
        is UiText.Raw -> text.value
    }

    private fun formatArgs(args: List<UiArg>): Array<Any> = args.map { formatArg(it) }.toTypedArray()

    private fun formatArg(arg: UiArg): String = when (arg) {
        is UiArg.Text -> arg.value
        is UiArg.Nested -> resolve(arg.text)
        is UiArg.Time -> arg.format(locale, TimeZone.getDefault())
        is UiArg.Date -> arg.format(locale, TimeZone.getDefault())
        is UiArg.Numeric -> arg.format(locale)
    }
}

fun uiTextResolver(context: Context): UiTextResolver =
    UiTextResolver(context.resources, context.resources.configuration.locales[0])

@Composable
fun rememberUiTextResolver(): UiTextResolver {
    val resources = LocalResources.current
    val locale = LocalConfiguration.current.locales[0]
    return remember(resources, locale) { UiTextResolver(resources, locale) }
}

@Composable
fun UiText.resolve(): String = rememberUiTextResolver().resolve(this)

@Preview(showBackground = true)
@Composable
private fun UiTextResolvePreview() {
    AppTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
            listOf(
                UiText.Res(R.string.designsystem_preview_title),
                UiText.Res(R.string.designsystem_preview_carbs, UiArg.Numeric.Grams(57.46)),
                UiText.Res(R.string.designsystem_preview_energy, UiArg.Numeric.Kcal(99.5)),
                UiText.Res(R.string.designsystem_preview_scans, UiArg.Numeric.Count(1234)),
                UiText.Res(R.string.designsystem_preview_share, UiArg.Numeric.Percent(0.31)),
                UiText.Res(R.string.designsystem_preview_product, UiArg.Text("Peanut butter")),
                UiText.Res(
                    R.string.designsystem_preview_product,
                    UiArg.Nested(UiText.Res(R.string.designsystem_preview_title)),
                ),
                UiText.Plural(R.plurals.designsystem_preview_products, 1, UiArg.Numeric.Count(1)),
                UiText.Plural(R.plurals.designsystem_preview_products, 5, UiArg.Numeric.Count(5)),
                UiText.Raw("Raw product name"),
            ).forEach {
                Text(it.resolve(), style = AppTheme.typography.body)
            }
        }
    }
}
