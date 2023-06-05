package co.com.asistia.tools.Adapters

import MyProgramObject
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.com.asistia.tools.HomeActivity
import co.com.asistia.tools.Pages.NormalActivity
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import co.com.asistia.tools.utils.LoadingScreen
import org.json.JSONObject
import java.util.*

class AdminProgramAdapter(programList: MutableList<MyProgramObject>, myActivity: Activity) : ListAdapter<MyProgramObject, AdminProgramAdapter.DefaultListHolder>(DiffUtilCallbackAdminProgram) {

    var programList_ = programList;
    var context = myActivity;
    var myActivityGlobal = myActivity;

    /**
     * Permite crear una vista por cada item de la lista
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultListHolder {
        println(viewType)

        // Definimos la vista que se desea tomar como referencia
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_admin_program_item, parent, false)

        // retornamos
        return DefaultListHolder(view);

    }

    override fun getItemCount(): Int {
        return programList_.size;
    }

    fun getFilter(): Filter {
        return programFilter;
    }

    /**
     * Metodo de busqueda
     **/
    private val programFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<MyProgramObject> = ArrayList();
            if (constraint == null || constraint.isEmpty()) {
                programList_ = programList;
            } else {
                val query = constraint.toString().trim().toLowerCase()
                programList.forEach {
                    if (it.creationDate.toLowerCase(Locale.ROOT).contains(query) || it.client.toLowerCase(Locale.ROOT).contains(query) || it.assignStatus.toLowerCase(Locale.ROOT).contains(query) || it.city.toLowerCase(Locale.ROOT).contains(query) || it.patientName.toLowerCase(Locale.ROOT).contains(query) || it.responsibleEmail.toLowerCase(Locale.ROOT).contains(query) || it.responsibleName.toLowerCase(Locale.ROOT).contains(query)) {
                        filteredList.add(it);
                    }
                }
                programList_ = filteredList;
            }
            val results = FilterResults();
            results.values = programList_;
            return results;
        }

        /**
         * Permite actualizar la vista - https://github.com/johncodeos-blog/SearchRecyclerViewExample/blob/master/app/src/main/java/com/example/searchrecyclerviewexample/RecyclerViewAdapter.kt
         **/
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values is ArrayList<*>) {
                programList_ = (results.values as ArrayList<MyProgramObject>);
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Permite qeu hacer con cada item recorrido, es decir, permite agregar los valores de cada lista
     */
    override fun onBindViewHolder(holder: DefaultListHolder, position: Int) {

        val item = programList_.get(position);
        holder.bind(item, context);

        // renderizamos cada uno de los elementos
        holder.bind(programList_[position], context);
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
        val recyclerCardView = view.findViewById(R.id.cardRecyclerMyProgram) as CardView;

        // inicamos la referencia de los elementos
        val recyclerTextValueOne = view.findViewById(R.id.textMainDataItem) as TextView;
        val recyclerTextValueTwo = view.findViewById(R.id.textSecondDataItem) as TextView;
        val recyclerTextValueThree = view.findViewById(R.id.textThirtyDataItem) as TextView;

        // Refenciamos los botones
        val recyclerBtnRemove = view.findViewById(R.id.btnRemoveProgramItem) as ImageButton;
        val recyclerBtnEdit = view.findViewById(R.id.btnEditProgramItem) as ImageButton;

        /**
         * Creamos el metodo para renderizar nustro objeto y datos
         */
        fun bind(programData: MyProgramObject, context: Context){

            // cargamos cada uno de los valores
            recyclerTextValueOne.text = programData.client;
            recyclerTextValueTwo.text = programData.assignDate;
            recyclerTextValueThree.text = programData.responsibleEmail;

            // validamos que estaoo es para asignar el color respectivp
            if(programData.assignStatus == "Asignado y notificado"){
                recyclerTextValueOne.setTextColor(Color.parseColor("#da7a00"));
            } else if(programData.assignStatus == "Gestionada"){
                recyclerTextValueOne.setTextColor(Color.parseColor("#00b11f"));
            } else if(programData.assignStatus == "Registro de checkin"){
                recyclerTextValueOne.setTextColor(Color.parseColor("#037be4"));
            } else if(programData.assignStatus == "Registro CX"){
                recyclerTextValueOne.setTextColor(Color.parseColor("#7c00ff"));
            } else if(programData.assignStatus == "Registro de checkout"){
                recyclerTextValueOne.setTextColor(Color.parseColor("#fb0a82"));
            } else {
                recyclerTextValueOne.setTextColor(Color.parseColor("#cc0b0b"));
            }

            // Asignamos el evento para ver detalles
            recyclerCardView.setOnClickListener(View.OnClickListener { view ->
                GlobalMethods.showDetailsPage(programData);
            });

            // Asignamos los eventos a estas 2 opciones
            recyclerBtnRemove.setOnClickListener(View.OnClickListener { view ->
                removeProgramingInSheet(programData);
            });
            recyclerBtnEdit.setOnClickListener(View.OnClickListener { view ->
                // Habilitamos el módulo de usuarios
                GlobalMethods.showManageProgramming(programData);
            });
        }

        /**
         * Permite eliminar una programación
         */
        fun removeProgramingInSheet(programData: MyProgramObject){

            // mostramos la ventana de confirmación
            GlobalMethods.showConfirm( "Eliminar programación", "¿Seguro(a) desea eliminar esta programación?", DialogInterface.OnClickListener { dialogInterface, i ->

                // MOstramos loading para habilitar la ventana de selección de cuentas
                LoadingScreen.displayLoadingWithText(myActivityIntenal, "Eliminando programación...",false);

                // definimos los datos a enviar
                val json = JSONObject();
                json.put("rowIndex", programData.rowIndex);

                // definimos el objeto base
                val mainJson = JSONObject();
                mainJson.put("method", "removeProgramingInSheet");
                mainJson.put("params", json);

                // se realiza la petición
                HttpTask({

                    // definimos un mensaje general
                    var alerText_ = "No fue posible eliminar la programación";
                    var isCloseActivity = false;

                    // Se valida que no exista error
                    if (it != null) {
                        // retornamos la respuesta
                        var responseJson = JSONObject(it);

                        // validamos si la respuesta es correcta
                        if (responseJson.getBoolean("success")) {

                            // obtenemos los datos de respuesta
                            var returnObject =
                                responseJson.getJSONObject("returnValue");

                            // obtenemos el mensaje a mostrar
                            alerText_ = returnObject.getString("message");
                        }
                    }

                    // Ocultamos el loading
                    LoadingScreen.hideLoading();

                    // recarga la lista
                    GlobalMethods.getCurrentProgramming(myActivityIntenal.findViewById(R.id.lvData));

                    // mostramos mensaje
                    GlobalMethods.showAlert( "Eliminar programación", alerText_, null);
                }).execute("POST", null, mainJson.toString());
            });
        }
    }
}

private object DiffUtilCallbackAdminProgram: DiffUtil.ItemCallback<MyProgramObject>(){
    override fun areItemsTheSame(oldItem: MyProgramObject, newItem: MyProgramObject): Boolean {
        return oldItem.rowIndex  == newItem.rowIndex;
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: MyProgramObject, newItem: MyProgramObject): Boolean {
        TODO("Not yet implemented")
        return oldItem.equals(newItem);
    }
}