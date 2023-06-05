package co.com.asistia.tools.Adapters

import LocationObject
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.com.asistia.tools.HomeActivity
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import java.util.*

class LocationAdapter(locationList: MutableList<LocationObject>, myActivity: Activity) : ListAdapter<LocationObject, LocationAdapter.DefaultListHolder>(DiffUtilCallbackLocation){

    var locationList_ = locationList;
    var context = myActivity;
    var myActivityGlobal = myActivity;

    /**
     * Permite crear una vista por cada item de la lista
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultListHolder {
        println(viewType)

        // Definimos la vista que se desea tomar como referencia
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_location_item, parent, false);

        // retornamos
        return DefaultListHolder(view);

    }

    override fun getItemCount(): Int {
        return locationList_.size;
    }

    fun getFilter(): Filter {
        return locationFilter;
    }

    /**
     * Metodo de busqueda
     **/
    private val locationFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<LocationObject> = ArrayList();

            if (constraint == null || constraint.isEmpty()) {
                locationList_ = locationList;
            } else {
                val query = constraint.toString().trim().toLowerCase()
                locationList.forEach {
                    if (it.date.toLowerCase(Locale.ROOT).contains(query) || it.email.toLowerCase(Locale.ROOT).contains(query) || it.fullName.toLowerCase(Locale.ROOT).contains(query) || it.address.toLowerCase(Locale.ROOT).contains(query)) {
                        filteredList.add(it);
                    }
                }
                locationList_ = filteredList;
            }
            val results = FilterResults();
            results.values = locationList_;
            return results;
        }

        /**
         * Permite actualizar la vista - https://github.com/johncodeos-blog/SearchRecyclerViewExample/blob/master/app/src/main/java/com/example/searchrecyclerviewexample/RecyclerViewAdapter.kt
         **/
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values is ArrayList<*>) {
                locationList_ = (results.values as ArrayList<LocationObject>);
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Permite qeu hacer con cada item recorrido, es decir, permite agregar los valores de cada lista
     */
    override fun onBindViewHolder(holder: DefaultListHolder, position: Int) {

        val item = locationList_.get(position);
        holder.bind(item, context);

        // renderizamos cada uno de los elementos
        holder.bind(locationList_[position], context);
    }

    /**
     * Clase para dar forma al recycler
     */
    class DefaultListHolder(view: View) : RecyclerView.ViewHolder(view) {

        // iniciamos la clase
        var myActivityIntenal = view.context as HomeActivity;
        // iniciamos la clase
        var GlobalMethods = GlobalMethods(myActivityIntenal);

        // Definimos el card
        val recyclerButton = view.findViewById(R.id.btnOpenMap) as Button;

        // inicamos la referencia de los elementos
        val recyclerTextValueOne = view.findViewById(R.id.textMainDataItem) as TextView;
        val recyclerTextValueTwo = view.findViewById(R.id.textSecondDataItem) as TextView;
        val recyclerTextValueThree = view.findViewById(R.id.textThirtyDataItem) as TextView;

        /**
         * Creamos el metodo para renderizar nustro objeto y datos
         */
        fun bind(locationData: LocationObject, context: Context){

            // cargamos cada uno de los valores
            recyclerTextValueOne.text = locationData.fullName;
            recyclerTextValueTwo.text = locationData.date;
            recyclerTextValueThree.text = locationData.address;

            // Asignamos el evento para ver detalles
            recyclerButton.setOnClickListener(View.OnClickListener { view ->
                println("https://www.google.com/maps/place/" + locationData.latLong)
                // Se procede abrir la url
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/place/" + locationData.latLong)));
            });

        }
    }
}

private object DiffUtilCallbackLocation: DiffUtil.ItemCallback<LocationObject>(){
    override fun areItemsTheSame(oldItem: LocationObject, newItem: LocationObject): Boolean {
        return oldItem.email  == newItem.email;
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: LocationObject, newItem: LocationObject): Boolean {
        TODO("Not yet implemented")
        return oldItem.equals(newItem);
    }
}