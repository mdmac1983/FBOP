package com.mdmac.fbop

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.mdmac.fbop.accessibility.TouchBlockController
import com.mdmac.fbop.data.PrefsManager

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var touchBlockController: TouchBlockController

    private val pickIconLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Some providers don't support persistable permissions — icon still works this session
            }
            prefsManager.hiddenFolderIconUri = uri.toString()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "fbop_prefs"
        preferenceManager.sharedPreferencesMode = android.content.Context.MODE_PRIVATE

        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        prefsManager = PrefsManager(requireContext())
        touchBlockController = TouchBlockController(requireContext())

        findPreference<Preference>("hidden_folder_icon")?.setOnPreferenceClickListener {
            pickIconLauncher.launch("image/*")
            true
        }

        setupTouchBlockPreferences()
    }

    override fun onResume() {
        super.onResume()
        updateAccessibilityStatusSummary()
    }

    private fun setupTouchBlockPreferences() {
        val gestureToggle = findPreference<SwitchPreferenceCompat>("touchblock_gesture_enabled")
        val triplePowerToggle = findPreference<SwitchPreferenceCompat>("touchblock_triple_power_enabled")

        val requireAccessibilityListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue == true && !touchBlockController.isAccessibilityServiceEnabled()) {
                touchBlockController.openAccessibilitySettings()
            }
            true
        }

        gestureToggle?.onPreferenceChangeListener = requireAccessibilityListener
        triplePowerToggle?.onPreferenceChangeListener = requireAccessibilityListener

        updateAccessibilityStatusSummary()
    }

    private fun updateAccessibilityStatusSummary() {
        val enabled = touchBlockController.isAccessibilityServiceEnabled()
        val statusText = if (enabled) {
            "FBOP Touch Block service is ON in system Accessibility settings"
        } else {
            "Tap either toggle above to turn on FBOP Touch Block in system Accessibility settings"
        }
        findPreference<Preference>("touchblock_gesture_enabled")?.summary =
            "Use a gesture to temporarily disable touch input\n$statusText"
    }
}
