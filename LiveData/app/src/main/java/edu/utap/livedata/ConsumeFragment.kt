package edu.utap.livedata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import edu.utap.livedata.databinding.FragmentMainBinding

class ConsumeFragment : Fragment() {
    companion object {
        const val buttonText = "Kill Frag"
    }
    // NB: This is the new way to initialize a viewModel that is shared
    // with the parent activity
    // XXX declare and initialize viewModel and navArgs
    private val viewModel: ViewModel by activityViewModels()
    private val consumeFragment: ConsumeFragmentArgs by navArgs()

    private var _binding: FragmentMainBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // XXX Write me.
        // Deal with view binding, on click listender and observe live data

        with(binding) {
            title.text = consumeFragment.title
            button.text = buttonText

            button.setOnClickListener {
                findNavController().navigate(ConsumeFragmentDirections.createEmptyFragment())
            }

            viewModel.observeData().observe(viewLifecycleOwner) { newData ->
                textView.text = newData.toString()
            }
        }

    }
}
