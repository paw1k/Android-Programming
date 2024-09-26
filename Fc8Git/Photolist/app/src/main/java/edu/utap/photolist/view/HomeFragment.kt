package edu.utap.photolist.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import edu.utap.photolist.MainActivity
import edu.utap.photolist.MainViewModel
import edu.utap.photolist.R
import edu.utap.photolist.SortColumn
import edu.utap.photolist.databinding.FragmentHomeBinding

class HomeFragment :
    Fragment(R.layout.fragment_home) {
    private val viewModel: MainViewModel by activityViewModels()
    // It is a real bummer that we must initialize a registerForActivityResult
    // here or in onViewCreated.  You CAN'T initialize it in an onClickListener
    // where it could capture state like the file name.
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.pictureSuccess()
        } else {
            viewModel.pictureFailure()
        }
    }

    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder#getBindingAdapterPosition()
    // Getting the position of the selected item is unfortunately complicated
    // This always returns a valid index.
    private fun getPos(holder: RecyclerView.ViewHolder) : Int {
        val pos = holder.bindingAdapterPosition
        // notifyDataSetChanged was called, so position is not known
        if( pos == RecyclerView.NO_POSITION) {
            return holder.absoluteAdapterPosition
        }
        return pos
    }

    // Touch helpers provide functionality like detecting swipes or moving
    // entries in a recycler view.  Here we do swipe left to delete
    private fun initTouchHelper(): ItemTouchHelper {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START)
            {
                override fun onMove(recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder): Boolean {
                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                      direction: Int) {
                    val position = getPos(viewHolder)
                    Log.d(javaClass.simpleName, "Swipe delete $position")
                    viewModel.removePhotoAt(position)
                }
            }
        return ItemTouchHelper(simpleItemTouchCallback)
    }
    // No need for onCreateView because we passed R.layout to Fragment constructor
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        val mainActivity = (requireActivity() as MainActivity)

        // Long press to edit.
        val adapter = PhotoMetaAdapter(viewModel)

        val rv = binding.photosRV
        val itemDecor = DividerItemDecoration(rv.context, LinearLayoutManager.VERTICAL)
        rv.addItemDecoration(itemDecor)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(rv.context)
        // Swipe left to delete
        initTouchHelper().attachToRecyclerView(rv)

        // XXX Write me, onclick listeners and observers
        viewModel.observeSortInfo().observe(viewLifecycleOwner) { sortInfo ->
            mainActivity.progressBarOn()
            binding.rowPictureTitle.setBackgroundColor(Color.TRANSPARENT)
            binding.rowSize.setBackgroundColor(Color.TRANSPARENT)

            val (targetView, color) = when (sortInfo.sortColumn) {
                SortColumn.TITLE -> binding.rowPictureTitle to if (sortInfo.ascending) Color.YELLOW else Color.RED
                SortColumn.SIZE -> binding.rowSize to if (sortInfo.ascending) Color.YELLOW else Color.RED
                else -> null to null
            }

            // Apply the determined color to the target view
            targetView?.setBackgroundColor(color ?: Color.TRANSPARENT)

            viewModel.fetchPhotoMeta { mainActivity.progressBarOff() }
        }

        viewModel.observePhotoMeta().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.rowPictureTitle.setOnClickListener {
            viewModel.sortInfoClick(SortColumn.TITLE) {
            }
        }

        binding.rowSize.setOnClickListener {
            viewModel.sortInfoClick(SortColumn.SIZE) {
            }
        }

        binding.cameraButton.setOnClickListener {
            binding.inputET.text.toString().takeIf { it.isNotBlank() }?.also { title ->
                viewModel.pictureNameByUser = title
                TakePictureWrapper.takePicture(title, requireContext(), viewModel, cameraLauncher)
            } ?: Snackbar.make(view, "You must title picture", Snackbar.LENGTH_SHORT).show()
        }

    }
}