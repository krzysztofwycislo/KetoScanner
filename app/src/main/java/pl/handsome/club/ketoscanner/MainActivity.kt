package pl.handsome.club.ketoscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.ketoscanner.ui.KetoScannerApp


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                KetoScannerApp()
            }
        }
    }

}
