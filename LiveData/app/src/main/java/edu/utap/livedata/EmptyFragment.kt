package edu.utap.livedata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.utap.livedata.databinding.EmptyLayoutBinding

class EmptyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = EmptyLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }
}