package co.com.asistia.tools.Adapters

import MyProgramObject
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.com.asistia.tools.Pages.NormalActivity
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import co.com.asistia.tools.utils.LoadingScreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.util.*


class MyProgramAdapter(programList: MutableList<MyProgramObject>, myActivity: Activity) : ListAdapter<MyProgramObject, MyProgramAdapter.DefaultListHolder>(DiffUtilCallbackMyProgram) {

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
            .inflate(R.layout.recycler_my_program_item, parent, false)

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
     * Clase para dar forma al recycler para vista de usuario
     */
    class DefaultListHolder(view: View) : RecyclerView.ViewHolder(view) {

        // iniciamos la clase
        var myActivityIntenal = view.context as NormalActivity;
        // iniciamos la clase
        var GlobalMethods = GlobalMethods(myActivityIntenal);

        // Definimos el card
        val recyclerCardView = view.findViewById(R.id.cardRecyclerMyProgram) as CardView;

        // inicamos la referencia de los elementos
        val recyclerTextValueOne = view.findViewById(R.id.textMainDataItem) as TextView;
        val recyclerTextValueTwo = view.findViewById(R.id.textSecondDataItem) as TextView;
        val recyclerTextValueThree = view.findViewById(R.id.textThirtyDataItem) as TextView;

        // Refenciamos los botones
        val recyclerBtnDennyAssign = view.findViewById(R.id.btnDennyAssign) as ImageButton;
        val recyclerBtnApproveAssign = view.findViewById(R.id.btnApproveAssign) as ImageButton;
        val recyclerBtnCheckin = view.findViewById(R.id.btnCheckin) as ImageButton;
        val recyclerBtnCheckout = view.findViewById(R.id.btnCheckout) as ImageButton;
        val recyclerBtnFormCX = view.findViewById(R.id.btnFormCX) as ImageButton;

        /**
         * Creamos el metodo para renderizar nustro objeto y datos de la vista de usuario
         */
        fun bind(programData: MyProgramObject, context: Context){

            // cargamos cada uno de los valores
            recyclerTextValueOne.text = programData.client;
            recyclerTextValueTwo.text = programData.creationDate;
           recyclerTextValueThree.text = programData.observations;

            // Por defceto ocultamos todos los botones
            recyclerBtnDennyAssign.visibility = View.GONE;
            recyclerBtnApproveAssign.visibility = View.GONE;
            recyclerBtnCheckin.visibility = View.GONE;
            recyclerBtnCheckout.visibility = View.GONE;
            recyclerBtnFormCX.visibility = View.GONE;

            // Asignamos el evento para ver detalles
            recyclerCardView.setOnClickListener(View.OnClickListener { view ->
                GlobalMethods.showDetailsPage(programData);
            });

            // validamos si se debe mostrar la opción de gestionar asignación
            if(programData.assignStatus == "Asignado y notificado" && programData.assignAuthorize == ""){

                // Mostramos las opciones de gestión
                recyclerBtnDennyAssign.visibility = View.VISIBLE;
                recyclerBtnApproveAssign.visibility = View.VISIBLE;

                // Asignamos los eventos a estas 2 opciones
                recyclerBtnDennyAssign.setOnClickListener(View.OnClickListener { view ->
                    manageAssignEvent("¿Esta seguro que NO desea autorizar la asignación?", "No", programData);
                });
                recyclerBtnApproveAssign.setOnClickListener(View.OnClickListener { view ->
                    manageAssignEvent("¿Esta seguro que desea autorizar la asignación?", "Si", programData);
                });
            } else if(programData.assignStatus == "Gestionada" && programData.assignAuthorize == "Si"){

                // Mostramos la opción de checkin
                recyclerBtnCheckin.visibility = View.VISIBLE;

                // Asignamos el evento a la opción de checkin
                recyclerBtnCheckin.setOnClickListener(View.OnClickListener { view ->
                    // Permite mostrar el módulo de checkin
                    GlobalMethods.showCheckinPage(programData);
                });

            } else if(programData.assignStatus == "Registro CX" && programData.assignAuthorize == "Si"){

                // Mostramos la opción de checkout
                recyclerBtnCheckout.visibility = View.VISIBLE;

                // Asignamos el evento a la opción de checkout
                recyclerBtnCheckout.setOnClickListener(View.OnClickListener { view ->
                    // Permite mostrar el módulo de checkout
                    GlobalMethods.showCheckioutPage(programData);
                });
            } else if(programData.assignStatus == "Registro de checkin" && programData.assignAuthorize == "Si"){

                // Mostramos la opción de formulario CX
                recyclerBtnFormCX.visibility = View.VISIBLE;

                // Asignamos el evento a la opción de formulario CX
                recyclerBtnFormCX.setOnClickListener(View.OnClickListener { view ->

                    // Permite mostrar el módulo de formulario CX
                    GlobalMethods.showFormCxPage(programData);
                });
            }
        }

        /**
         * Permite gestionar la asignación de una programación
         */
        fun manageAssignEvent(confirmText: String, assignAuthorize: String, programData: MyProgramObject){

            // mostramos la ventana de confirmación
            GlobalMethods.showConfirm("Gestión de asignación", confirmText, DialogInterface.OnClickListener { dialogInterface, i ->

                // MOstramos loading para habilitar la ventana de selección de cuentas
                LoadingScreen.displayLoadingWithText(myActivityIntenal,"Registrando asignación...",false);

                // definimos los datos a enviar
                val json = JSONObject();
                json.put("rowIndex", programData.rowIndex);
                json.put("assignStatus", "Gestionada");
                json.put("assignAuthorize", assignAuthorize);

                // definimos el objeto base
                val mainJson = JSONObject();
                mainJson.put("method", "manageAssignEvent");
                mainJson.put("params", json);

                // se realiza la petición
                HttpTask( {

                    // definimos un mensaje general
                    var alerText_ = "No fue posible registrar la gestión de asignación de la programación";

                    // Se valida que no exista error
                    if (it != null) {
                        // retornamos la respuesta
                        var responseJson = JSONObject(it);

                        // validamos si la respuesta es correcta
                        if (responseJson.getBoolean("success")) {

                            // obtenemos los datos de respuesta
                            var returnObject = responseJson.getJSONObject("returnValue");

                            // obtenemos el mensaje a mostrar
                            alerText_ = returnObject.getString("message");

                            // validamos si la respuesta es positiva
                            if (returnObject.getBoolean("success")) {

                                // Ocultamos el loading
                                LoadingScreen.hideLoading();

                                // Obtenemos los datos de programaciones
                                GlobalMethods.getMyProgramming(programData.responsibleEmail, myActivityIntenal.findViewById(R.id.lvData));
                            }
                        }
                    }

                    // Ocultamos el loading
                    LoadingScreen.hideLoading();

                    // mostramos mensaje
                    GlobalMethods.showAlert("Gestión de asignación", alerText_, null);
                } ).execute("POST", null, mainJson.toString());
            })
        }
    }
}

private object DiffUtilCallbackMyProgram: DiffUtil.ItemCallback<MyProgramObject>(){
    override fun areItemsTheSame(oldItem: MyProgramObject, newItem: MyProgramObject): Boolean {
        return oldItem.rowIndex  == newItem.rowIndex;
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: MyProgramObject, newItem: MyProgramObject): Boolean {
        TODO("Not yet implemented")
        return oldItem.equals(newItem);
    }
}