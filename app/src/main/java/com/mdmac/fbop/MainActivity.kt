package com.mdmac.fbop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.mdmac.fbop.accessibility.TouchBlockController
import com.mdmac.fbop.data.AppRepository
import com.mdmac.fbop.data.FolderManager
import com.mdmac.fbop.data.PrefsManager
import com.mdmac.fbop.model.AppInfo
import com.mdmac.fbop.model.FolderInfo
import com.mdmac.fbop.model.HomeGridItem
import com.mdmac.fbop.ui.AppAdapter
import com.mdmac.fbop.ui.AppOptionsDialog
import com.mdmac.fbop.ui.FolderOverlayController
import com.mdmac.fbop.ui.HomeOptionsDialog
import com.mdmac.fbop.ui.HomePagerAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var appRepository: AppRepository
    private lateinit var folderManager: FolderManager
    private lateinit var folderOverlayController: FolderOverlayController
    private lateinit var appOptionsDialog: AppOptionsDialog
    private lateinit var homeOptionsDialog: HomeOptionsDialog
    private lateinit var touchBlockController: TouchBlockController

    private lateinit var homeViewPager: ViewPager2
    private lateinit var pageIndicatorContainer: LinearLayout
    private lateinit var pinnedRowRecyclerView: RecyclerView

    private var dotViews: List<ImageView> = emptyList()

    private var pendingIconEditApp: AppInfo? = null
    private val pickIconLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val app = pendingIconEditApp
        if (uri != null && app != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Some providers don't support persistable permissions — icon still works this session
            }
            appOptionsDialog.applyCustomIcon(app, uri)
        }
        pendingIconEditApp = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefsManager = PrefsManager(this)
        appRepository = AppRepository(this)
        folderManager = FolderManager(prefsManager)
        touchBlockController = TouchBlockController(this)

        homeViewPager = findViewById(R.id.homeViewPager)
        pageIndicatorContainer = findViewById(R.id.pageIndicatorContainer)
        pinnedRowRecyclerView = findViewById(R.id.pinnedRowRecyclerView)

        val overlayRoot = findViewById<View>(R.id.folderOverlayRoot)
        val folderTitle = findViewById<TextView>(R.id.folderTitle)
        val folderContentsGrid = findViewById<RecyclerView>(R.id.folderContentsGrid)

        folderOverlayController = FolderOverlayController(
            context = this,
            overlayRoot = overlayRoot,
            folderTitle = folderTitle,
            folderContentsGrid = folderContentsGrid,
            folderManager = folderManager,
            appRepository = appRepository,
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app, _ -> appOptionsDialog.show(app) },
            onFolderChanged = { refreshHomeScreen() }
        )

        appOptionsDialog = AppOptionsDialog(
            context = this,
            prefsManager = prefsManager,
            onChanged = { refreshHomeScreen(); setupPinnedRow() },
            onPickCustomIcon = { app ->
                pendingIconEditApp = app
                pickIconLauncher.launch("image/*")
            }
        )

        homeOptionsDialog = HomeOptionsDialog(this, prefsManager)

        applyStatusBarVisibility()
        refreshHomeScreen()
        setupPinnedRow()
    }

    override fun onResume() {
        super.onResume()
        refreshHomeScreen()
        setupPinnedRow()
    }

    private fun refreshHomeScreen() {
        setupHomePages()
    }

    private fun openDrawer() {
        val intent = Intent(this, DrawerActivity::class.java)
        startActivity(intent)
    }

    private fun applyStatusBarVisibility() {
        if (prefsManager.statusBarVisible) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    private fun buildGridItems(): List<HomeGridItem> {
        val allVisibleApps = appRepository.getVisibleApps()
        val regularFolders = folderManager.getAllFolders().filter { !it.isHiddenAppsFolder }

        val appsInFolders = regularFolders.flatMap { it.appComponentKeys }.toSet()
        val topLevelApps = allVisibleApps.filter { !appsInFolders.contains(it.componentKey) }

        val items = mutableListOf<HomeGridItem>()
        items.addAll(topLevelApps.map { HomeGridItem.AppItem(it) })
        items.addAll(regularFolders.map { HomeGridItem.FolderItem(it) })

        val hiddenComponentKeys = prefsManager.hiddenApps
        if (hiddenComponentKeys.isNotEmpty()) {
            val hiddenFolder = folderManager.getOrCreateHiddenFolder().copy(
                name = prefsManager.hiddenFolderName,
                iconUri = prefsManager.hiddenFolderIconUri,
                appComponentKeys = hiddenComponentKeys.toMutableList()
            )
            items.add(HomeGridItem.FolderItem(hiddenFolder))
        }

        return items
    }

    private fun setupHomePages() {
        if (prefsManager.layoutLocked) return

        val allItems = buildGridItems()
        val columns = prefsManager.homeColumns
        val rows = prefsManager.homeRows
        val perPage = (columns * rows).coerceAtLeast(1)

        val pages: List<MutableList<HomeGridItem>> = if (allItems.isEmpty()) {
            listOf(mutableListOf())
        } else {
            allItems.chunked(perPage).map { it.toMutableList() }
        }

        homeViewPager.adapter = HomePagerAdapter(
            pages = pages,
            columns = columns,
            appLookup = { key -> appRepository.getAppByComponentKey(key) },
            onAppClick = { app -> launchApp(app) },
            onAppOptionsRequested = { app -> appOptionsDialog.show(app) },
            onFolderClick = { folder -> folderOverlayController.open(folder) },
            onFolderOptionsRequested = { folder -> showFolderOptions(folder) },
            onMergeRequested = { _, fromItem, toItem -> handleMergeRequest(fromItem, toItem) },
            onReordered = { _, _ -> /* Manual ordering persistence — future enhancement */ },
            onSwipeUpRequested = {
                if (!folderOverlayController.isOpen()) openDrawer()
            },
            onEmptySpaceLongPress = {
                if (!folderOverlayController.isOpen()) homeOptionsDialog.show()
            },
            onTwoFingerLongPress = {
                if (folderOverlayController.isOpen()) return@HomePagerAdapter
                if (prefsManager.touchBlockGestureEnabled) {
                    if (touchBlockController.isAccessibilityServiceEnabled()) {
                        touchBlockController.startTouchBlock()
                    } else {
                        touchBlockController.openAccessibilitySettings()
                    }
                }
            }
        )

        setupPageIndicator(pages.size)

        homeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateActiveDot(position)
            }
        })
    }

    private fun showFolderOptions(folder: FolderInfo) {
        val options = if (folder.isHiddenAppsFolder) {
            arrayOf("Open")
        } else {
            arrayOf("Open", "Delete folder")
        }

        AlertDialog.Builder(this)
            .setTitle(folder.name)
            .setItems(options) { _, which ->
                when (options[which]) {
                    "Open" -> folderOverlayController.open(folder)
                    "Delete folder" -> {
                        folderManager.deleteFolder(folder.id)
                        refreshHomeScreen()
                    }
                }
            }
            .show()
    }

    private fun handleMergeRequest(fromItem: HomeGridItem, toItem: HomeGridItem) {
        if (toItem is HomeGridItem.FolderItem && toItem.folder.isHiddenAppsFolder) return

        when {
            fromItem is HomeGridItem.AppItem && toItem is HomeGridItem.AppItem -> {
                folderManager.createFolder(
                    firstComponentKey = toItem.app.componentKey,
                    secondComponentKey = fromItem.app.componentKey,
                    pageIndex = 0,
                    position = 0
                )
                refreshHomeScreen()
            }
            fromItem is HomeGridItem.AppItem && toItem is HomeGridItem.FolderItem -> {
                folderManager.addAppToFolder(toItem.folder.id, fromItem.app.componentKey)
                refreshHomeScreen()
            }
            else -> {
                // Folder-onto-app or folder-onto-folder merging not supported
            }
        }
    }

    private fun setupPageIndicator(pageCount: Int) {
        pageIndicatorContainer.removeAllViews()

        if (pageCount <= 1) {
            dotViews = emptyList()
            return
        }

        val dots = mutableListOf<ImageView>()
        for (i in 0 until pageCount) {
            val dot = ImageView(this).apply {
                setImageResource(
                    if (i == 0) R.drawable.page_dot_selected else R.drawable.page_dot_unselected
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 6
                    marginEnd = 6
                    gravity = Gravity.CENTER
                }
            }
            pageIndicatorContainer.addView(dot)
            dots.add(dot)
        }
        dotViews = dots
    }

    private fun updateActiveDot(activeIndex: Int) {
        dotViews.forEachIndexed { index, dot ->
            dot.setImageResource(
                if (index == activeIndex) R.drawable.page_dot_selected else R.drawable.page_dot_unselected
            )
        }
    }

    private fun setupPinnedRow() {
        val allApps = appRepository.getVisibleApps()
        val pinnedKeys = prefsManager.pinnedApps
        val pinnedApps = pinnedKeys.mapNotNull { key -> allApps.find { it.componentKey == key } }

        pinnedRowRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pinnedRowRecyclerView.adapter = AppAdapter(
            context = this,
            apps = pinnedApps,
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app, _ -> appOptionsDialog.show(app) }
        )
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) startActivity(launchIntent)
    }
}
