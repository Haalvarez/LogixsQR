package com.logixs.logixsqr

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.logixs.logixsqr.ui.home.HomeFragment
import com.logixs.logixsqr.ui.perfil.PerfilFragment
import com.logixs.logixsqr.ui.qr_entrega.QrCargaFragment
import com.logixs.logixsqr.ui.qr_entrega.QrEntregaFragment
import com.logixs.logixsqr.ui.qr_recibo.QrRetiroFragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navTitulo: CharSequence
    private lateinit var drawerLayout: DrawerLayout
    private var fragmentHome = true;

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_inicio, R.id.nav_perfil, R.id.nav_qr_entrega, R.id.nav_qr_retiro
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configuro el textview del navigator drawer
        navTitulo = getString(R.string.nav_header_title, SharedPref.getNombreUsuario(this))
        navView.getHeaderView(0).tx_nombre.setText(navTitulo)
        navView.setNavigationItemSelectedListener(this)
        navView.itemIconTintList = null;

        val url =
            "https://www.logixs.com.ar/" + SharedPref.getPathUsuario(this) + "/images/logo.jpeg"

        // Cargo la imagen en el navigation drawer
        Picasso.with(this).load(url)
            .resize(
                resources.getDimension(R.dimen.nav_header_image_size).toInt(),
                resources.getDimension(R.dimen.nav_header_image_size).toInt()
            )
            .into(navView.getHeaderView(0).img_persona, object : Callback {
                override fun onSuccess() {
                    val imageBitmap =
                        (navView.getHeaderView(0).img_persona.getDrawable() as BitmapDrawable).bitmap
                    val imageDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                    imageDrawable.isCircular = true
                    imageDrawable.cornerRadius =
                        Math.max(imageBitmap.width, imageBitmap.height) / 2f
                    navView.getHeaderView(0).img_persona.setImageDrawable(imageDrawable)
                }

                override fun onError() {
                    navView.getHeaderView(0).img_persona.setImageDrawable(
                        ContextCompat.getDrawable(this@HomeActivity, R.drawable.close)
                    )
                }
            })

        // Configuro el textview de la toolbar y deshabilito el titulo
        txv_toolbar.text = navTitulo
        supportActionBar?.setDisplayShowTitleEnabled(false);

        // Cargo la imagen en la toolbar
        Picasso.with(this).load(url)
            .resize(
                resources.getDimension(R.dimen.toolbar_image_size).toInt(),
                resources.getDimension(R.dimen.toolbar_image_size).toInt()
            )
            .into(img_toolbar, object : Callback {
                override fun onSuccess() {
                    val imageBitmap = (img_toolbar.getDrawable() as BitmapDrawable).bitmap
                    val imageDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                    imageDrawable.isCircular = true
                    imageDrawable.cornerRadius =
                        Math.max(imageBitmap.width, imageBitmap.height) / 2f
                    img_toolbar.setImageDrawable(imageDrawable)
                }

                override fun onError() {
                    img_toolbar.setImageDrawable(
                        ContextCompat.getDrawable(this@HomeActivity, R.drawable.close)
                    )
                }
            })

        // Cuando presiono sobre la imagen de la toolbar abro el fragment perfil
        img_toolbar.setOnClickListener {
            abrirFragment(PerfilFragment(), resources.getString(R.string.menu_mi_perfil))
        }

    }

    fun abrirFragment(fragment: Fragment, title: CharSequence): Boolean {

        // Cambio al fragment recibido
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()

        // Seteo el titulo
        supportActionBar?.setTitle(title)
        txv_toolbar.text = title

        // Guardo en un flag si el fragment actual es el home, para manejar el onBackPressed
        fragmentHome = (fragment is HomeFragment)

        return true
    }

    private fun cerrarSesion(context: Context): Boolean {

        // Borro todas las preferencias del usuario
        SharedPref.borrarTodo(context)

        // Inicio la activity de Login
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)

        return true
    }

    private fun abrirAyuda(): Boolean {

        // Inicio la web de ayuda
        val url = Configuracion.URL_AYUDA
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_inicio -> {
                abrirFragment(HomeFragment(), navTitulo)
            }
            R.id.nav_qr_entrega -> {
                abrirFragment(QrEntregaFragment(), item.title)
            }
            R.id.nav_qr_carga -> {
                abrirFragment(QrCargaFragment(), item.title)
            }
            R.id.nav_qr_retiro -> {
                abrirFragment(QrRetiroFragment(), item.title)
            }
            R.id.nav_perfil -> {
                abrirFragment(PerfilFragment(), item.title)
            }

            R.id.nav_ayuda -> {
                abrirAyuda()
            }
            R.id.nav_cerrar_sesion -> {
                cerrarSesion(this)
            }
            else -> {
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (fragmentHome) {
            super.onBackPressed()
        } else {
            abrirFragment(HomeFragment(), navTitulo)
        }
    }
}
