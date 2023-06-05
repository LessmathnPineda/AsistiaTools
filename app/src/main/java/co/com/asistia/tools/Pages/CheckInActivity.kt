package co.com.asistia.tools.Pages

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import co.com.asistia.tools.utils.LoadingScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.util.*


class CheckInActivity : AppCompatActivity() {

    // Variables globales
    var txtDate: EditText? = null;
    var txtTime:EditText? = null;
    lateinit var GlobalMethods: GlobalMethods;
    // declare a global variable of FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in);

        // iniciamos la clase
        GlobalMethods = GlobalMethods(this);

        // Inicializamos el toolbar
        var topAppBar = findViewById<View>(R.id.toolbarCheckin) as androidx.appcompat.widget.Toolbar;
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

        // Establcemos el titulo
        this.setTitle("Registro de ingreso");

        // obtenemos los datos de la actividad anterir
        val beforeData = this.intent.extras;

        // Obtenemos la url
        val programData = JSONObject(beforeData!!.getString("programData"));

        // validamos que sea diferente de null
        if (programData != null) {

            addTextInLabel(programData.getString("creationDate"), R.id.textCheckIn_creationDate, R.id.textTitleCheckIn_creationDate);
            addTextInLabel(programData.getString("client"), R.id.textCheckIn_client, R.id.textTitleCheckIn_client);
            addTextInLabel(programData.getString("companyAT"), R.id.textCheckIn_companyAT, R.id.textTitleCheckIn_companyAT);
            addTextInLabel(programData.getString("specialist"), R.id.textCheckIn_specialist, R.id.textTitleCheckIn_specialist);
            addTextInLabel(programData.getString("patientName"), R.id.textCheckIn_patientName, R.id.textTitleCheckIn_patientName);
        }

        // Iniciamos los elementos
        txtDate = findViewById(R.id.textCheckin_inputDate);
        txtTime = findViewById(R.id.textCheckin_inputTime);

        // Asignamos el evento al selector de fecha
        GlobalMethods.initDatePicker(txtDate!!);
        GlobalMethods.initTimePicker(txtTime!!);

        // refernciamos los campos del menu inferir
        var tabOptionFormCancel: LinearLayout = findViewById(R.id.tabOptionFormCancel);
        var tabOptionFormOk: LinearLayout = findViewById(R.id.tabOptionFormOk);
        findViewById<TextView>(R.id.textOptionButtomOkForm).setText("Registrar llegada");

        // Creamos los eventos para obtener programaciones
        tabOptionFormCancel.setOnClickListener {
            // retornamos a la actividad anterior
            returnActivity();
        };

        // asignamos el evento al boton de checkin
        tabOptionFormOk?.setOnClickListener {

            // mostramos el menu de confirmación
            registerCheckinInSheet(programData);
        }

        // in onCreate() initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Permite registrar la información de checkin
     */
    @SuppressLint("MissingPermission")
    fun registerCheckinInSheet(programData: JSONObject){

        // obtenemos los valores
        var txtDateValue = txtDate?.text;
        var txtTimeValue = txtTime?.text;

        // validamos si la fecha existe
        if(txtDateValue != null && txtDateValue.isEmpty() == false){

            // validamos si la hora existe
            if(txtTimeValue != null && txtTimeValue.isEmpty() == false){

                //Preguntar si se tiene permisos.
                if (GlobalMethods.checkPermissions()) {
                    Log.d("CALLBACK", "OKKK111")

                    fusedLocationClient.lastLocation .addOnSuccessListener { location ->
                        if (location != null) {

                            // mostramos la ventana de confirmación
                            GlobalMethods.showConfirm(
                                "Registro de llegada",
                                "¿Seguro(a) desea registrar la llegada?",
                                DialogInterface.OnClickListener { dialogInterface, i ->

                                    // MOstramos loading para habilitar la ventana de selección de cuentas
                                    LoadingScreen.displayLoadingWithText(
                                        this,
                                        "Registrando llegada...",
                                        false
                                    );

                                    // definimos los datos a enviar
                                    val json = JSONObject();
                                    json.put("rowIndex", programData.getInt("rowIndex"));
                                    json.put("stringDate", txtDateValue);
                                    json.put("stringTime", txtTimeValue);
                                    json.put("locationText", location.latitude.toString() + "," + location.longitude.toString());

                                    // definimos el objeto base
                                    val mainJson = JSONObject();
                                    mainJson.put("method", "registerCheckIn");
                                    mainJson.put("params", json);

                                    // se realiza la petición
                                    HttpTask({

                                        // definimos un mensaje general
                                        var alerText_ = "No fue posible registrar la llegada";
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

                                                // validamos si la respuesta es positiva
                                                if (returnObject.getBoolean("success")) {
                                                    isCloseActivity = true;
                                                }
                                            }
                                        }

                                        // Ocultamos el loading
                                        LoadingScreen.hideLoading();

                                        // mostramos mensaje
                                        GlobalMethods.showAlert(
                                            "Registro de llegada",
                                            alerText_,
                                            DialogInterface.OnClickListener { dialogInterface, i ->

                                                // Cerramos la vista actual
                                                if (isCloseActivity) {

                                                    // retornamos a la actividad anterior
                                                    returnActivity();
                                                }
                                            });
                                    }).execute("POST", null, mainJson.toString());
                                });
                        }
                    }


                } else {
                    // Se soliicta que autorize permisos
                    GlobalMethods.sendRequestPermissions();
                }

            } else {
                GlobalMethods.showAlert("¡Aviso!", "Por favor seleccione una hora para continuar.", null);
            }
        } else {
            GlobalMethods.showAlert("¡Aviso!", "Por favor seleccione una fecha para continuar.", null);
        }
    }

    /**
     * Permite agregar el valor de cada campo y si no existe no lo muestra
     **/
    fun addTextInLabel(valueText: String, contentLabel: Int, contentTitle: Int){

        // Insertamos el valor
        findViewById<TextView>(contentLabel).setText(valueText);

        // validamos si el valor no existe
        if(valueText == "" || valueText == null){
            findViewById<TextView>(contentLabel).visibility = View.GONE;
            findViewById<TextView>(contentTitle).visibility = View.GONE;
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

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(this, NormalActivity::class.java);

        // Mostramos la actividad
        startActivity(intent);

        // Se llama el metodo de cerar sesión
        finish();
    }
}