package edu.utap.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import edu.utap.fragment.databinding.SettingsBinding

class SettingsFragment : Fragment() {
    // https://developer.android.com/topic/libraries/view-binding#fragments
    private var _binding: SettingsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun initSpinner(spinner: Spinner) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        val mainActivity = requireActivity() as MainActivity
        ArrayAdapter(
            mainActivity,
            R.layout.spinner,
            // Having a spinner for False and True is non-standard, but I wanted
            // to throw in a custom spinner because they are useful to apps
            // and unfortunately clunky in Android
            arrayOf("False", "True")
        ).also { adapter ->
            // Set adapter
            spinner.adapter = adapter
            // Set the currently https://developer.android.com/guide/navigation/use-graph/safe-args#example-safeargselected option
            spinner.setSelection(if(MainActivity.showText){ 1 } else { 0 })
            // Because we need to define multiple callbacks, we must allocate an
            // object here, rather than just provide a lambda, which would be more
            // idiomatic in Kotlin.
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                // Spinner callbacks
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    // XXX Write me (one liner)
                    MainActivity.showText = position == 1
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                  MainActivity.showText = false
                }
            }
        }
    }
    private fun initMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Clear menu because we don't want settings icon
                menu.clear()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Do nothing when settings is clicked
                return false
            }
        }, viewLifecycleOwner)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSpinner(binding.permuteSpinner)
        initMenu()
        // XXX Write me.  Deal with immersiveSwitch see setOnCheckedChangeListener
        binding.immersiveSwitch.apply {
            isChecked = MainActivity.immersiveMode
            setOnCheckedChangeListener { _, isChecked ->
                MainActivity.immersiveMode = isChecked
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}