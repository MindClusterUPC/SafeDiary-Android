package com.mindcluster.safediary.payments.presentation.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun StripeCheckoutWebViewDialog(
    checkoutUrl: String,
    onSuccess: () -> Unit,
    onCancelled: () -> Unit,
    onDismissRequest: () -> Unit,
    onError: ((String) -> Unit)? = null
) {
    var isLoading by remember { mutableStateOf(true) }
    var callbackFired by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar with Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stripe Secure Checkout",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryNavy
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryNavy
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true && !callbackFired) {
                                            callbackFired = true
                                            isLoading = false
                                            onError?.invoke("No se pudo cargar la pasarela de pago de Stripe. Verifica tu conexión o salta el pago por esta vez.")
                                                ?: onCancelled()
                                        }
                                    }

                                    override fun onReceivedHttpError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        errorResponse: WebResourceResponse?
                                    ) {
                                        if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 0) >= 400 && !callbackFired) {
                                            callbackFired = true
                                            isLoading = false
                                            onError?.invoke("Stripe devolvió un error (${errorResponse?.statusCode}). Puedes reintentar o saltar el pago por esta vez.")
                                                ?: onCancelled()
                                        }
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val url = request?.url?.toString().orEmpty()
                                        if (url.contains("/payments/subscriptions/success") || url.contains("session_id=")) {
                                            if (!callbackFired) {
                                                callbackFired = true
                                                onSuccess()
                                            }
                                            return true
                                        }
                                        if (url.contains("/payments/subscriptions/cancel")) {
                                            if (!callbackFired) {
                                                callbackFired = true
                                                onCancelled()
                                            }
                                            return true
                                        }
                                        return false
                                    }
                                }
                                loadUrl(checkoutUrl)
                            }
                        }
                    )
                }
            }
        }
    }
}
