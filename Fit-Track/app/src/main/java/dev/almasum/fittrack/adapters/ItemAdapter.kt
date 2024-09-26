package dev.almasum.fittrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.almasum.fittrack.R
import dev.almasum.fittrack.databinding.ItemAdapterLayoutBinding
import dev.almasum.fittrack.models.Item

class ItemAdapter(private var items: MutableList<Item>,private var listener: ItemClickListener) : RecyclerView.Adapter<ItemAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemAdapterLayoutBinding = ItemAdapterLayoutBinding.bind(itemView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowPostBinding =
            ItemAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        return VH(rowPostBinding.root)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        Picasso.get().load(items[position].imageUrl).placeholder(R.drawable.placeholder).into(holder.binding.image)
        holder.binding.title.text = items[position].name
        holder.binding.subtitle.text = items[position].group.replace("en:","")

        holder.itemView.setOnClickListener {
            listener.onItemClick(items[position])
        }
    }

    interface ItemClickListener {
        fun onItemClick(item: Item)
    }

}

