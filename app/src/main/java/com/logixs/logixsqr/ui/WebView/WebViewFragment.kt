package com.logixs.logixsqr.ui.WebView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.logixs.logixsqr.R


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
        val rootView: View = inflater.inflate(R.layout.fragment_web_view, container, false)

        val url = "https://www.logixs.com.ar/"+ path+"/WebViewMensajero/index?idUsuario="+idUsuario.toString()


        val view = rootView.findViewById<View>(R.id.webview) as WebView
        view.settings.javaScriptEnabled = true
        view.loadUrl(url)
        return rootView
    }
        //return root
    //}
}