package com.example.pocketbudget.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pocketbudget.R
import com.example.pocketbudget.data.datastore.UserPreferencesManager
import com.example.pocketbudget.data.json.InitialDataInitializer
import com.example.pocketbudget.data.local.PocketBudgetDatabase
import com.example.pocketbudget.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = PocketBudgetDatabase.getDatabase(this)
        val preferencesManager = UserPreferencesManager(applicationContext)

        lifecycleScope.launch {
            InitialDataInitializer.initializeIfNeeded(applicationContext, database, preferencesManager)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                preferencesManager.themeModeFlow.collectLatest { mode ->
                    val targetNightMode = when (mode) {
                        "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
                        "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    if (AppCompatDelegate.getDefaultNightMode() != targetNightMode) {
                        Log.d(TAG, "Applying theme mode: $mode ($targetNightMode)")
                        AppCompatDelegate.setDefaultNightMode(targetNightMode)
                    }
                }
            }
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
