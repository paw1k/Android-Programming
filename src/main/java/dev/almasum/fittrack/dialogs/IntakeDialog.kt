package com.my.intotal.Utils

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.squareup.picasso.Picasso
import dev.almasum.fittrack.R
import dev.almasum.fittrack.databinding.IntakeDialogLayoutBinding
import dev.almasum.fittrack.local_db.entities.ItemEntity
import dev.almasum.fittrack.models.Item

class IntakeDialog(private val item: Item, private var listener: ItemSaveListener) :
    AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(
            requireActivity(), R.style.Dialog
        )

        val binding: IntakeDialogLayoutBinding = IntakeDialogLayoutBinding.inflate(layoutInflater)
        builder.setView(binding.root)


        Picasso.get().load(item.imageUrl).placeholder(R.drawable.placeholder).into(binding.image)

        binding.title.text = item.name

        var description = ""
        description += "<b>Group:</b> ${item.group.replace("en:", "")}<br>"
        description += "<b>Energy:</b> ${item.energy} kcal/100g<br>"
        description += "<b>Fat:</b> ${item.fat} g/100g<br>"
        description += "<b>Protein:</b> ${item.protein} g/100g<br>"
        description += "<b>Sugar:</b> ${item.sugar} g/100g<br>"
        description += "<b>Sodium:</b> ${item.sodium} g/100g<br>"

        binding.description.text = Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)

        binding.save.setOnClickListener {
            val amount: Double
            try {
                amount = binding.amount.text.toString().toDouble()
            } catch (e: NumberFormatException) {
                binding.amount.error = "Invalid amount"
                return@setOnClickListener
            }
            val itemEntity = ItemEntity(
                System.currentTimeMillis(),
                item.code,
                item.name,
                item.imageUrl,
                item.group,
                item.energy,
                item.fat,
                item.protein,
                item.sugar,
                item.sodium,
                amount
            )
            listener.onSave(itemEntity)
            dismiss()
        }

        return builder.create()
    }


    interface ItemSaveListener {
        fun onSave(itemEntity: ItemEntity)
    }
}
