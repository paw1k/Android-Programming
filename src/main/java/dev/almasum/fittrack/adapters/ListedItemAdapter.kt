package dev.almasum.fittrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.almasum.fittrack.R
import dev.almasum.fittrack.databinding.ItemAdapterLayoutBinding
import dev.almasum.fittrack.databinding.ListedItemAdapterLayoutBinding
import dev.almasum.fittrack.local_db.entities.ItemEntity
import dev.almasum.fittrack.models.Item
import java.text.SimpleDateFormat
import java.util.Locale

class ListedItemAdapter(private var items: MutableList<ItemEntity>, private var listener: ItemClickListener) : RecyclerView.Adapter<ListedItemAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ListedItemAdapterLayoutBinding = ListedItemAdapterLayoutBinding.bind(itemView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowPostBinding =
            ListedItemAdapterLayoutBinding.inflate(
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
        SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault()).format(items[position].timestamp).also {
            holder.binding.time.text = it
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(items[position])
        }
    }

    interface ItemClickListener {
        fun onItemClick(item: ItemEntity)
    }

}

