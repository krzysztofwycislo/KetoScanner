package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.databinding.HomeFragmentBinding
import pl.handsome.club.ketoscanner.util.safeNavigateTo


class HomeFragment : Fragment(R.layout.home_fragment) {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = HomeFragmentBinding.bind(view)

        binding.homeMenuButton.setOnClickListener {
            showMenu(it)
        }
    }

    private fun showMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.home_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            onMenuOptionChoose(it.itemId)
            true
        }
        popup.show()
    }

    private fun onMenuOptionChoose(itemId: Int) {
        when (itemId) {
            R.id.homeMenuDietPreferences ->
                safeNavigateTo(HomeFragmentDirections.toMacronutrientPreferencesFragment())

            R.id.homeMenuAbout ->
                safeNavigateTo(HomeFragmentDirections.toAboutFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}