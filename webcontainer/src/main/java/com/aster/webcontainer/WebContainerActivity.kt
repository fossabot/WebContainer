package com.aster.webcontainer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.aster.webcontainer.databinding.ActivityWebContainerBinding
import com.aster.webcontainer.listener.WebContainerBridge
import com.aster.webcontainer.listener.WebContainerListener

/**
 * @author ichsanachmad
 */
internal class WebContainerActivity : AppCompatActivity() {
    private val binding by lazy { ActivityWebContainerBinding.inflate(layoutInflater) }

    private val url by lazy { intent.getStringExtra(EXTRA_WC_URL) ?: "" }

    private val chromeClientProp by lazy {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                setProgressLoadWebView(newProgress)
            }
        }
    }

    private val webViewClientProp by lazy {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())

                return false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupListener()
        setupWebContainer()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebContainer() {
        Log.e(TAG, "UserAgent: ${System.getProperty(USER_AGENT_PROPERTY_KEY)}")
        binding.webContainer.apply {
            webViewClient = webViewClientProp
            webChromeClient = chromeClientProp
            settings.apply {
                useWideViewPort = true
                loadWithOverviewMode = true
                javaScriptEnabled = true
                userAgentString = System.getProperty(USER_AGENT_PROPERTY_KEY)
            }
            loadUrl(this@WebContainerActivity.url)
        }
    }

    private fun setupListener() {
        listener?.let {
            binding.webContainer.addJavascriptInterface(WebContainerBridge(it), CALLBACK_KEY)
        }
    }

    private fun setProgressLoadWebView(progress: Int) {
        binding.apply {
            progressBar.progress = progress
            supportActionBar?.title = when {
                progress < MAX_PROGRESS -> getString(R.string.loading)
                else -> {
                    progressBar.visibility = View.GONE
                    webContainer.title
                }
            }
        }
    }

    companion object {
        private const val TAG = "WebContainer"
        private const val USER_AGENT_PROPERTY_KEY = "http.agent"
        private const val EXTRA_WC_URL = "web_container_url"
        private const val CALLBACK_KEY = "AndroidAppCallback"
        private const val MAX_PROGRESS = 100

        private var listener: WebContainerListener? = null

        fun openWebContainer(context: Context, url: String) {
            Intent(context, WebContainerActivity::class.java).apply {
                putExtra(EXTRA_WC_URL, url)
                context.startActivity(this)
            }
        }

        fun openWebContainerWithListener(
            context: Context,
            url: String,
            listener: WebContainerListener
        ) {
            Intent(context, WebContainerActivity::class.java).apply {
                putExtra(EXTRA_WC_URL, url)
                context.startActivity(this)
            }
            this.listener = listener
        }
    }
}