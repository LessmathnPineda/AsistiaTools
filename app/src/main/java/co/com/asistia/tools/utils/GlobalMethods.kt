package co.com.asistia.tools.utils

import DefaultMessageObject
import LocationObject
import ModelLinks
import MyProgramObject
import UserObject
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import co.com.asistia.tools.Adapters.*
import co.com.asistia.tools.CircleTransform
import co.com.asistia.tools.LocationUpdatesService
import co.com.asistia.tools.MainActivity
import co.com.asistia.tools.Pages.*
import co.com.asistia.tools.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.util.*

/**
 * Se crea la clase que vamos a usar en todas las actividades existentes
 * */
class GlobalMethods( activity: Activity){

    // definimos la variable que referencia la acatividad donde se inicio la clase
    lateinit var myActivity: Activity;
    val PERMISSION_ID = 42;
    val BACKGROUND_LOCATION_PERMISSION_API_30_REQUEST_CODE = 200;

    // initializer block
    init {
        println("Clase Iniciada")
        myActivity = activity;
    }

    /**
     * Permite mostrar mensaje en la interfaz personalizada
     */
    @SuppressLint("ResourceAsColor")
    fun showAlertCustom(title: String?, message: String?) {
        println("LLLEGOOOOO")
        // MOstramos alerta
        val builderCustom = AlertDialog.Builder(myActivity,R.style.CustomAlertDialog).create();
        val viewCustom = myActivity.layoutInflater.inflate(R.layout.custom_alert_layout,null);

        // definimos la variable para guardar el titulo
        var titleAlert = title;

        // valido si el titulo es null
        if(titleAlert == null) titleAlert = myActivity.getString(R.string.title_alert);

        // Actualizamos los valores
        viewCustom.findViewById<TextView>(R.id.titleAlertDanger).setText(titleAlert);
        viewCustom.findViewById<TextView>(R.id.messageAlertDanger).setText(message);

        // Referenciamos el boton para asignar evento
        val button = viewCustom.findViewById<Button>(R.id.dialogDismiss_button);
        builderCustom.setView(viewCustom)
        button.setOnClickListener {
            builderCustom.dismiss();
        }
        builderCustom.setCanceledOnTouchOutside(false);
        builderCustom.show();
    }

    /**
     * Permite mostrar mensaje en la interfaz
     */
    @SuppressLint("ResourceAsColor")
    fun showAlert(title: String?, message: String?, listener: DialogInterface.OnClickListener? ) {

        // Creamos una interfaz de dialogo para mostrar el mensa
        val builder = MaterialAlertDialogBuilder(myActivity, R.style.AlertDialogTheme);

        // definimos la variable para guardar el titulo
        var titleAlert = title;

        // valido si el titulo es null
        if(titleAlert == null) titleAlert = myActivity.getString(R.string.title_alert);

        // agregamos las propiedades
        builder.setTitle(titleAlert)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.alert_ok, listener);

