package com.logixs.logixsqr.ui.perfil

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.logixs.logixsqr.R
import com.logixs.logixsqr.SharedPref
import kotlinx.android.synthetic.main.app_bar_main.*

class PerfilFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        container?.removeAllViews()

        val root = inflater.inflate(R.layout.fragment_perfil, container, false)

        val txNombre: TextView = root.findViewById(R.id.tx_nombre)
        val txApellido: TextView = root.findViewById(R.id.tx_apellido)
        val txIdUsuario: TextView = root.findViewById(R.id.tx_id_usuario)
        val txApodo: TextView = root.findViewById(R.id.tx_apodo)
        val txEmpresa: TextView = root.findViewById(R.id.tx_empresa)
        val txPerfil: TextView = root.findViewById(R.id.tx_perfil)

        txNombre.text = SharedPref.getNombreUsuario(requireContext())
        txApellido.text = SharedPref.getApellidoUsuario(requireContext())
        txIdUsuario.text = SharedPref.getIdUsuario(requireContext())
        txApodo.text = SharedPref.getApodoUsuario(requireContext())
        txEmpresa.text = SharedPref.getEmpresaUsuario(requireContext())
        txPerfil.text = SharedPref.getPerfilUsuario(requireContext())

        return root
    }
}