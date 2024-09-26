package edu.utexas.cs.zhitingz.fcsql

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import edu.utexas.cs.zhitingz.fcsql.databinding.RestaurantItemBinding

class RestaurantItemAdapter(context: Context, c: Cursor, autoRequery: Boolean) :
//https://developer.android.com/reference/kotlin/android/widget/CursorAdapter
        CursorAdapter(context, c, autoRequery) {

    override fun newView(context: Context, cursor: Cursor, viewGroup: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.restaurant_item, viewGroup, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val binding = RestaurantItemBinding.bind(view)
        val restaurantName = binding.restaurantName
        val restaurantPhone = binding.restaurantPhone
        val restaurantAddress = binding.restaurantAddress
        val restaurantUrl = binding.restaurantUrl
        val restaurantPrice = binding.restaurantPrice
        // XXX WRITE ME: Fill the TextView using data in cursor.
        // price will be in the form of "$", e.g. 0 will be "", 1 will be "$" and 2 will be "$$".
        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        val address = cursor.getString(cursor.getColumnIndexOrThrow("full_address"))
        val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
        val url = cursor.getString(cursor.getColumnIndexOrThrow("url"))

        val priceLevels = mapOf("1" to "$", "2" to "$$", "3" to "$$$", "4" to "$$$$")
        val price = priceLevels[cursor.getString(cursor.getColumnIndexOrThrow("price"))] ?: ""

        restaurantName.text = name
        restaurantPhone.text = phone
        restaurantPrice.text = price
        restaurantPhone.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
            v.context.startActivity(intent)
        }
        restaurantAddress.text = address
        restaurantUrl.text = Html.fromHtml("<a href=\"$url\">website</a>", Html.FROM_HTML_MODE_LEGACY)
        restaurantUrl.movementMethod = LinkMovementMethod.getInstance()
    }
}