        // Se crea el alerta y se visualiza en la interfaz
        builder.show();
    }

    /**
     * Permite mostrar mensaje de confirmación
     */
    @SuppressLint("ResourceAsColor")
    fun showConfirm(title: String?, message: String?, listener: DialogInterface.OnClickListener? ) {

        // definimos la variable para guardar el titulo
        var titleConfirm = title;

        // valido si el titulo es null
        if(titleConfirm == null) titleConfirm = myActivity.getString(R.string.title_confirm);

        // Creamos una interfaz de dialogo para mostrar el mensa
        val builder = MaterialAlertDialogBuilder(myActivity, R.style.AlertDialogTheme);

        // agregamos las propiedades
        builder.setTitle(titleConfirm)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.alert_ok, listener)
            .setNegativeButton(R.string.alert_cancel, null);

        // Se crea el alerta y se visualiza en la interfaz
        builder.show();
    }

    /**
     * Permite inicializar el menu inferior
     */
    fun initButtomMenuToolbar(userEmail: String, lvData: RecyclerView, searchView: SearchView, swipeRefreshLayout: SwipeRefreshLayout){

        // refernciamos los campos
        var tabMyProgram: LinearLayout = myActivity.findViewById(R.id.tabMyProgramming);
        var tabFormList: LinearLayout = myActivity.findViewById(R.id.tabFormList);
        var G_MODULE_NORMAL = "normal_programming";

        // Creamos los eventos para obtener mis programaciones
        tabMyProgram.setOnClickListener {
            getMyProgramming(userEmail, lvData);
            searchView.setQuery("", false);
            G_MODULE_NORMAL = "normal_programming";
        };

        // Obtenemos la lista de acceso directos
        tabFormList.setOnClickListener {
            getDirectAccess(lvData, false);
            searchView.setQuery("", false);
            G_MODULE_NORMAL = "normal_form";
        };

        // Se agrega el evento para que recargue el recycler
        swipeRefreshLayout.setOnRefreshListener { // Esto se ejecuta cada vez que se realiza el gesto

            // Filtramos que modulo debe de cargar
            if(G_MODULE_NORMAL == "normal_programming"){
                getMyProgramming(userEmail, lvData);
            } else if(G_MODULE_NORMAL == "normal_form"){
                getDirectAccess(lvData, false);
            }

            // Ocultamos por defecto el loading
            swipeRefreshLayout.isRefreshing = false;
        }
    }

    /**
     * Permite inicializar el menu inferior completo
     */
    fun initButtomMenuAdminToolbar(lvData: RecyclerView, fabButton: FloatingActionButton, searchView: SearchView, swipeRefreshLayout: SwipeRefreshLayout, currModule: String){

        // refernciamos los campos
        var tabMyProgram: LinearLayout = myActivity.findViewById(R.id.tabProgramming);
        var tabFormList: LinearLayout = myActivity.findViewById(R.id.tabFormList);
        var tabUsers: LinearLayout = myActivity.findViewById(R.id.tabUsers);
        var tabLocations: LinearLayout = myActivity.findViewById(R.id.tabLocations);
        var G_MODULE_ADMIN = currModule;

        // Creamos los eventos para obtener programaciones
        tabMyProgram.setOnClickListener {
            // mostramos la opción
            fabButton.visibility = View.VISIBLE;
            getCurrentProgramming(lvData);
            searchView.setQuery("", false);
            G_MODULE_ADMIN = "admin_programming";
        };

        // Obtenemos la lista de usuarios
        tabUsers.setOnClickListener {
            // mostramos la opción
            fabButton.visibility = View.VISIBLE;
            getUsersList(lvData);
            searchView.setQuery("", false);
            G_MODULE_ADMIN = "admin_user";
        };

        // Obtenemos la lista de acceso directos
        tabFormList.setOnClickListener {
            // Ocultamos la opción
            fabButton.visibility = View.GONE;
            getDirectAccess(lvData, true);
            searchView.setQuery("", false);
            G_MODULE_ADMIN = "admin_form";
        };

        // Obtenemos la lista de ubicaciones
        tabLocations.setOnClickListener {
            // Ocultamos la opción
            fabButton.visibility = View.GONE;
            getLocationList(lvData);
            searchView.setQuery("", false);
            G_MODULE_ADMIN = "admin_location";
        };

        // Se agrega el evento para que recargue el recycler
        swipeRefreshLayout.setOnRefreshListener { // Esto se ejecuta cada vez que se realiza el gesto

            // Filtramos que modulo debe de cargar
            if(G_MODULE_ADMIN == "admin_programming"){
                getCurrentProgramming(lvData);
            } else if(G_MODULE_ADMIN == "admin_user"){
                getUsersList(lvData);
            } else if(G_MODULE_ADMIN == "admin_form"){
                getDirectAccess(lvData, true);
            } else if(G_MODULE_ADMIN == "admin_location"){
                getLocationList(lvData);
            }

            // Ocultamos por defecto el loading
            swipeRefreshLayout.isRefreshing = false;
        }
    }

    /**
     * Permite obtener la lista de programaciones del día
     */
    fun getCurrentProgramming(lvData: RecyclerView){

        // creamos una lista muteable para permitir agregar items
        var listAccess_: MutableList<MyProgramObject> = ArrayList();
        //lvData?.adapter = AdminProgramAdapter(listAccess_, myActivity);

        // activamos la opción
        manageActiveOptionFull_(myActivity.findViewById<LinearLayout?>(R.id.tabProgramming), myActivity.findViewById<LinearLayout?>(R.id.tabLocations), myActivity.findViewById<LinearLayout?>(R.id.tabFormList), myActivity.findViewById<LinearLayout?>(R.id.tabUsers));

        // Actualizamos el titulo del panel
        myActivity.findViewById<TextView?>(R.id.textMenuLayout).setText("Programaciones");

        // MOstramos loading para habilitar la ventana de selección de cuentas
        LoadingScreen.displayLoadingWithText(myActivity,"Consultando lista de programaciones...",false);

        // definimos el objeto base
        val mainJson = JSONObject();
        mainJson.put("method", "getCurrentProgramming");

        // se realiza la petición
        HttpTask( {
            println(it)

            if (it == null) {
                println("Error de conexión");
            } else {

                // retornamos la respuesta
                var responseJson = JSONObject(it);

                // validamos si la respuesta es correcta
                if (responseJson.getBoolean("success")) {

                    // obtenemos los datos de respuesta
                    var returnArray = responseJson.getJSONArray("returnValue");

                    // recorremos cada uno de los vinculos
                    for (i in 0 until returnArray.length()) {

                        val item = returnArray.getJSONObject(i);
                        listAccess_?.add(MyProgramObject(item.getInt("rowIndex"), item.getString("colective"), item.getString("regional"), item.getString("observations"), item.getString("city"), item.getString("creationDate"), item.getString("onlyDate"), item.getString("onlyHour"), item.getString("client"), item.getString("systemJJ"), item.getString("systemOM"), item.getString("system"), item.getString("companyAT"), item.getString("specialist"), item.getString("patientName"), item.getString("pagador"), item.getString("lineNeg"), item.getString("house"), item.getString("patientCC"), item.getString("appoitmentNumber"), item.getString("requestNumber"), item.getString("monthOne"), item.getString("urgenceHours"), item.getString("coberture"), item.getString("confirmEmailHour"), item.getString("planning"), item.getString("externSupport"), item.getString("responsibleEmail"), item.getString("responsibleName"), item.getString("assignDate"), item.getString("assignStatus"), item.getString("assignAuthorize"), item.getString("realArriveDate"), item.getString("arriveDate"), item.getString("arriveLocation"), item.getString("realExitDate"), item.getString("exitDate"), item.getString("exitLocation"), item.getString("supportImage"), item.getString("supportDoc"), item.getString("technicalName"), item.getString("remissionNumber"), item.getString("processStatus"), item.getString("typeCX"), item.getString("date"), item.getString("initialHour"), item.getString("initialHourCX"), item.getString("cancelMotive"), item.getString("otherCancelMotive"), item.getString("finishHourCX"), item.getString("generalComments"), item.getString("abourtType"), item.getString("patientNameConfirm"), item.getString("prequirurgico"), item.getString("motiveCXConsume"), item.getString("remissionStatus"), item.getString("perfectProcess")));
                    }
                }
            }

            // Ocultamos loading
            LoadingScreen.hideLoading();

            // validamos si no existe datos
            if(listAccess_.size == 0){

                // creamos una lista muteable para permitir agregar items
                var blankOptions_: MutableList<DefaultMessageObject> = ArrayList();

                // AGregamos la opción vacia -
                blankOptions_?.add(DefaultMessageObject("Sin información", "No existen programaciones para mostrar"));

                // Actualizamos el adapatador
                lvData?.adapter = DefaultAdapter(blankOptions_, myActivity);
            } else {

                // creamos un adapter personalizado
                lvData?.adapter = AdminProgramAdapter(listAccess_, myActivity);
            }

        } ).execute("POST", null, mainJson.toString());
    }

    /**
     * Permite obtener la lista de ubicaciones
     */
    fun getLocationList(lvData: RecyclerView){

        // creamos una lista muteable para permitir agregar items
        var listAccess_: MutableList<LocationObject> = ArrayList();
        //lvData?.adapter = LocationAdapter(listAccess_, myActivity);

        // activamos la opción
        manageActiveOptionFull_(myActivity.findViewById<LinearLayout?>(R.id.tabLocations), myActivity.findViewById<LinearLayout?>(R.id.tabProgramming), myActivity.findViewById<LinearLayout?>(R.id.tabFormList), myActivity.findViewById<LinearLayout?>(R.id.tabUsers));

        // Actualizamos el titulo del panel
        myActivity.findViewById<TextView?>(R.id.textMenuLayout).setText("Lista de ubicaciones");

        // Mostramos loading para habilitar la ventana de selección de cuentas
        LoadingScreen.displayLoadingWithText(myActivity,"Consultando lista de ubicaciones...",false);

        // definimos el objeto base
        val mainJson = JSONObject();
        mainJson.put("method", "getAllLocation");

        // se realiza la petición
        HttpTask( {

            if (it == null) {
                println("Error de conexión");
            } else {

                // retornamos la respuesta
                var responseJson = JSONObject(it);

                // validamos si la respuesta es correcta
                if (responseJson.getBoolean("success")) {

                    // obtenemos los datos de respuesta
                    var returnArray = responseJson.getJSONArray("returnValue");

                    // recorremos cada uno de los vinculos
                    for (i in 0 until returnArray.length()) {

                        val item = returnArray.getJSONObject(i);
                        listAccess_?.add(LocationObject(item.getString("date"), item.getString("fullName"), item.getString("email"), item.getString("latLong"), item.getString("address")));
                    }
                }
            }
            println(it)
            // Ocultamos loading
            LoadingScreen.hideLoading();

            // validamos si no existe datos
            if(listAccess_.size == 0){

                // creamos una lista muteable para permitir agregar items
                var blankOptions_: MutableList<DefaultMessageObject> = ArrayList();

                // AGregamos la opción vacia -
                blankOptions_?.add(DefaultMessageObject("Sin información", "No existen ubicaciones para mostrar."));

                // Actualizamos el adapatador
                lvData?.adapter = DefaultAdapter(blankOptions_, myActivity);
            } else {

                // creamos un adapter personalizado
                lvData?.adapter = LocationAdapter(listAccess_, myActivity);
            }
        } ).execute("POST", null, mainJson.toString());
    }

    /**
     * Permite obtene la lista de usuarios
     */
    fun getUsersList(lvData: RecyclerView){

        // creamos una lista muteable para permitir agregar items
        var listAccess_: MutableList<UserObject> = ArrayList();
        //lvData?.adapter = UserAdapter(listAccess_, myActivity);

        // activamos la opción
        manageActiveOptionFull_(myActivity.findViewById<LinearLayout?>(R.id.tabUsers), myActivity.findViewById<LinearLayout?>(R.id.tabProgramming), myActivity.findViewById<LinearLayout?>(R.id.tabFormList), myActivity.findViewById<LinearLayout?>(R.id.tabLocations));

        // Actualizamos el titulo del panel
        myActivity.findViewById<TextView?>(R.id.textMenuLayout).setText("Lista de usuarios");

        // Mostramos loading para habilitar la ventana de selección de cuentas
        LoadingScreen.displayLoadingWithText(myActivity,"Consultando lista de usuarios...",false);

        // definimos el objeto base
        val mainJson = JSONObject();
        mainJson.put("method", "getAllUserList");

        // se realiza la petición
        HttpTask( {

            if (it == null) {
                println("Error de conexión");
            } else {

                // retornamos la respuesta
                var responseJson = JSONObject(it);

                // validamos si la respuesta es correcta
                if (responseJson.getBoolean("success")) {

                    // obtenemos los datos de respuesta
                    var returnArray = responseJson.getJSONArray("returnValue");

                    // recorremos cada uno de los vinculos
                    for (i in 0 until returnArray.length()) {

                        val item = returnArray.getJSONObject(i);
                        listAccess_?.add(UserObject(item.getInt("rowIndex"), item.getString("userId"), item.getString("userName"), item.getString("userMail"), item.getString("userStatus"), item.getString("userProfile"), item.getString("userType")));
                    }
                }
            }
            println(it)
            // Ocultamos loading
            LoadingScreen.hideLoading();

            // validamos si no existe datos
            if(listAccess_.size == 0){

                // creamos una lista muteable para permitir agregar items
                var blankOptions_: MutableList<DefaultMessageObject> = ArrayList();

                // AGregamos la opción vacia -
                blankOptions_?.add(DefaultMessageObject("Sin información", "No existen usuarios para mostrar."));

                // Actualizamos el adapatador
                lvData?.adapter = DefaultAdapter(blankOptions_, myActivity);
            } else {

                // creamos un adapter personalizado
                lvData?.adapter = UserAdapter(listAccess_, myActivity);
            }
        } ).execute("POST", null, mainJson.toString());
    }

    /**
     * Permite intercambiar entre opciones activas
     */
    fun manageActiveOptionFull_(elementActive: LinearLayout, elementNotActiveOne: LinearLayout, elementNotActiveTwo: LinearLayout, elementNotActiveThree: LinearLayout){

        // agregamos el color activo
        elementActive.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.purple_500));
        elementActive.findViewById<ImageView>(R.id.imageViewButtomTab).setColorFilter(Color.WHITE);
        elementActive.findViewById<TextView>(R.id.textOptionButtomTab).setTextColor(Color.WHITE);

        // eliminamos el color del que debe de quedar desactivo
        elementNotActiveOne.setBackgroundColor(Color.TRANSPARENT);
        elementNotActiveOne.findViewById<ImageView>(R.id.imageViewButtomTab).setColorFilter(ContextCompat.getColor(myActivity, R.color.gray_letter));
        elementNotActiveOne.findViewById<TextView>(R.id.textOptionButtomTab).setTextColor(ContextCompat.getColor(myActivity, R.color.gray_letter));

        elementNotActiveTwo.setBackgroundColor(Color.TRANSPARENT);
        elementNotActiveTwo.findViewById<ImageView>(R.id.imageViewButtomTab).setColorFilter(ContextCompat.getColor(myActivity, R.color.gray_letter));
        elementNotActiveTwo.findViewById<TextView>(R.id.textOptionButtomTab).setTextColor(ContextCompat.getColor(myActivity, R.color.gray_letter));

        elementNotActiveThree.setBackgroundColor(Color.TRANSPARENT);
        elementNotActiveThree.findViewById<ImageView>(R.id.imageViewButtomTab).setColorFilter(ContextCompat.getColor(myActivity, R.color.gray_letter));
        elementNotActiveThree.findViewById<TextView>(R.id.textOptionButtomTab).setTextColor(ContextCompat.getColor(myActivity, R.color.gray_letter));
    }

    /**
     * Permite obtener la lista de mis programaciones
     */
    fun getMyProgramming(userEmail: String, lvData: RecyclerView){

        // creamos una lista muteable para permitir agregar items
        var listAccess_: MutableList<MyProgramObject> = ArrayList();
        //lvData?.adapter = MyProgramAdapter(listAccess_, myActivity);

        // activamos la opción
        manageActiveOptionBasic_(myActivity.findViewById<LinearLayout?>(R.id.tabMyProgramming), myActivity.findViewById<LinearLayout?>(R.id.tabFormList));

        // Actualizamos el titulo del panel
        myActivity.findViewById<TextView?>(R.id.textMenuLayout).setText("Mis programaciones");

        // MOstramos loading para habilitar la ventana de selección de cuentas
        LoadingScreen.displayLoadingWithText(myActivity,"Consultando lista de programaciones...",false);

        // definimos los datos a enviar
        val json = JSONObject();
        json.put("email", userEmail);

        // definimos el objeto base
        val mainJson = JSONObject();
        mainJson.put("method", "getMyProgramming");
        mainJson.put("params", json);

        // se realiza la petición
        HttpTask( {
            println(it)

            if (it == null) {
                println("Error de conexión");
            } else {

                // retornamos la respuesta
                var responseJson = JSONObject(it);

                // validamos si la respuesta es correcta
                if (responseJson.getBoolean("success")) {

                    // obtenemos los datos de respuesta
                    var returnArray = responseJson.getJSONArray("returnValue");

                    // recorremos cada uno de los vinculos
                    for (i in 0 until returnArray.length()) {

                        val item = returnArray.getJSONObject(i);
                        listAccess_?.add(MyProgramObject(item.getInt("rowIndex"), item.getString("colective"), item.getString("regional"), item.getString("observations"), item.getString("city"), item.getString("creationDate"), item.getString("onlyDate"), item.getString("onlyHour"), item.getString("client"), item.getString("systemJJ"), item.getString("systemOM"), item.getString("system"), item.getString("companyAT"), item.getString("specialist"), item.getString("patientName"), item.getString("pagador"), item.getString("lineNeg"), item.getString("house"), item.getString("patientCC"), item.getString("appoitmentNumber"), item.getString("requestNumber"), item.getString("monthOne"), item.getString("urgenceHours"), item.getString("coberture"), item.getString("confirmEmailHour"), item.getString("planning"), item.getString("externSupport"), item.getString("responsibleEmail"), item.getString("responsibleName"), item.getString("assignDate"), item.getString("assignStatus"), item.getString("assignAuthorize"), item.getString("realArriveDate"), item.getString("arriveDate"), item.getString("arriveLocation"), item.getString("realExitDate"), item.getString("exitDate"), item.getString("exitLocation"), item.getString("supportImage"), item.getString("supportDoc"), item.getString("technicalName"), item.getString("remissionNumber"), item.getString("processStatus"), item.getString("typeCX"), item.getString("date"), item.getString("initialHour"), item.getString("initialHourCX"), item.getString("cancelMotive"), item.getString("otherCancelMotive"), item.getString("finishHourCX"), item.getString("generalComments"), item.getString("abourtType"), item.getString("patientNameConfirm"), item.getString("prequirurgico"), item.getString("motiveCXConsume"), item.getString("remissionStatus"), item.getString("perfectProcess")));
                    }
                }
            }

            // Ocultamos loading
            LoadingScreen.hideLoading();

            // validamos si no existe datos
            if(listAccess_.size == 0){

                // creamos una lista muteable para permitir agregar items
                var blankOptions_: MutableList<DefaultMessageObject> = ArrayList();

                // AGregamos la opción vacia -
                blankOptions_?.add(DefaultMessageObject("Sin información", "No existen programaciones para mostrar"));

                // Actualizamos el adapatador
                lvData?.adapter = DefaultAdapter(blankOptions_, myActivity);
            } else {

                // creamos un adapter personalizado
                lvData?.adapter = MyProgramAdapter(listAccess_, myActivity);
                //lvData?.adapter.
                //lvData?.adapter.getFilter().filter(query);
            }

        } ).execute("POST", null, mainJson.toString());
    }

    /**
     * Permite obtene la lista de acceso directos
     */
    fun getDirectAccess(lvData: RecyclerView, isAdmin: Boolean){

        // creamos una lista muteable para permitir agregar items
        var listAccess_: MutableList<ModelLinks> = ArrayList();
        //lvData?.adapter = LinkAdapter(listAccess_, myActivity);

        // se valdia si es administardor
        if(isAdmin){

            // activamos la opción
            manageActiveOptionFull_(myActivity.findViewById<LinearLayout?>(R.id.tabFormList), myActivity.findViewById<LinearLayout?>(R.id.tabProgramming), myActivity.findViewById<LinearLayout?>(R.id.tabUsers), myActivity.findViewById<LinearLayout?>(R.id.tabLocations));
        } else {

            // activamos la opción
            manageActiveOptionBasic_(
                myActivity.findViewById<LinearLayout?>(R.id.tabFormList),
                myActivity.findViewById<LinearLayout?>(R.id.tabMyProgramming)
            );
        }

        // Actualizamos el titulo del panel
        myActivity.findViewById<TextView?>(R.id.textMenuLayout).setText("Lista de formularios");

        // MOstramos loading para habilitar la ventana de selección de cuentas
        LoadingScreen.displayLoadingWithText(myActivity,"Consultando lista de formularios...",false);

        // definimos el objeto base
        val mainJson = JSONObject();
        mainJson.put("method", "getDirectLinks");

        // se realiza la petición
        HttpTask( {

            if (it == null) {
                println("Error de conexión");
            } else {

                // retornamos la respuesta
                var responseJson = JSONObject(it);

                // validamos si la respuesta es correcta
                if (responseJson.getBoolean("success")) {

                    // obtenemos los datos de respuesta
                    var returnArray = responseJson.getJSONArray("returnValue");

                    // recorremos cada uno de los vinculos
                    for (i in 0 until returnArray.length()) {

                        val item = returnArray.getJSONObject(i);
                        listAccess_?.add(ModelLinks(item.getString("pageName"), item.getString("pageLink")));
                    }
                }
            }
            println(it)
            // Ocultamos loading
            LoadingScreen.hideLoading();

            // validamos si no existe datos
            if(listAccess_.size == 0){

                // creamos una lista muteable para permitir agregar items
                var blankOptions_: MutableList<DefaultMessageObject> = ArrayList();

                // AGregamos la opción vacia -
                blankOptions_?.add(DefaultMessageObject("Sin información", "No existen formularios para mostrar."));

                // Actualizamos el adapatador
                lvData?.adapter = DefaultAdapter(blankOptions_, myActivity);
            } else {

                // creamos un adapter personalizado
                lvData?.adapter = LinkAdapter(listAccess_, myActivity);
            }
        } ).execute("POST", null, mainJson.toString());
    }

    /**
     * Permite intercambiar entre opciones activas
     */
    fun manageActiveOptionBasic_(elementActive: LinearLayout, elementNotActive: LinearLayout){

        // agregamos el color activo
        elementActive.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.purple_500));
        elementActive.findViewById<ImageView>(R.id.imageViewButtomTab).setColorFilter(Color.WHITE);
        elementActive.findViewById<TextView>(R.id.textOptionButtomTab).setTextColor(Color.WHITE);

        // eliminamos el color del que debe de quedar desactivo
        elementNotActive.setBackgroundColor(Color.TRANSPARENT);
        elementNotActive.findViewById<ImageView>(R.id.imageViewButtomTab).setColorFilter(ContextCompat.getColor(myActivity, R.color.gray_letter));
        elementNotActive.findViewById<TextView>(R.id.textOptionButtomTab).setTextColor(ContextCompat.getColor(myActivity, R.color.gray_letter));
    }

    /**
     * Permite inicializar el menu
     */
    fun initMenuToolbar(titleActivity: String, userUrl: String, displayName: String, callback: PopupMenu.OnMenuItemClickListener){

        // inicializamos el boton
        myActivity.findViewById<TextView?>(R.id.textMenuLayout).setText(titleActivity);

        // agregamos la imagen del usuario
        myActivity.findViewById<ImageView?>(R.id.imageMenuLayout)?.loadUrl(userUrl);
        myActivity.findViewById<TextView?>(R.id.textUserMenuLayout).setText(displayName);

        // Asignamos el evento de abrir la opción de cerrar sesión
        var cardViewBadge: CardView = myActivity.findViewById(R.id.contentBadgeToolbar);
        cardViewBadge.setOnClickListener {
            showPopupMenu(it, callback);
        };
    }

    /**
     * MOstramos el menu de cerrar sesión
     */
    fun showPopupMenu(view: View, callback: PopupMenu.OnMenuItemClickListener) {
        // inflate menu
        val popup = PopupMenu(myActivity, view);
        val inflater: MenuInflater = popup.getMenuInflater();
        inflater.inflate(R.menu.close_app_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(callback);
        popup.show();
    }

    /**
     * Permite mostrar el panel de gestión de programación
     */
    fun showManageProgramming(programData: MyProgramObject?) {

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(myActivity, ManageProgramActivity::class.java);

        // validamos si existen datos
        if(programData != null){

            // pasamos los datos
            intent?.putExtra("programData", programData.toString());
        }

        // Mostramos la actividad
        myActivity.startActivity(intent);

        // Cerramos esta actividad
        myActivity.finish();
    }

    /**
     * Permite obtener el valor de spinner
     */
    fun getSpinnerValue(spinnerField: Spinner): String{

        // definimos el valor de retorno
        var returnValue = spinnerField.selectedItem.toString();

        // reemplazmaos el texto ""
        returnValue = returnValue.replace("- Seleccione una opción -", "");

        // retornamos el valor
        return returnValue;
    }

    /**
     * Permite mostrar el panel de gestión de un usuario
     */
    fun showManageUser(userData: UserObject?) {

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(myActivity, ManageUserActivity::class.java);

        // validamos si existen datos
        if(userData != null){

            // pasamos los datos
            intent?.putExtra("userData", userData.toString());
        }

        // Mostramos la actividad
        myActivity.startActivity(intent);

        // Cerramos esta actividad
        myActivity.finish();
    }

    /**
     * Permite verificar el tipo de perfil, y si ya autorizo politicas
     */
    fun showLoginPage() {

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(myActivity, MainActivity::class.java);

        // Mostramos la actividad
        myActivity.startActivity(intent);

        // Cerramos esta actividad
        myActivity.finish();
    }

    /**
     * Permite acceder a la vista de detalles
     */
    fun showDetailsPage(programData: MyProgramObject) {

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(myActivity, DetailsActivity::class.java);

        // pasamos los datos
        intent?.putExtra("programData", programData.toString());

        // Mostramos la actividad
        myActivity.startActivity(intent);
    }

    /**
     * Permite acceder a la vista de realizar chckin
     */
    fun showCheckinPage(programData: MyProgramObject) {

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(myActivity, CheckInActivity::class.java);

        // pasamos los datos
        intent?.putExtra("programData", programData.toString());

        // Mostramos la actividad
        myActivity.startActivity(intent);

        // cerramosla actividad
        myActivity.finish();
    }

    /**
     * Permite acceder a la vista de realizar el registro del formulario CX
     */
    fun showFormCxPage(programData: MyProgramObject) {

        // realizamos la apertura de la actividad del formulario CX
        var intent: Intent? = Intent(myActivity, FormCXActivity::class.java);

        // pasamos los datos
        intent?.putExtra("programData", programData.toString());

        // Mostramos la actividad
        myActivity.startActivity(intent);

        // cerramosla actividad
        myActivity.finish();
    }

    /**
     * Permite acceder a la vista de realizar checkout
     */
    fun showCheckioutPage(programData: MyProgramObject) {

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(myActivity, CheckOutActivity::class.java);

        // pasamos los datos
        intent?.putExtra("programData", programData.toString());

        // Mostramos la actividad
        myActivity.startActivity(intent);

        // cerramosla actividad
        myActivity.finish();
    }

    /**
     * Permitedar forma a una fecha
     */
    fun formatDateDialog(year: Int, monthOfYear: Int, dayOfMonth: Int): String{

        // variables para meses y días
        var stringMonth = monthOfYear.toString();
        var stringDay = dayOfMonth.toString();

        // validazmos si los valores son menores a 10
        if(monthOfYear < 10){
            stringMonth = "0" + stringMonth;
        }
        if(dayOfMonth < 10){
            stringDay = "0" + stringDay;
        }

        // retonamos la fecha
        return stringMonth + "/" + stringDay + "/" + year.toString();
    }

    /**
     * Permite inicalizar un campo de fecha
     */
    fun initDatePicker(inputElem: EditText){

        // Creamos las variables a usar
        var mYear: Int = 0;
        var mMonth: Int = 0;
        var mDay: Int = 0;

        // Asignamos el evento al campo
        inputElem.setOnClickListener {

            // se valida si el valor es null
            if(mYear == 0) {

                // Obtenemos la fecha actual
                var c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
            }

            // Mostramos el selector de fecha
            val dpd = DatePickerDialog(myActivity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                inputElem.setText(formatDateDialog(year, monthOfYear + 1, dayOfMonth));

                // actualizamos los valores
                mYear = year;
                mMonth = monthOfYear ;
                mDay = dayOfMonth;

            }, mYear, mMonth, mDay);

            // Mostramos el selector
            dpd.show();
        }
    }

    /**
     * Permite inicalizar un campo de hora
     */
    fun initTimePicker(inputElem: EditText){

        // Creamos las variables a usar
        var mHour: Int = 0;
        var mMinute: Int = 0;

        // Asignamos el evento al campo de hora
        inputElem.setOnClickListener {

            // se valida si el valor es null
            if(mHour == 0) {

                // Obtenemos la fecha actual
                var c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);
            }

            // Mostramos el selector de fecha
            val tpd = TimePickerDialog (myActivity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

                var tempHourOfDay = hourOfDay.toString();
                var tempMinute = minute.toString();

                // validazmos si los valores son menores a 10
                if(hourOfDay < 10){
                    tempHourOfDay = "0" + tempHourOfDay;
                }
                if(minute < 10){
                    tempMinute = "0" + tempMinute;
                }

                // Display Selected date in textbox
                inputElem.setText(tempHourOfDay + ":" + tempMinute);

                // actualizamos los valores
                mHour = hourOfDay;
                mMinute = minute;

            }, mHour, mMinute, true);

            // Mostramos el selector
            tpd.show();
        }
    }

    /**
     * Permite establecer la lista de opciones de un spinner
     **/
    fun addOptionsInSpinner(spinner: Spinner, items: Array<String>, callback: ((spinner: Spinner, items: Array<String>, position: Int) -> Unit)?){

        // Se valida el elemento
        if (spinner != null) {
            val arrayAdapter = ArrayAdapter(myActivity, android.R.layout.simple_spinner_dropdown_item, items);
            spinner.adapter = arrayAdapter;

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                    // validamos si existe función de llamado
                    if(callback != null) {
                        callback(spinner, items, position);
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    /**
     * Permite validar los permisos
     */
    fun checkPermissions(): Boolean {

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {

            if (checkSinglePermission(Manifest.permission.ACCESS_COARSE_LOCATION) && checkSinglePermission(
                    Manifest.permission.ACCESS_FINE_LOCATION) && checkSinglePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                return true;
            } else {
                return false;
            }

        } else {
            if (checkSinglePermission(Manifest.permission.ACCESS_COARSE_LOCATION) && checkSinglePermission(
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     *Permite realizar la solicitud de permisos
     */
    fun sendRequestPermissions(){

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            Log.d("CALLBACK", "OKKK3333")
            // Si no hay permisos solicitarlos al usuario.
            ActivityCompat.requestPermissions(myActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), PERMISSION_ID);

        } else {
            Log.d("CALLBACK", "OKKK444")
            // Si no hay permisos solicitarlos al usuario.
            ActivityCompat.requestPermissions(myActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID);
        }
    }

    /**
     * Se valida los permisos por individual
     */
    fun checkSinglePermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            myActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Permite obtener los datos de ubicación
     */
    fun readCurrentLocation(mServiceConnection: ServiceConnection){

        // Se valida los permisos
        if (checkPermissions()){
            Log.d("CALLBACK", "OKKK111AAAA")
            if (isLocationEnabled()){
                Log.d("CALLBACK", "OKOKOKOK")

                // Se realiza la consulta de la ultima ubicación
                initializeService(mServiceConnection);
            } else {
                Toast.makeText(myActivity, "Activar ubicación", Toast.LENGTH_SHORT).show();
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                myActivity.startActivity(intent);
                myActivity.finish();
                Log.d("CALLBACK", "Activar ubicación")
            }
        } else {
            Log.d("CALLBACK", "OKKK111BBB")
            // Se soliicta que autorize permisos
            sendRequestPermissions();
        }
    }

    /**
     * Permite inicializar el servivio de consulta de ubicación
     */
    fun initializeService(mServiceConnection: ServiceConnection){

        // se compila el servicio
        var service = myActivity.bindService(Intent(myActivity, LocationUpdatesService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE);
        //mService?.requestLocationUpdates();
        Log.i("RUBEN", service.toString())
    }

    /**
     * Permite validar si tiene habilitado el GPS
     */
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = myActivity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        );
    }

    /**
     * Permite agregar la imagen
     */
    fun ImageView.loadUrl(url: String) {
        Picasso.get().load(url).transform(CircleTransform()).into(this)
    }

}