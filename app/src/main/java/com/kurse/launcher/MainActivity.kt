package com.kurse.launcher

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val purple = Color.rgb(190, 105, 255)
    private val ink = Color.rgb(10, 7, 17)
    private lateinit var root: LinearLayout
    private lateinit var search: EditText
    private lateinit var appGrid: GridLayout
    private lateinit var countLabel: TextView
    private var apps: List<ApplicationInfo> = emptyList()
    private var category = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 6, 13)
        window.navigationBarColor = Color.rgb(8, 6, 13)
        window.decorView.systemUiVisibility = 0
        buildUi()
        loadApps()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(12))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(12, 7, 22), Color.rgb(25, 8, 39), Color.rgb(7, 6, 13)))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        brand.addView(TextView(this).apply { text = "KURSE"; textSize = 25f; letterSpacing = .22f; setTextColor(Color.WHITE); typeface = Typeface.create("sans-serif-black", Typeface.BOLD) })
        brand.addView(TextView(this).apply { text = "VOID SYSTEM  /  PERSONAL LAUNCHER"; textSize = 9f; letterSpacing = .12f; setTextColor(purple) })
        header.addView(brand)
        header.addView(makePill("⚙", 44) { showSettings() })
        root.addView(header)

        val clockBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(10))
        }
        clockBlock.addView(TextView(this).apply {
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            textSize = 48f; typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        })
        clockBlock.addView(TextView(this).apply {
            text = SimpleDateFormat("EEEE  •  MMMM d", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
            textSize = 10f; letterSpacing = .14f; gravity = Gravity.CENTER; setTextColor(0xFFD4B8F2.toInt())
        })
        root.addView(clockBlock)

        search = EditText(this).apply {
            hint = "Search apps…"; setHintTextColor(0xFFB7A8C7.toInt()); setTextColor(Color.WHITE)
            textSize = 14f; singleLine = true; setPadding(dp(16), 0, dp(16), 0)
            background = rounded(0xB81A1027.toInt(), 18, 0xFF633B83.toInt())
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(48)))
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderApps() }
            override fun afterTextChanged(s: Editable?) {}
        })

        val categories = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(12), 0, dp(8)) }
        listOf("All", "Games", "Social", "Tools").forEach { label ->
            categories.addView(makePill(label, 0) { category = label; renderApps() }, LinearLayout.LayoutParams(0, dp(36), 1f).apply { marginEnd = dp(5) })
        }
        root.addView(categories)
        countLabel = TextView(this).apply { text = "YOUR APPS"; textSize = 10f; letterSpacing = .18f; setTextColor(purple); setPadding(dp(2), dp(6), 0, dp(8)) }
        root.addView(countLabel)
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        appGrid = GridLayout(this).apply { columnCount = 4; alignmentMode = GridLayout.ALIGN_BOUNDS; useDefaultMargins = false }
        scroll.addView(appGrid)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(8), dp(8), dp(8), dp(8))
            background = rounded(0xB51A1026.toInt(), 24, 0xFF51316C.toInt())
        }
        listOf("Phone" to "com.google.android.dialer", "Messages" to "com.google.android.apps.messaging", "Camera" to "com.sec.android.app.camera", "Settings" to "com.android.settings").forEach { (name, pkg) ->
            dock.addView(dockItem(name, pkg), LinearLayout.LayoutParams(0, dp(58), 1f))
        }
        root.addView(dock, LinearLayout.LayoutParams(-1, dp(68)).apply { topMargin = dp(10) })
        setContentView(root)
    }

    private fun loadApps() {
        apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app -> packageManager.getLaunchIntentForPackage(app.packageName) != null && app.packageName != packageName }
            .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase(Locale.getDefault()) }
        renderApps()
    }

    private fun renderApps() {
        if (!::appGrid.isInitialized) return
        appGrid.removeAllViews()
        val query = if (::search.isInitialized) search.text.toString().trim().lowercase(Locale.getDefault()) else ""
        val filtered = apps.filter { app ->
            val label = packageManager.getApplicationLabel(app).toString().lowercase(Locale.getDefault())
            val matchesText = label.contains(query)
            val n = label
            val matchesCategory = when (category) {
                "Games" -> (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0 || n.contains("game") || n.contains("play")
                "Social" -> listOf("discord", "instagram", "facebook", "snapchat", "tiktok", "reddit", "x ", "messenger", "whatsapp", "twitch").any { n.contains(it) }
                "Tools" -> listOf("settings", "files", "calculator", "clock", "calendar", "camera", "drive", "maps").any { n.contains(it) }
                else -> true
            }
            matchesText && matchesCategory
        }
        countLabel.text = "${filtered.size} APPS  /  ${category.uppercase(Locale.getDefault())}"
        filtered.forEach { app ->
            val item = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(2), dp(8), dp(2), dp(8)); isClickable = true; isFocusable = true }
            val icon = ImageView(this).apply { setImageDrawable(packageManager.getApplicationIcon(app)); scaleType = ImageView.ScaleType.FIT_CENTER }
            item.addView(icon, LinearLayout.LayoutParams(dp(44), dp(44)))
            item.addView(TextView(this).apply {
                text = packageManager.getApplicationLabel(app).toString(); textSize = 10f; gravity = Gravity.CENTER
                setTextColor(Color.WHITE); maxLines = 2; ellipsize = android.text.TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(-1, dp(30)))
            item.setOnClickListener { launch(app.packageName) }
            item.setOnLongClickListener { showAppOptions(app); true }
            val params = GridLayout.LayoutParams().apply { width = 0; height = dp(88); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); setMargins(dp(1), 0, dp(1), 0) }
            appGrid.addView(item, params)
        }
        if (filtered.isEmpty()) appGrid.addView(TextView(this).apply { text = "No apps found"; setTextColor(0xFFE0D0F0.toInt()); textSize = 14f; setPadding(dp(12), dp(20), 0, 0) })
    }

    private fun launch(pkg: String) {
        try { startActivity(packageManager.getLaunchIntentForPackage(pkg)) }
        catch (_: Exception) { Toast.makeText(this, "Can't open this app", Toast.LENGTH_SHORT).show() }
    }

    private fun dockItem(label: String, pkg: String): View {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; isClickable = true; isFocusable = true }
        val icon = ImageView(this)
        val found = try { packageManager.getApplicationIcon(pkg) } catch (_: Exception) { null }
        if (found != null) icon.setImageDrawable(found) else icon.setImageResource(android.R.drawable.sym_def_app_icon)
        box.addView(icon, LinearLayout.LayoutParams(dp(28), dp(28)))
        box.addView(TextView(this).apply { text = label; textSize = 9f; setTextColor(Color.WHITE); gravity = Gravity.CENTER }, LinearLayout.LayoutParams(-1, dp(18)))
        box.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) startActivity(intent) else Toast.makeText(this, "$label app not found", Toast.LENGTH_SHORT).show()
        }
        return box
    }

    private fun makePill(label: String, width: Int, action: () -> Unit): TextView = TextView(this).apply {
        text = label; gravity = Gravity.CENTER; textSize = if (label == "⚙") 20f else 11f; setTextColor(Color.WHITE)
        background = rounded(0xCC29163B.toInt(), 16, 0xFF6A438A.toInt()); isClickable = true; isFocusable = true; setOnClickListener { action() }
        if (width > 0) layoutParams = LinearLayout.LayoutParams(dp(width), dp(42))
    }

    private fun showSettings() {
        val options = arrayOf("Set Kurse wallpaper", "Android home settings", "Wallpaper & style", "About Kurse Launcher")
        AlertDialog.Builder(this).setTitle("KURSE • CONTROL PANEL").setItems(options) { _, which ->
            when (which) {
                0 -> try { WallpaperManager.getInstance(this).setResource(com.kurse.launcher.R.drawable.kurse_wallpaper); Toast.makeText(this, "Wallpaper applied", Toast.LENGTH_SHORT).show() } catch (_: Exception) { Toast.makeText(this, "Set the included wallpaper from Gallery instead", Toast.LENGTH_LONG).show() }
                1 -> startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                2 -> startActivity(Intent("android.settings.WALLPAPER_SETTINGS"))
                3 -> AlertDialog.Builder(this).setTitle("Kurse Launcher 1.0").setMessage("A dark violet launcher concept for your Galaxy S23. App icons and launches use installed Android apps. This is an early starter build, not a full replacement for every Nova/Smart Launcher feature.").setPositiveButton("OK", null).show()
            }
        }.show()
    }

    private fun showAppOptions(app: ApplicationInfo) {
        val name = packageManager.getApplicationLabel(app).toString()
        AlertDialog.Builder(this).setTitle(name).setItems(arrayOf("Open app", "App info")) { _, which ->
            if (which == 0) launch(app.packageName) else startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}")))
        }.show()
    }

    private fun rounded(color: Int, radius: Int, stroke: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat(); setStroke(dp(1), stroke)
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
