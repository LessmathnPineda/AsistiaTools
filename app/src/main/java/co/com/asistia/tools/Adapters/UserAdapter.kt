package co.com.asistia.tools.Adapters

import UserObject
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
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
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import co.com.asistia.tools.utils.LoadingScreen
import org.json.JSONObject
import java.util.*

class UserAdapter (userList: MutableList<UserObject>, myActivity: Activity) : ListAdapter<UserObject, UserAdapter.DefaultListHolder>(DiffUtilCallbackUser){

    var userList_ = userList;
    var context = myActivity;
    var myActivityGlobal = myActivity;

    /**
     * Permite crear una vista por cada item de la lista
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultListHolder {
        println(viewType)

        // Definimos la vista que se desea tomar como referencia
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_user_item, parent, false)

        // retornamos
        return DefaultListHolder(view);

    }

    override fun getItemCount(): Int {
        return userList_.size;
    }

    fun getFilter(): Filter {
        return userFilter;
    }

    /**
     * Metodo de busqueda
     **/
    private val userFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<UserObject> = ArrayList();
            if (constraint == null || constraint.isEmpty()) {
                userList_ = userList;
            } else {
                val query = constraint.toString().trim().toLowerCase()
                userList.forEach {
                    if (it.userName.toLowerCase(Locale.ROOT).contains(query) || it.userMail.toLowerCase(Locale.ROOT).contains(query)) {
                        filteredList.add(it);
                    }
                }
                userList_ = filteredList;
            }
            val results = FilterResults();
            results.values = userList_;
            return results;
        }

        /**
         * Permite actualizar la vista - https://github.com/johncodeos-blog/SearchRecyclerViewExample/blob/master/app/src/main/java/com/example/searchrecyclerviewexample/RecyclerViewAdapter.kt
         **/
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values is ArrayList<*>) {
                userList_ = (results.values as ArrayList<UserObject>);
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Permite qeu hacer con cada item recorrido, es decir, permite agregar los valores de cada lista
     */
    override fun onBindViewHolder(holder: DefaultListHolder, position: Int) {

        val item = userList_.get(position);
        holder.bind(item, context);

        // renderizamos cada uno de los elementos
        holder.bind(userList_[position], context);
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
        val recyclerBtnDeleteUser = view.findViewById(R.id.btnDeleteUser) as ImageButton;
        val recyclerBtnEditUser = view.findViewById(R.id.btnEditUser) as ImageButton;

        /**
         * Creamos el metodo para renderizar nustro objeto y datos
         */
        fun bind(userData: UserObject, context: Context){

            // cargamos cada uno de los valores
            recyclerTextValueOne.text = userData.userName;
            recyclerTextValueTwo.text = userData.userProfile;
            recyclerTextValueThree.text = userData.userMail;

            // Asignamos el evento para ver detalles
            recyclerCardView.setOnClickListener(View.OnClickListener { view ->
                //GlobalMethods.showDetailsPage(userData);
            });

            // Asignamos los eventos a estas 2 opciones
            recyclerBtnDeleteUser.setOnClickListener(View.OnClickListener { view ->
                removeUserInSheet(userData);
            });
            recyclerBtnEditUser.setOnClickListener(View.OnClickListener { view ->
                // Habilitamos el módulo de usuarios
                GlobalMethods.showManageUser(userData);
            });

        }

        /**
         * Permite eliminar una programación
         */
        fun removeUserInSheet(userData: UserObject){

            // mostramos la ventana de confirmación
            GlobalMethods.showConfirm( "Eliminar usuario", "¿Seguro(a) desea eliminar este usuario?", DialogInterface.OnClickListener { dialogInterface, i ->

                // MOstramos loading para habilitar la ventana de eliminación
                LoadingScreen.displayLoadingWithText(myActivityIntenal, "Eliminando usuario...",false);

                // definimos los datos a enviar
                val json = JSONObject();
                json.put("rowIndex", userData.rowIndex);

                // definimos el objeto base
                val mainJson = JSONObject();
                mainJson.put("method", "removeUserInSheet");
                mainJson.put("params", json);

                // se realiza la petición
                HttpTask({

                    // definimos un mensaje general
                    var alerText_ = "No fue posible eliminar el usuario";

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
                    GlobalMethods.getUsersList(myActivityIntenal.findViewById(R.id.lvData));

                    // mostramos mensaje
                    GlobalMethods.showAlert( "Eliminar usuario", alerText_, null);
                }).execute("POST", null, mainJson.toString());
            });
        }
    }
}

private object DiffUtilCallbackUser: DiffUtil.ItemCallback<UserObject>(){
    override fun areItemsTheSame(oldItem: UserObject, newItem: UserObject): Boolean {
        return oldItem.rowIndex  == newItem.rowIndex;
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: UserObject, newItem: UserObject): Boolean {
        TODO("Not yet implemented")
        return oldItem.equals(newItem);
    }
}