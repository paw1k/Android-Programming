package dev.almasum.fittrack.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.my.intotal.Utils.IntakeDialog
import dev.almasum.fittrack.adapters.ListedItemAdapter
import dev.almasum.fittrack.consts.Keys
import dev.almasum.fittrack.databinding.ActivityNutritionBinding
import dev.almasum.fittrack.local_db.RoomDB
import dev.almasum.fittrack.local_db.entities.ItemEntity
import dev.almasum.fittrack.models.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NutritionActivity : AppCompatActivity(), IntakeDialog.ItemSaveListener,
    ListedItemAdapter.ItemClickListener {

    private lateinit var binding: ActivityNutritionBinding
    private var _items = mutableListOf<ItemEntity>()
    private lateinit var adapter: ListedItemAdapter

    private var searchLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            var item: Item? = null
            if (data != null) {
                item = data.getSerializableExtra("item") as Item
            }
            var intakeDialog: IntakeDialog = IntakeDialog(item!!, this)
            intakeDialog.show(supportFragmentManager, "intake_dialog")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNutritionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.searchBox.setOnEditorActionListener { v, actionId, event ->
            if (actionId == 3) {
                searchLauncher.launch(Intent(this, SearchResultActivity::class.java).apply {
                    putExtra("search_term", binding.searchBox.text.toString())
                })
                binding.searchBox.clearFocus()
            }
            false
        }

        adapter = ListedItemAdapter(_items, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val today = System.currentTimeMillis()
        val todayMin = today - today % 86400000



        RoomDB.getInstance(this).getItemDao().getTodayItems(todayMin).observe(this) {
            _items.clear()

            for (itemEntity in it) {
                _items.add(
                    itemEntity
                )
            }

            adapter.notifyDataSetChanged()

            var totalEnergy = 0.0
            var totalFat = 0.0
            var totalProtein = 0.0
            var totalSugar = 0.0
            var totalSodium = 0.0
            var totalAmount = 0.0

            for (itemEntity in _items) {
                totalEnergy += itemEntity.energy * itemEntity.amount / 100
                totalFat += itemEntity.fat * itemEntity.amount / 100
                totalProtein += itemEntity.protein * itemEntity.amount / 100
                totalSugar += itemEntity.sugar * itemEntity.amount / 100
                totalSodium += itemEntity.sodium * itemEntity.amount / 100
                totalAmount += itemEntity.amount
            }

            binding.energy.text = Html.fromHtml(
                "<b>Energy:</b> %.2f kcal".format(totalEnergy),
                Html.FROM_HTML_MODE_LEGACY
            )
            binding.fat.text =
                Html.fromHtml("<b>Fat:</b> %.2f g".format(totalFat), Html.FROM_HTML_MODE_LEGACY)
            binding.protein.text = Html.fromHtml(
                "<b>Protein:</b> %.2f g".format(totalProtein),
                Html.FROM_HTML_MODE_LEGACY
            )
            binding.sugar.text =
                Html.fromHtml("<b>Sugar:</b> %.2f g".format(totalSugar), Html.FROM_HTML_MODE_LEGACY)
            binding.sodium.text = Html.fromHtml(
                "<b>Sodium:</b> %.2f mg".format(totalSodium),
                Html.FROM_HTML_MODE_LEGACY
            )

            binding.energyProgress.progress = (totalEnergy / 2000 * 100).toInt()
            binding.fatProgress.progress = (totalFat / 70 * 100).toInt()
            binding.proteinProgress.progress = (totalProtein / 50 * 100).toInt()
            binding.sugarProgress.progress = (totalSugar / 35 * 100).toInt()
            binding.sodiumProgress.progress = (totalSodium / 5 * 100).toInt()


            binding.today.text = Html.fromHtml(
                "<b>Today:</b> %.2f g (Intake)".format(totalAmount),
                Html.FROM_HTML_MODE_LEGACY
            )
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSave(itemEntity: ItemEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            RoomDB.getInstance(this@NutritionActivity).getItemDao().insert(itemEntity)
            saveRemote(itemEntity)
        }
    }

    private fun saveRemote(itemEntity: ItemEntity) {
        val db = FirebaseFirestore.getInstance()
        val userName = getSharedPreferences(Keys.prefName, MODE_PRIVATE).getString(Keys.userName, "")
        db.collection("users").document(userName!!).collection("intake").add(itemEntity)
    }

    override fun onItemClick(item: ItemEntity) {

    }


    override fun finish() {
        super.finish()
        val view = window.decorView as ViewGroup
        view.removeAllViews()
    }
}