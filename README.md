# FBOP
see read me
FBOP is a moderately customizable Android launcher (home screen replacement) built for Android 10, optimized for low-RAM "Go edition" devices (armeabi-v7a architecture). It follows Android 10's default design language and icon shape throughout.
Home screen
Multiple swipeable pages with dot page indicators
App grid with user-adjustable columns/rows (set in Settings), fixed icon size, labels under each icon
A fixed row of pinned apps at the bottom, with a user-configurable number of slots
Folders: created by dragging one app onto another, tap to expand in place (not a separate screen), name and icon are user-editable
A special Hidden Apps folder that appears automatically once any app is hidden — nameable, icon-editable via Settings, contents sortable/draggable
Home screen widgets (clock, weather, etc.) placeable directly on pages — not yet built
Optional layout lock to freeze the arrangement
Toggleable status bar visibility
App drawer
Opened via swipe-up gesture from the home screen
Same visual style as home grid, with its own independently adjustable columns/rows
Search bar pinned to the top, adjustable transparency, automatically follows light/dark theme, searches installed app names only (no contacts/web)
Alphabetical sort only
Customization
Per-app: editable label, editable icon (any PNG/JPG/JPEG)
Theme: light/dark toggle only
Long-press menu (on apps or empty home space) is itself configurable — user chooses in Settings whether home options (wallpaper, add widget) and/or app options (uninstall, info, hide, pin, edit) appear
Settings
Reachable via an icon in the drawer/menu and/or long-press on the home screen — both independently toggleable, or hideable entirely so Settings is only reachable via its own app icon
Every grid/row/transparency/pin-count value above is adjustable here
Footer reads: "©2026 @mdmac@orion FTF"
Accessibility
A touch-blocking mode to prevent accidental touches, aimed at accessibility users
Enabled via Android's Accessibility Service (one-time setup in system settings, shows a persistent system indicator while active)
Triggered by a two-finger long-press gesture, a Settings toggle, or (best-effort) triple-tap of the power button
Unlocked by 5 rapid taps on the blocked overlay
