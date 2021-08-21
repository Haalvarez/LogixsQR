package com.logixs.logixsqr.ui.WebView

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.logixs.logixsqr.R
import com.logixs.logixsqr.R.layout
import kotlinx.android.synthetic.main.fragment_web_view.*


class WebViewFragment(pathUsuario: String?, idUsuario: String?) : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
val path=pathUsuario.toString();
val idUsuario=idUsuario.toString();

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(layout.fragment_web_view, container, false)

        val url = "https://www.logixs.com.ar/"+ path+"/WebViewMensajero/index?idUsuario="+idUsuario.toString()

        val Wview = rootView.findViewById<View>(R.id.webview) as WebView
//refresh
       // swipeRefresh.setOnRefreshListener {
       //     Wview.reload()
       // }

        //Wview.webChromeClient = object : WebChromeClient() {        }

        Wview.webViewClient = object : WebViewClient() {

           override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
               return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                swipeRefresh.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
               super.onPageFinished(view, url)

                swipeRefresh.isRefreshing = false
            }

        }
        Wview.settings.javaScriptEnabled = true
        Wview.loadUrl(url)





        return rootView



    }
        //return root
    //}
}