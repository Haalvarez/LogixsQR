package com.logixs.logixsqr.ui.home

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import com.logixs.logixsqr.HomeActivity
import com.logixs.logixsqr.R
import com.logixs.logixsqr.ui.qr_entrega.QrCargaFragment
import com.logixs.logixsqr.ui.qr_entrega.QrEntregaFragment
import com.logixs.logixsqr.ui.qr_recibo.QrRetiroFragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        container?.removeAllViews()

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val imgEntregar: ImageButton = root.findViewById(R.id.img_entregar)
        val imgRetirar: ImageButton = root.findViewById(R.id.img_retirar)
        val imgCarga: ImageButton = root.findViewById(R.id.img_carga)

        // Cargo la imagen de entrega
        Picasso.with(requireActivity()).load(R.drawable.qr_entrega)
            .resize(
                resources.getDimension(R.dimen.image_home_size).toInt(),
                resources.getDimension(R.dimen.image_home_size).toInt()
            )
            .into(imgEntregar, object : Callback {
                override fun onSuccess() {
                    val imageBitmap = (imgEntregar.getDrawable() as BitmapDrawable).bitmap
                    val imageDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                    imageDrawable.isCircular = true
                    imageDrawable.cornerRadius =
                        Math.max(imageBitmap.width, imageBitmap.height) / 2f
                    imgEntregar.setImageDrawable(imageDrawable)
                }

                override fun onError() {
                    imgEntregar.setImageDrawable(
                        ContextCompat.getDrawable(requireActivity(), R.drawable.close)
                    )
                }
            })

        // Cargo la imagen de retiro
        Picasso.with(requireActivity()).load(R.drawable.qr_retiro)
            .resize(
                resources.getDimension(R.dimen.image_home_size).toInt(),
                resources.getDimension(R.dimen.image_home_size).toInt()
            )
            .into(imgRetirar, object : Callback {
                override fun onSuccess() {
                    val imageBitmap = (imgRetirar.getDrawable() as BitmapDrawable).bitmap
                    val imageDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                    imageDrawable.isCircular = true
                    imageDrawable.cornerRadius =
                        Math.max(imageBitmap.width, imageBitmap.height) / 2f
                    imgRetirar.setImageDrawable(imageDrawable)
                }

                override fun onError() {
                    imgRetirar.setImageDrawable(
                        ContextCompat.getDrawable(requireActivity(), R.drawable.close)
                    )
                }
            })

        // Cargo la imagen de retiro
        Picasso.with(requireActivity()).load(R.drawable.qr_cargar)
            .resize(
                resources.getDimension(R.dimen.image_home_size).toInt(),
                resources.getDimension(R.dimen.image_home_size).toInt()
            )
            .into(imgCarga, object : Callback {
                override fun onSuccess() {
                    val imageBitmap = (imgCarga.getDrawable() as BitmapDrawable).bitmap
                    val imageDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                    imageDrawable.isCircular = true
                    imageDrawable.cornerRadius =
                        Math.max(imageBitmap.width, imageBitmap.height) / 2f
                    imgCarga.setImageDrawable(imageDrawable)
                }

                override fun onError() {
                    imgCarga.setImageDrawable(
                        ContextCompat.getDrawable(requireActivity(), R.drawable.close)
                    )
                }
            })

        imgEntregar.setOnClickListener {
            val homeActivity = (activity as HomeActivity)
            homeActivity.abrirFragment(QrEntregaFragment(), homeActivity.resources.getString(R.string.menu_qr_entrega))
        }

        imgRetirar.setOnClickListener {
            val homeActivity = (activity as HomeActivity)
            homeActivity.abrirFragment(QrRetiroFragment(), homeActivity.resources.getString(R.string.menu_qr_retiro))
        }

        imgCarga.setOnClickListener {
            val homeActivity = (activity as HomeActivity)
            homeActivity.abrirFragment(QrCargaFragment(), homeActivity.resources.getString(R.string.menu_qr_carga))
        }


        return root
    }
}