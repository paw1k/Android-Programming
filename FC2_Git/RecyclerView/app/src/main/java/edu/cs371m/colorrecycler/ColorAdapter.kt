package edu.cs371m.colorrecycler

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import edu.cs371m.colorrecycler.databinding.ColorCardBinding
import java.util.Locale
import kotlin.random.Random


/**
 * Created by witchel on 1/29/18.  Subsequently modified.
 */

class ColorAdapter(private val colorList: List<ColorList.ColorName>,
                   private val onClickListener: ColorAdapter.(Int) -> Unit)
    : RecyclerView.Adapter<ColorAdapter.VH>() {
    private var random = Random(System.currentTimeMillis())
    // Create a new, writable list that we initialize with colorList
    private var list = mutableListOf<ColorList.ColorName>().apply {
        addAll(colorList.shuffled())
    }

    // ViewHolder pattern makes list rendering more efficient by associating
    // a binding with each list element for direct access
    inner class VH(val binding: ColorCardBinding)
        : RecyclerView.ViewHolder(binding.root)

    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder#getBindingAdapterPosition()
    // Getting the position of the selected item is unfortunately complicated
    // This always returns a valid index.
    // witchel 2/4/24, I think this can return NO_POSITION, but what do we do then? Early return?
    private fun getPos(holder: VH) : Int {
        val pos = holder.bindingAdapterPosition
        // notifyDataSetChanged was called, so position is not known
        if( pos == RecyclerView.NO_POSITION) {
            return holder.absoluteAdapterPosition
        }
        return pos
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ColorCardBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)

        val holder = VH(binding).apply {
            itemView.setOnClickListener {
                val position = getPos(this)
                if (position != RecyclerView.NO_POSITION) {
                    onClickListener(position)
                }
            }
        }

        return holder
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val binding = holder.binding

        val item = list[position]
        val luminance = getLuminance(item.color)
        binding.TV.text = "${item.name} ${String.format(Locale.getDefault(), "%.2f", luminance)}"
        binding.TV.setBackgroundColor(item.color)
        binding.TV.setTextColor(if (luminance < 0.3) Color.WHITE else Color.BLACK)

    }

    override fun getItemCount(): Int {
        return ColorList.size()
    }

    fun moveToTop(position: Int) {
        if (position > 0) {
            val item = list.removeAt(position)
            list.add(0, item)
            notifyItemMoved(position, 0)

            notifyItemRangeChanged(0, position + 1)
        }
    }
    fun swapItem(position: Int) {
        if (list.size <= 1) return
        var index = random.nextInt(list.size)
        while (index == position) {
            index = random.nextInt(list.size)
        }
        val temp = list[position]
        list[position] = list[index]
        list[index] = temp
        notifyItemChanged(position)
        notifyItemChanged(index)
    }

    // A static function for computing luminance
    companion object {
        fun getLuminance(color: Int): Float {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val hsl = FloatArray(3)
            ColorUtils.RGBToHSL(red, green, blue, hsl)
            return hsl[2]
        }
    }
}
