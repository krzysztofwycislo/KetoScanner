package pl.handsome.club.ketoscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.android.ext.android.inject
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.ketoscanner.ui.KetoScannerApp


class MainActivity : ComponentActivity() {

    private val appEvents: AppEvents by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                KetoScannerApp(appEvents)
            }
        }
    }

}
