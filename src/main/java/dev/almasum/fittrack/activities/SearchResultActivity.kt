package dev.almasum.fittrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import dev.almasum.fittrack.adapters.ItemAdapter
import dev.almasum.fittrack.databinding.ActivitySearchResultBinding
import dev.almasum.fittrack.models.Item
import dev.almasum.fittrack.network.WebService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchResultActivity : AppCompatActivity(), ItemAdapter.ItemClickListener {

    private lateinit var binding: ActivitySearchResultBinding
    private var _items = mutableListOf<Item>()
    private lateinit var adapter: ItemAdapter

    private var searching: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val name = intent.getStringExtra("search_term")
        binding.searchBox.setText(name)

        adapter = ItemAdapter(_items,this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter


        searching.observe(this) {
            if (it) {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.recyclerView.visibility = android.view.View.GONE
            } else {
                binding.progressBar.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.VISIBLE
            }
        }

        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == 3) {
                searchItems(binding.searchBox.text.toString())
                binding.searchBox.clearFocus()
            }
            false
        }

        searchItems(name!!)
    }

    private fun searchItems(searchTerm: String) {
        searching.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val response = WebService.create().getItems(searchTerm)

            try {
                val items = response.asJsonObject["products"].asJsonArray
                _items.clear()
                for (item in items) {
                    val product = item.asJsonObject
                    val code = product["code"].asString
                    val name = product["product_name"].asString
                    var imageUrl = ""
                    try {
                        imageUrl = product["image_url"].asString
                    } catch (e: Exception) {
                        imageUrl =
                            "https://cpworldgroup.com/wp-content/uploads/2021/01/placeholder-1024x683.png"
                    }
                    val group = product["food_groups"].asString
                    val energy = product["nutriments"].asJsonObject["energy-kcal_100g"].asDouble
                    val fat = product["nutriments"].asJsonObject["fat_100g"].asDouble
                    val protein = product["nutriments"].asJsonObject["proteins_100g"].asDouble
                    val sugar = product["nutriments"].asJsonObject["sugars_100g"].asDouble
                    val sodium = product["nutriments"].asJsonObject["sodium_100g"].asDouble
                    val _item =
                        Item(code, name, imageUrl, group, energy, fat, protein, sugar, sodium)
                    println(_item)

                    _items.add(_item)
                }
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            searching.postValue(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(item: Item) {
        val intent = Intent()
        intent.putExtra("item", item)
        setResult(RESULT_OK, intent)
        finish()
    }
}