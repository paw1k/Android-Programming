package edu.utap.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.utap.fragment.databinding.FourImageBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FourImageFragment : Fragment() {
    // https://developer.android.com/topic/libraries/view-binding#fragments
    private var _binding: FourImageBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun makeImageClickable(imageView: ImageView, index: Int) {
        imageView.setOnClickListener {
            // XXX Write me using a direction object and findNavController()
            val action = FourImageFragmentDirections.actionFourImageFragmentToOneImageFragment(index)
            findNavController().navigate(action)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FourImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = requireActivity() as MainActivity
        // XXX Write me

        binding.IV0.setImageBitmap(mainActivity.bitMap[0])
        binding.IV1.setImageBitmap(mainActivity.bitMap[1])
        binding.IV2.setImageBitmap(mainActivity.bitMap[2])
        binding.IV3.setImageBitmap(mainActivity.bitMap[3])

        makeImageClickable(binding.IV0, 0)
        makeImageClickable(binding.IV1, 1)
        makeImageClickable(binding.IV2, 2)
        makeImageClickable(binding.IV3, 3)

        // This is a valid use of the visibility tag, to either include
        // or exclude a display element.
        if (MainActivity.showText) {
            binding.TV0.text = "Aye aye"
            binding.TV1.text = "Trees"
            binding.TV2.text = "Red panda"
            binding.TV3.text = "Rorschach"
            // XXX Write me, set visibility of text views

            val visibility = if (MainActivity.showText) View.VISIBLE else View.GONE
            binding.TV0.visibility = visibility
            binding.TV1.visibility = visibility
            binding.TV2.visibility = visibility
            binding.TV3.visibility = visibility

        } else {
            // XXX Write me, set visibility of text views
            // Whether MainActivity.showText is true or false, we always
            // set all of the visibility flags.  Why?

            val visibility = if (MainActivity.showText) View.VISIBLE else View.GONE
            binding.TV0.visibility = visibility
            binding.TV1.visibility = visibility
            binding.TV2.visibility = visibility
            binding.TV3.visibility = visibility

        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}