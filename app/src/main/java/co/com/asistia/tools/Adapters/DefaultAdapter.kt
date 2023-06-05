package co.com.asistia.tools.Adapters
import DefaultMessageObject
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.com.asistia.tools.R
import java.util.*


class DefaultAdapter(defaultData: MutableList<DefaultMessageObject>, myActivity: Activity) : ListAdapter<DefaultMessageObject, DefaultAdapter.DefaultListHolder>(DiffUtilCallbackDefault) {

    var defaultData_ = defaultData;
    var context = myActivity;

    /**
     * Permite crear una vista por cada item de la lista
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultListHolder {
        println(viewType)
        // Definimos la vista que se desea tomar como referencia
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_default_item, parent, false)

        // retornamos
        return DefaultListHolder(view);

    }

    override fun getItemCount(): Int {
        return defaultData_.size;
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: ArrayList<DefaultMessageObject> = ArrayList();
                if (constraint == null || constraint.isEmpty()) {
                    defaultData_.let { filteredList.addAll(it) }
                } else {
                    val query = constraint.toString().trim().toLowerCase()
                    defaultData_.forEach {
                        if (it.message.toLowerCase(Locale.ROOT).contains(query)) {
                            filteredList.add(it);
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                defaultData_.clear()
                defaultData_.addAll(results!!.values as ArrayList<DefaultMessageObject>)
                notifyDataSetChanged();
            }

        }
    }

    /*fun getFilter(): Filter {
        return programFilter;
    }*/

    /**
     * Metodo de busqueda
     **/
    private val programFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<DefaultMessageObject> = ArrayList();
            if (constraint == null || constraint.isEmpty()) {
                defaultData_.let { filteredList.addAll(it) }
            } else {
                val query = constraint.toString().trim().toLowerCase()
                defaultData_.forEach {
                    if (it.message.toLowerCase(Locale.ROOT).contains(query)) {
                        filteredList.add(it);
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        /**
         * Permite actualizar la vista
         **/
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values is ArrayList<*>) {
                defaultData_.clear()
                defaultData_.addAll(results.values as ArrayList<DefaultMessageObject>)
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Permite qeu hacer con cada item recorrido, es decir, permite agregar los valores de cada lista
     */
    override fun onBindViewHolder(holder: DefaultListHolder, position: Int) {

        val item = defaultData_.get(position);
        holder.bind(item, context);

        // renderizamos cada uno de los elementos
        holder.bind(defaultData_[position], context);
    }

    /**
     * Clase para dar forma al recycler
     */
    class DefaultListHolder(view: View) : RecyclerView.ViewHolder(view) {

        // inicamos la referencia de los elementos
        val recyclerTitle = view.findViewById(R.id.recyclerTitleDefault) as TextView;
        val recyclerMessage = view.findViewById(R.id.recyclerMessageDefault) as TextView;

        /**
         * Creamos el metodo para renderizar nustro objeto y datos
         */
        fun bind(linkData: DefaultMessageObject, context: Context){

            // cargamos cada uno de los valores
            //recyclerTitle.text = Html.fromHtml("<b>" + linkData.title + "</b>");
            recyclerTitle.text = linkData.title;
            recyclerMessage.text = linkData.message;
        }
    }
}

private object DiffUtilCallbackDefault: DiffUtil.ItemCallback<DefaultMessageObject>(){
    override fun areItemsTheSame(oldItem: DefaultMessageObject, newItem: DefaultMessageObject): Boolean {
        return oldItem.title  == newItem.title;
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DefaultMessageObject, newItem: DefaultMessageObject): Boolean {
        TODO("Not yet implemented")
        return oldItem.equals(newItem);
    }
}
