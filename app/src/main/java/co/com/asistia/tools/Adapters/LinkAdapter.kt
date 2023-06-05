package co.com.asistia.tools.Adapters

import ModelLinks
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.com.asistia.tools.R
import java.util.*


class LinkAdapter(listAccess_: MutableList<ModelLinks>, myActivity: Activity) : ListAdapter<ModelLinks, LinkAdapter.LinkListHolder>(DiffUtilCallback) {

    var linkData_ = listAccess_;
    var context = myActivity;
    var linkFilterList = listAccess_;

    /**
     * Permite crear una vista por cada item de la lista
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkListHolder {

        // Definimos la vista que se desea tomar como referencia
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_form_list_item, parent, false)

        // retornamos
        return LinkListHolder(view);

    }

    override fun getItemCount(): Int {
        return linkData_.size;
    }

    fun getFilter(): Filter {
        println(getItemCount())
        return linkFilter;
    }

    /**
     * Metodo de busqueda
     **/
    private val linkFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filteredList: ArrayList<ModelLinks> = ArrayList();
            if (constraint == null || constraint.isEmpty()) {
                linkData_ = listAccess_;
            } else {
                val query = constraint.toString().trim().toLowerCase()
                listAccess_.forEach {
                    if (it.pageName.toLowerCase(Locale.ROOT).contains(query)) {
                        filteredList.add(it);
                    }
                }

                linkData_ = filteredList;
            }
            val results = FilterResults();
            results.values = linkData_;
            return results;
        }

        /**
         * Permite actualizar la vista - https://github.com/johncodeos-blog/SearchRecyclerViewExample/blob/master/app/src/main/java/com/example/searchrecyclerviewexample/RecyclerViewAdapter.kt
         **/
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values is ArrayList<*>) {
                linkData_ = (results.values as ArrayList<ModelLinks>);
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Permite qeu hacer con cada item recorrido, es decir, permite agregar los valores de cada lista
     */
    override fun onBindViewHolder(holder: LinkListHolder, position: Int) {

        val item = linkData_.get(position);
        holder.bind(item, context);

        // renderizamos cada uno de los elementos
        holder.bind(linkData_[position], context);
    }

    /**
     * Clase para dar forma al recycler
     */
    class LinkListHolder(view: View) : RecyclerView.ViewHolder(view) {

        // inicamos la referencia de los elementos
        val recyclerLinkName = view.findViewById(R.id.directAccessLink) as TextView;
        val recyclerCard = view.findViewById(R.id.cardLinkItem) as CardView;

        /**
         * Creamos el metodo para renderizar nustro objeto y datos
         */
        fun bind(linkData: ModelLinks, context: Context){

            // cargamos cada uno de los valores
            recyclerLinkName.text = Html.fromHtml("<b>" + linkData.pageName + "</b>");

            // agregamos el evento para que al dar clic muestr el panel de detalles
            recyclerCard.setOnClickListener(){

                // Se procede abrir la url
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkData.pageLink)));
            }
        }
    }
}

private object DiffUtilCallback: DiffUtil.ItemCallback<ModelLinks>(){
    override fun areItemsTheSame(oldItem: ModelLinks, newItem: ModelLinks): Boolean {
        return oldItem.pageName  == newItem.pageName;
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ModelLinks, newItem: ModelLinks): Boolean {
        TODO("Not yet implemented")
        return oldItem.equals(newItem);
    }
}
