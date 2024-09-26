package edu.utap.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import edu.utap.fragment.databinding.OneImageBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class OneImageFragment : Fragment() {
    // https://developer.android.com/topic/libraries/view-binding#fragments
    private var _binding: OneImageBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    // SafeArgs plugins
    private val args: OneImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OneImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = requireActivity() as MainActivity

        mainActivity.hideBars()
        // Clicking the big image returns us to the four image fragment
        binding.oneImageIV.setOnClickListener {
            // Why don't we do this?
            // val action = OneImageFragmentDirections.actionOneImageFragmentToFourImageFragment()
            // findNavController().navigate(action)
            mainActivity.showBars()
            findNavController().popBackStack()
        }
        // XXX Write me
        with(mainActivity.bitMap) {
            if (args.index in indices) {
                binding.oneImageIV.setImageBitmap(this[args.index])
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}