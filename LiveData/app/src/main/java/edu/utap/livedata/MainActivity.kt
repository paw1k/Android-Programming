package edu.utap.livedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import edu.utap.livedata.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val consumerTitle = "Consumer"
        private const val producerTitle = "Producer"
    }
    private fun navigateProducer() {
        // XXX Write me, navigate to produce fragment, pass title
        val action = NavGraphDirections.createProducer(producerTitle)
        findNavController(R.id.containerForProducerFragment).navigate(action)
    }
    private fun navigateConsumer() {
        // XXX Write me, navigate to consume fragment, pass title
        val action = NavGraphDirections.createConsumer(consumerTitle)
        findNavController(R.id.containerForConsumerFragment).navigate(action)
    }
    private fun navigateEmpty() {
        // XXX Write me, navigate to empty fragment
        val action = NavGraphDirections.createEmptyFragment()
        findNavController(R.id.containerForConsumerFragment).navigate(action)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateProducer()
        navigateConsumer()
        binding.killConsumeBut.setOnClickListener {
           navigateEmpty()
        }
        binding.spawnConsumeBut.setOnClickListener {
            navigateConsumer()
        }

    }
}