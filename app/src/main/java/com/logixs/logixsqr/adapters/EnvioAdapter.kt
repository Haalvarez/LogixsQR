import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.logixs.logixsqr.R
import com.logixs.logixsqr.database.Envio

internal class EnvioAdapter(private val context: Context, listContacts: ArrayList<Envio>) :
    RecyclerView.Adapter<EnvioViewHolder>() {

    private var listContacts: ArrayList<Envio> = listContacts

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnvioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_envio, parent, false)
        return EnvioViewHolder(view)
    }
    override fun onBindViewHolder(holder: EnvioViewHolder, position: Int) {
        val envio = listContacts[position]
        holder.txvIdEnvio.text = envio.idEnvio
    }

    override fun getItemCount(): Int {
        return listContacts.size
    }

}

class EnvioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var txvIdEnvio: TextView = itemView.findViewById(R.id.txv_id_envio)
}
