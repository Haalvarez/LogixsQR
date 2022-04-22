import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.logixs.logixsqr.EntregaActivity
import com.logixs.logixsqr.EntregasPaquetesModel
import com.logixs.logixsqr.R
import java.util.*
import kotlin.collections.ArrayList


internal class CargadosAdapter(private val context: Context, listContacts: ArrayList<EntregasPaquetesModel>) :
    RecyclerView.Adapter<CargadosViewHolder>() {

    private var listContacts: ArrayList<EntregasPaquetesModel> = listContacts

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CargadosViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_cargados, parent, false)
        return CargadosViewHolder(view)
    }
    override fun onBindViewHolder(holder: CargadosViewHolder, position: Int) {
        val entrega = listContacts[position]
        holder.txvDescripcion.text = entrega.AddressLine
        holder.txvDescripcion.setOnClickListener {
            val intent = Intent(context, EntregaActivity::class.java)
            intent.putExtra("entrega", entrega)
            context.startActivity(intent)
        }
        holder.imgMapa.setOnClickListener {
            val uri: String = java.lang.String.format(
                Locale.ENGLISH,
                "https://maps.google.com/maps?q=loc:%f,%f",
                entrega.Lat.toFloat(),
                entrega.Lng.toFloat()
            )
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listContacts.size
    }

}

class CargadosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var txvDescripcion: TextView = itemView.findViewById(R.id.txv_descripcion)
    var imgMapa: ImageButton = itemView.findViewById(R.id.img_mapa)
}
