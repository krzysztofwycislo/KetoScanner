package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.databinding.MacronutrientPreferencesFragmentBinding
import pl.handsome.club.ketoscanner.viewmodel.preferences.DietPreferencesViewModel
import pl.handsome.club.ketoscanner.viewmodel.preferences.DietPreferencesViewModel.Companion.MAX_MAX_CARBS_AMOUNT
import pl.handsome.club.ketoscanner.viewmodel.preferences.DietPreferencesViewModel.Companion.MIN_MAX_CARBS_AMOUNT


class MacronutrientPreferencesFragment : Fragment(R.layout.macronutrient_preferences_fragment) {

    private val viewModel: DietPreferencesViewModel by viewModel()

    private var _binding: MacronutrientPreferencesFragmentBinding? = null
    private val binding get() = _binding!!

    // The input is prefilled only once per view, so later emissions never overwrite what the user is typing.
    private var inputPrefilled = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = MacronutrientPreferencesFragmentBinding.bind(view)
        // After a configuration change the EditText restores its own text, so it must not be overwritten.
        inputPrefilled = savedInstanceState != null

        binding.preferencesSaveButton.setOnClickListener { saveMaxCarbsAmount() }
        binding.preferencesMaxCarbsInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveMaxCarbsAmount()
                true
            } else {
                false
            }
        }

        viewModel.getMaxCarbsAmount().observe(viewLifecycleOwner, ::onMaxCarbsAmountChanged)
        viewModel.getInvalidInput().observe(viewLifecycleOwner, ::onInvalidInputChanged)
    }

    private fun onMaxCarbsAmountChanged(amount: Int) {
        binding.preferencesMaxCarbsSummary.text =
            getString(R.string.preferences_max_carb_amount_summary, amount)

        if (!inputPrefilled) {
            inputPrefilled = true
            binding.preferencesMaxCarbsInput.setText(amount.toString())
        }
    }

    private fun onInvalidInputChanged(invalid: Boolean) {
        binding.preferencesMaxCarbsInputLayout.error = if (invalid) {
            getString(R.string.preferences_max_carb_amount_error, MIN_MAX_CARBS_AMOUNT, MAX_MAX_CARBS_AMOUNT)
        } else {
            null
        }
    }

    private fun saveMaxCarbsAmount() {
        viewModel.saveMaxCarbsAmount(binding.preferencesMaxCarbsInput.text?.toString().orEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
