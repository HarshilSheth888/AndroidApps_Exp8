package com.example.menusdemo

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menusdemo.databinding.ActivityMainBinding
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isDesktopMode = false
    private var targetContextUrl: String = DEFAULT_URL

    companion object {
        private const val DEFAULT_URL = "https://www.google.com"
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdgeInsets()
        setupWebView()
        setupNavigationControls()
        setupToolbarOptionsMenu()
        setupQuickBookmarks()
        setupBackNavigation()

        // Load default website on startup
        loadInputUrl(DEFAULT_URL)
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = binding.webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressIndicator.visibility = View.VISIBLE
                binding.layoutError.visibility = View.GONE
                url?.let {
                    if (!binding.etUrl.hasFocus()) {
                        binding.etUrl.setText(it)
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressIndicator.visibility = View.GONE
                url?.let {
                    if (!binding.etUrl.hasFocus()) {
                        binding.etUrl.setText(it)
                    }
                }
                updateNavButtonsState()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    binding.progressIndicator.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                }
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressIndicator.progress = newProgress
                if (newProgress == 100) {
                    binding.progressIndicator.visibility = View.GONE
                } else {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!title.isNullOrBlank()) {
                    binding.toolbar.subtitle = title
                } else {
                    binding.toolbar.subtitle = getString(R.string.app_subtitle)
                }
            }
        }

        // Register Context Menu for WebView & Long Press Link Actions
        registerForContextMenu(binding.webView)
    }

    private fun setupNavigationControls() {
        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) binding.webView.goBack()
        }

        binding.btnForward.setOnClickListener {
            if (binding.webView.canGoForward()) binding.webView.goForward()
        }

        binding.btnRefresh.setOnClickListener {
            binding.webView.reload()
        }

        binding.btnClearUrl.setOnClickListener {
            binding.etUrl.setText("")
        }

        binding.btnGo.setOnClickListener {
            hideKeyboard()
            loadInputUrl(binding.etUrl.text.toString())
        }

        binding.btnRetry.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            binding.webView.reload()
        }

        binding.etUrl.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.btnClearUrl.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                }

                override fun afterTextChanged(s: Editable?) {}
            }
        )

        binding.etUrl.setOnEditorActionListener { _, actionId, _ ->
            if ((actionId == EditorInfo.IME_ACTION_GO) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                hideKeyboard()
                loadInputUrl(binding.etUrl.text.toString())
                true
            } else {
                false
            }
        }

        binding.btnPopupMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun setupToolbarOptionsMenu() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // Quick Websites Submenu
                R.id.site_google -> loadInputUrl("https://www.google.com")
                R.id.site_wikipedia -> loadInputUrl("https://www.wikipedia.org")
                R.id.site_android_dev -> loadInputUrl("https://developer.android.com")
                R.id.site_kotlin -> loadInputUrl("https://kotlinlang.org")
                R.id.site_github -> loadInputUrl("https://github.com")
                R.id.site_news -> loadInputUrl("https://news.ycombinator.com")

                // Color Themes Submenu
                R.id.theme_sunset -> {
                    menuItem.isChecked = true
                    applyColorTheme(
                        R.drawable.bg_toolbar_sunset,
                        R.color.sunset_primary,
                        R.color.white,
                    )
                }

                R.id.theme_ocean -> {
                    menuItem.isChecked = true
                    applyColorTheme(
                        R.drawable.bg_toolbar_ocean,
                        R.color.ocean_primary,
                        R.color.ocean_accent,
                    )
                }

                R.id.theme_neon -> {
                    menuItem.isChecked = true
                    applyColorTheme(
                        R.drawable.bg_toolbar_neon,
                        R.color.neon_primary,
                        R.color.neon_accent,
                    )
                }

                R.id.theme_mint -> {
                    menuItem.isChecked = true
                    applyColorTheme(
                        R.drawable.bg_toolbar_mint,
                        R.color.mint_primary,
                        R.color.white,
                    )
                }

                R.id.theme_berry -> {
                    menuItem.isChecked = true
                    applyColorTheme(
                        R.drawable.bg_toolbar_berry,
                        R.color.berry_primary,
                        R.color.berry_accent,
                    )
                }

                // Page Actions
                R.id.action_reload -> binding.webView.reload()
                R.id.action_desktop_view -> toggleDesktopMode()
                R.id.action_copy_url -> copyCurrentUrl()
                R.id.action_share_url -> shareCurrentUrl()
                R.id.action_add_bookmark -> {
                    Toast.makeText(this, getString(R.string.toast_bookmark_added), Toast.LENGTH_SHORT).show()
                }

                // More Actions
                R.id.action_clear_cache -> {
                    binding.webView.clearCache(true)
                    Toast.makeText(this, getString(R.string.toast_cache_cleared), Toast.LENGTH_SHORT).show()
                }

                R.id.action_about -> showAboutDialog()
                else -> {}
            }
            true
        }
    }

    private fun setupQuickBookmarks() {
        val bookmarkMap = mapOf(
            binding.chipGoogle to "https://www.google.com",
            binding.chipWiki to "https://www.wikipedia.org",
            binding.chipAndroid to "https://developer.android.com",
            binding.chipGithub to "https://github.com",
            binding.chipKotlin to "https://kotlinlang.org",
            binding.chipNews to "https://news.ycombinator.com",
        )

        for ((chip, url) in bookmarkMap) {
            chip.setOnClickListener {
                loadInputUrl(url)
            }
            chip.setOnLongClickListener { view ->
                targetContextUrl = url
                registerForContextMenu(view)
                openContextMenu(view)
                true
            }
        }
    }

    private fun showPopupMenu(anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.popup_actions_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.popup_home -> loadInputUrl(DEFAULT_URL)
                R.id.popup_reload -> binding.webView.reload()
                R.id.popup_desktop -> toggleDesktopMode()
                R.id.popup_share -> shareCurrentUrl()
                R.id.popup_clear_history -> {
                    binding.webView.clearHistory()
                    Toast.makeText(this, "Search history cleared!", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.show()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.quick_links_context_menu, menu)
        menu?.setHeaderTitle("Link Options")
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val targetUrl = if (binding.webView.url.isNullOrBlank()) targetContextUrl else (binding.webView.url ?: targetContextUrl)
        return when (item.itemId) {
            R.id.ctx_open -> {
                loadInputUrl(targetUrl)
                true
            }

            R.id.ctx_copy -> {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("URL", targetUrl))
                Toast.makeText(this, getString(R.string.toast_url_copied), Toast.LENGTH_SHORT).show()
                true
            }

            R.id.ctx_share -> {
                shareUrlString(targetUrl)
                true
            }

            R.id.ctx_delete -> {
                Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    private fun applyColorTheme(toolbarBgResId: Int, primaryColorResId: Int, indicatorColorResId: Int) {
        binding.toolbar.setBackgroundResource(toolbarBgResId)
        val colorPrimary = ContextCompat.getColor(this, primaryColorResId)
        val colorIndicator = ContextCompat.getColor(this, indicatorColorResId)

        binding.btnGo.backgroundTintList = ColorStateList.valueOf(colorPrimary)
        binding.progressIndicator.setIndicatorColor(colorIndicator)
        binding.btnRetry.backgroundTintList = ColorStateList.valueOf(colorPrimary)
    }

    private fun toggleDesktopMode() {
        isDesktopMode = !isDesktopMode
        val settings = binding.webView.settings
        if (isDesktopMode) {
            settings.userAgentString = DESKTOP_USER_AGENT
            Toast.makeText(this, "Desktop Site Enabled", Toast.LENGTH_SHORT).show()
        } else {
            settings.userAgentString = null
            Toast.makeText(this, "Mobile Site Enabled", Toast.LENGTH_SHORT).show()
        }
        binding.webView.reload()
    }

    private fun loadInputUrl(input: String) {
        var formattedUrl = input.trim()
        if (formattedUrl.isEmpty()) {
            formattedUrl = DEFAULT_URL
        } else if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = if (formattedUrl.contains(".") && !formattedUrl.contains(" ")) {
                "https://$formattedUrl"
            } else {
                "https://www.google.com/search?q=" + URLEncoder.encode(formattedUrl, "UTF-8")
            }
        }
        binding.etUrl.setText(formattedUrl)
        binding.webView.loadUrl(formattedUrl)
    }

    private fun updateNavButtonsState() {
        binding.btnBack.alpha = if (binding.webView.canGoBack()) 1.0f else 0.4f
        binding.btnForward.alpha = if (binding.webView.canGoForward()) 1.0f else 0.4f
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun copyCurrentUrl() {
        val currentUrl = binding.webView.url ?: binding.etUrl.text.toString()
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Web Page URL", currentUrl))
        Toast.makeText(this, getString(R.string.toast_url_copied), Toast.LENGTH_SHORT).show()
    }

    private fun shareCurrentUrl() {
        val currentUrl = binding.webView.url ?: binding.etUrl.text.toString()
        shareUrlString(currentUrl)
    }

    private fun shareUrlString(url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, binding.webView.title ?: "Web Link")
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Web Page"))
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about_dialog_title)
            .setMessage(R.string.about_dialog_message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    remove()
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken ?: binding.webView.windowToken, 0)
        binding.etUrl.clearFocus()
    }
}
