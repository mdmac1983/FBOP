package com.mdmac.fbop

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.fbop.data.AppRepository
import com.mdmac.fbop.data.PrefsManager
import com.mdmac.fbop.model.AppInfo
import com.mdmac.fbop.ui.AppAdapter

class DrawerActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var appRepository: AppRepository

    private lateinit var searchBar: EditText
    private lateinit var drawerGrid: RecyclerView
    private lateinit var adapter: AppAdapter

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)

        prefsManager = PrefsManager(this)
        appRepository = AppRepository(this)

        searchBar = findViewById(R.id.drawerSearchBar)
        drawerGrid = findViewById(R.id.drawerGridRecyclerView)

        applySearchBarAlpha()
        setupGrid()
        setupSearch()
    }

    private fun applySearchBarAlpha() {
        val background = searchBar.background
        if (background is GradientDrawable) {
            background.mutate()
            background.alpha = prefsManager.searchBarAlpha.coerceIn(0, 255)
        }
    }

    private fun setupGrid() {
        allApps = appRepository.getVisibleApps()

        val columns = prefsManager.drawerColumns
        drawerGrid.layoutManager = GridLayoutManager(this, columns)

        adapter = AppAdapter(
            context = this,
            apps = allApps,
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { _, _ -> /* Long-press menu not used in drawer for now */ }
        )
        drawerGrid.adapter = adapter
    }

    private fun setupSearch() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) {
                    allApps
                } else {
                    allApps.filter { it.displayLabel.lowercase().contains(query) }
                }
                adapter.updateApps(filtered)
            }
        })
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
            finish()
        }
    }
}
