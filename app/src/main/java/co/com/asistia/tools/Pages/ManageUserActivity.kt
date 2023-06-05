package co.com.asistia.tools.Pages

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import co.com.asistia.tools.HomeActivity
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import co.com.asistia.tools.utils.LoadingScreen
import org.json.JSONObject

class ManageUserActivity : AppCompatActivity() {

    // Variables globales
    lateinit var myActivity: ManageUserActivity;
    lateinit var GlobalMethods: GlobalMethods;
    lateinit var spinnerStatus: Spinner;
    lateinit var spinnerProfile: Spinner;
    lateinit var spinnerUserType: Spinner;
    var txtUserName: EditText? = null;
    var txtUserEmail: EditText? = null;

    // Obtenemos la url
    var userData: JSONObject? = null;

    // Definimos las lista de valores
    var statusList = arrayOf("- Seleccione una opción -", "Activo", "Inactivo");
    var profileList = arrayOf("- Seleccione una opción -", "Administrador", "Asistente técnico");
    var userTypeList = arrayOf("- Seleccione una opción -", "Master", "Sr", "Jr", "Evento");

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_user);

        // definimos la actividad actual
        myActivity = this;

        // iniciamos la clase
        GlobalMethods = GlobalMethods(this);

        // Inicializamos el toolbar
        var topAppBar = findViewById<View>(R.id.toolbarUser) as androidx.appcompat.widget.Toolbar;
        if(topAppBar != null){
            setSupportActionBar(topAppBar);
            topAppBar.bringToFront();
        }

        // Obtenemos el soporte de Toolbar
        var supportActionBar_ = getSupportActionBar();

        // Configuramos el toolbar
        supportActionBar_?.setDisplayShowHomeEnabled(true);
        supportActionBar_?.setDisplayHomeAsUpEnabled(true);
        supportActionBar_?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24_white);

        // Iniciamos cada uno de los campos
        spinnerStatus = findViewById(R.id.sltUserForm_userStatus);
        spinnerProfile = findViewById(R.id.sltUserForm_userProfile);
        spinnerUserType = findViewById(R.id.sltUserForm_userType);
        txtUserName = findViewById(R.id.textUserForm_userName);
        txtUserEmail = findViewById(R.id.textUserForm_userMail);

        // Iniciamos los selects
        GlobalMethods.addOptionsInSpinner(spinnerStatus, statusList, null);
        GlobalMethods.addOptionsInSpinner(spinnerProfile, profileList, null);
        GlobalMethods.addOptionsInSpinner(spinnerUserType, userTypeList, null);

        // obtenemos los datos de la actividad anterir
        val beforeData = this.intent.extras;

        // validamos que sea diferente de null
        if (beforeData != null) {

            // obtenemos los datos como objeto
            userData = JSONObject(beforeData!!.getString("userData"));

            // Agregamos los datos
            if (userData != null) {

                // adicionamos el valor en los selects
                addSelectValue(spinnerStatus, statusList, userData!!.getString("userStatus"));
                addSelectValue(spinnerProfile, profileList, userData!!.getString("userProfile"));
                addSelectValue(spinnerUserType, userTypeList, userData!!.getString("userType"));

                // Agregamso el texto a las demas opciones
                txtUserName?.setText(userData!!.getString("userName"));
                txtUserEmail?.setText(userData!!.getString("userMail"));
            }

            // Establecemos el titulo
            this.setTitle("Editar usuario");

        } else {
            // Establecemos el titulo
            this.setTitle("Crear usuario");
        }

        // refernciamos los campos del menu inferir
        var tabOptionFormCancel: LinearLayout = findViewById(R.id.tabOptionFormCancel);
        var tabOptionFormOk: LinearLayout = findViewById(R.id.tabOptionFormOk);
        findViewById<TextView>(R.id.textOptionButtomOkForm).setText("Registrar datos");

        // Creamos los eventos para obtener programaciones
        tabOptionFormCancel.setOnClickListener {
            // retornamos a la actividad anterior
            returnActivity();
        };

        // asignamos el evento al boton de checkin
        tabOptionFormOk?.setOnClickListener {

            // mostramos el menu de confirmación
            registerUserFormInSheet(userData);
        }
    }

    /**
     * Permite registrar la información del usuario
     */
    @SuppressLint("MissingPermission")
    fun registerUserFormInSheet(userData: JSONObject?){

        // obtenemos los valores
        var params: JSONObject = JSONObject();
        params.put("userName", txtUserName?.text);
        params.put("userMail", txtUserEmail?.text);
        params.put("userStatus", GlobalMethods.getSpinnerValue(spinnerStatus));
        params.put("userProfile", GlobalMethods.getSpinnerValue(spinnerProfile));
        params.put("userType", GlobalMethods.getSpinnerValue(spinnerUserType));

        // definimos el texto de confirmación
        var confirmText = "¿Seguro(a) desea crear este nuevo usuario?";

        // validamos si existen datos del usuario
        if(userData != null){

            // Actualizamos datos de etxto y valores a enviar
            confirmText = "¿Seguro(a) desea modificar este usuario?";
            params.put("userId", userData.getString("userId"));
            params.put("rowIndex", userData.getInt("rowIndex"));
        }

        println(params)
        // validamos los campos obligatorios
        if(params.getString("userName").isNotEmpty() && params.getString("userMail").isNotEmpty() && params.getString("userStatus").isNotEmpty() && params.getString("userProfile").isNotEmpty()) {

            // mostramos la ventana de confirmación
            GlobalMethods.showConfirm( "Gestión de usuarios",confirmText, DialogInterface.OnClickListener { dialogInterface, i ->

                // MOstramos loading para habilitar la ventana de selección de cuentas
                LoadingScreen.displayLoadingWithText(this, "Registrando información del usuario...", false);

                // definimos el objeto base
                val mainJson = JSONObject();
                mainJson.put("method", "saveUserDataInSheet");
                mainJson.put("params", params);

                // se realiza la petición
                HttpTask({

                    // definimos un mensaje general
                    var alerText_ = "No fue posible almacenar los datos del usuario";
                    var isCloseActivity = false;

                    // Se valida que no exista error
                    if (it != null) {
                        // retornamos la respuesta
                        var responseJson = JSONObject(it);

                        // validamos si la respuesta es correcta
                        if (responseJson.getBoolean("success")) {

                            // obtenemos los datos de respuesta
                            alerText_ = responseJson.getString("returnValue");
                            isCloseActivity = true;
                        }
                    }

                    // Ocultamos el loading
                    LoadingScreen.hideLoading();

                    // mostramos mensaje
                    GlobalMethods.showAlert("Gestión de usuarios", alerText_, DialogInterface.OnClickListener { dialogInterface, i ->

                        // Cerramos la vista actual
                        if (isCloseActivity) {

                            // realizamos la apertura de la actividad principal
                            var intent: Intent? = Intent(this, HomeActivity::class.java);

                            // pasamos los datos
                            intent?.putExtra("isUserPanel", true);

                            // Mostramos la actividad
                            startActivity(intent);

                            // Se llama el metodo de cerar sesión
                            finish();
                        }
                    });
                }).execute("POST", null, mainJson.toString());
            });
        } else {
            GlobalMethods.showAlert("¡Aviso!", "Los campos con asterisco(*) son obligatorios.", null);
        }
    }

    /**
     * Permite agregar el valor a un select
     */
    fun addSelectValue(spinner: Spinner, records: Array<String>, valueText: String) {

        // Obtenemos el indice del valor
        var indexSearch = records.indexOf(valueText);

        // validamos si el valor es diferente a -1
        if(indexSearch != -1){
            spinner.setSelection(indexSearch);
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                // retornamos a la actividad anterior
                returnActivity();
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Permite ejecutar un evento al oprimir la fecha de atras
     */
    override fun onBackPressed() {

        // retornamos a la actividad anterior
        returnActivity();
    }

    /**
     * Permite volver a la actividad anterior
     */
    fun returnActivity(){

        // mensaje de confirmación
        GlobalMethods.showConfirm("Mensaje de confirmación", "¿Esta seguro(a) que desea cancelar la creación o actualización de este usuario?", DialogInterface.OnClickListener { dialogInterface, i ->

            // realizamos la apertura de la actividad principal
            var intent: Intent? = Intent(this, HomeActivity::class.java);

            // pasamos los datos
            intent?.putExtra("isUserPanel", true);

            // Mostramos la actividad
            startActivity(intent);

            // Se llama el metodo de cerar sesión
            finish();
        });
    }
}