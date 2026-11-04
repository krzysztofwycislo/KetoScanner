package pl.handsome.club.ketoscanner

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.ketoscanner.ui.KetoScannerApp

private val LightNavigationBarScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkNavigationBarScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(LightNavigationBarScrim, DarkNavigationBarScrim),
        )
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                KetoScannerApp()
            }
        }
    }

}
