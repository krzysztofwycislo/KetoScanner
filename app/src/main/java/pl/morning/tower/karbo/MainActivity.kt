package pl.morning.tower.karbo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.ui.AppViewModel
import pl.morning.tower.karbo.ui.KarboApp

private val LightNavigationBarScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkNavigationBarScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { appViewModel.start.value == null }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(LightNavigationBarScrim, DarkNavigationBarScrim),
        )
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                KarboApp()
            }
        }
    }

}
