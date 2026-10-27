package pl.handsome.club.ketoscanner

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.text.uiTextResolver
import pl.handsome.club.ketoscanner.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val appEvents: AppEvents by inject()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appEvents.events.collect(::onAppEvent)
            }
        }

        // bottom inset is handled by BottomNavigationView itself
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val navController =
            (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
    }

    private fun onAppEvent(event: AppEvent) {
        when (event) {
            is AppEvent.ShowMessage ->
                Snackbar.make(binding.root, uiTextResolver(this).resolve(event.text), Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomNavigation)
                    .show()

            AppEvent.HideKeyboard -> {
                WindowCompat.getInsetsController(window, binding.root).hide(WindowInsetsCompat.Type.ime())
                currentFocus?.clearFocus()
            }
        }
    }

}
