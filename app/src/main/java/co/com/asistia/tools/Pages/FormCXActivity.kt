package co.com.asistia.tools.Pages

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import co.com.asistia.tools.utils.LoadingScreen
import org.json.JSONObject

class FormCXActivity : AppCompatActivity() {

    // Variables globales
    lateinit var myActivity: FormCXActivity;
    lateinit var GlobalMethods: GlobalMethods;
    lateinit var spinnerProcessStatus: Spinner;
    lateinit var spinnerCxType: Spinner;
    lateinit var spinnerAbourtType: Spinner;
    lateinit var spinnerMotiveCXConsume: Spinner;
    lateinit var spinnerRemissionStatus: Spinner;
    lateinit var spinnerCancelMotive: Spinner;
    var txtDate: EditText? = null;
    var txtInitialHour: EditText? = null;
    var txtInitialHourCX: EditText? = null;
    var txtOtherCancelMotive: EditText? = null;
    var txtFinishHourCX: EditText? = null;
    var txtGeneralComments: EditText? = null;
    var txtPatientNameConfirm: EditText? = null;
    var radPerfectProcess: RadioGroup? = null;
    var radPrequirurgico: RadioGroup? = null;
    var radPrequirurgicoYes: RadioButton? = null;
    var radPrequirurgicoNot: RadioButton? = null;
    var radPerfectProcessYes: RadioButton? = null;
    var radPerfectProcessNot: RadioButton? = null;

    // Etiquetas
    var lblCancelMotive: TextView? = null;
    var lblOtherCancelMotive: TextView? = null;
    var lblFinishHourCX: TextView? = null;
    var lblPerfectProcess: TextView? = null;
    var lblMotiveCXConsume: TextView? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_cxactivity);

        // definimos la actividad actual
        myActivity = this;

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
        this.setTitle("Registrar datos de CX");

        // obtenemos los datos de la actividad anterir
        val beforeData = this.intent.extras;

        // Obtenemos la url
        val programData = JSONObject(beforeData!!.getString("programData"));

        // validamos que sea diferente de null
        if (programData != null) {

            // Agregamos el dato actual
            findViewById<TextView>(R.id.textFormCX_ColectiveCX).setText(programData.getString("colective"));
        }

        // Iniciamos cada uno de los campos
        spinnerProcessStatus = findViewById(R.id.sltFormCX_processStatus);
        spinnerCxType = findViewById(R.id.sltFormCX_CxType);
        spinnerAbourtType = findViewById(R.id.sltFormCX_abourtType);
        spinnerMotiveCXConsume = findViewById(R.id.sltFormCX_motiveCXConsume);
        spinnerRemissionStatus = findViewById(R.id.sltFormCX_remissionStatus);
        spinnerCancelMotive = findViewById(R.id.sltFormCX_cancelMotive);
        txtDate = findViewById(R.id.textFormCx_date);
        txtInitialHour = findViewById(R.id.textFormCx_initialHour);
        txtInitialHourCX = findViewById(R.id.textFormCx_initialHourCX);
        txtOtherCancelMotive = findViewById(R.id.textFormCx_otherCancelMotive);
        txtFinishHourCX = findViewById(R.id.textFormCx_finishHourCX);
        txtGeneralComments = findViewById(R.id.textFormCx_generalComments);
        txtPatientNameConfirm = findViewById(R.id.textFormCx_patientNameConfirm);
        radPrequirurgicoYes = findViewById(R.id.textFormCx_prequirurgicoYes);
        radPrequirurgicoNot = findViewById(R.id.textFormCx_prequirurgicoNot);
        radPerfectProcessYes = findViewById(R.id.textFormCx_perfectProcessYes);
        radPerfectProcessNot = findViewById(R.id.textFormCx_perfectProcessNot);
        lblCancelMotive = findViewById(R.id.lblFormCX_cancelMotive);
        lblOtherCancelMotive = findViewById(R.id.lblFormCx_otherCancelMotive);
        lblFinishHourCX = findViewById(R.id.lblFormCx_finishHourCX);
        lblPerfectProcess = findViewById(R.id.lblFormCx_perfectProcess);
        lblMotiveCXConsume = findViewById(R.id.lblFormCX_motiveCXConsume);
        radPerfectProcess = findViewById(R.id.textFormCx_perfectProcess);
        radPrequirurgico = findViewById(R.id.textFormCx_prequirurgico);

        // Iniciamos las listas
        val processStatusList = arrayOf("- Seleccione una opción -", "Cancelada", "Completa y Atendida", "Sin Consumo", "Con Novedades");
        GlobalMethods.addOptionsInSpinner(spinnerProcessStatus, processStatusList) { spinner: Spinner, items: Array<String>, pos: Int ->

            // Ocultamos los campos
            hideDefaultFields();

            // validamos si el texto seleccionado es ""
            if(items[pos] == "Cancelada"){

                // Mostramos los campos de cancelación
                showCancelFields();
            } else if(items[pos] == "Con Novedades"){

                // Mostramos los campos de novedades
                showNoveltyFields();
            } else if(items[pos] == "Sin Consumo"){

                // Mostramos los campos de Sin Consumo
                showNotConsumeFields();
            }
        };
        GlobalMethods.addOptionsInSpinner(spinnerCxType, arrayOf("- Seleccione una opción -", "Programada", "Urgencia"), null);
        GlobalMethods.addOptionsInSpinner(spinnerCancelMotive, arrayOf("- Seleccione una opción -", "Cirujano no llego", "Cancelaron la sala", "Paciente no apto", "No llego el instrumental", "No llegamos", "Tiempo de Espera Mayor a 3 Horas", "Otro"), null);
        GlobalMethods.addOptionsInSpinner(spinnerAbourtType, arrayOf("- Seleccione una opción -", "Unilateral", "Bilateral"), null);
        GlobalMethods.addOptionsInSpinner(spinnerMotiveCXConsume, arrayOf("- Seleccione una opción -", "Producto No Indicado", "Uso de material de la Competencia (otra casa)", "No Requiere ningún material", "No llega remisión o no esta completa"), null);
        GlobalMethods.addOptionsInSpinner(spinnerRemissionStatus, arrayOf("- Seleccione una opción -", "Sin Novedad", "Desordenada Físicamente", "Llegó con Faltantes", "Implantes Trocados", "No coincide físico con la hoja de remisión", "Gradilla de tornillería trocada", "Gradilla de placas trocada"), null);

        // Asignamos el evento al selector de fecha
        GlobalMethods.initDatePicker(txtDate!!);
        GlobalMethods.initTimePicker(txtInitialHour!!);
        GlobalMethods.initTimePicker(txtInitialHourCX!!);
        GlobalMethods.initTimePicker(txtFinishHourCX!!);

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
            registerFormCXInSheet(programData);
        }

        // Ocultamos los campos
        hideDefaultFields();
    }

    /**
     * Permite registrar la información del formulario de CX
     */
    @SuppressLint("MissingPermission")
    fun registerFormCXInSheet(programData: JSONObject){

        // obtenemos los valores
        var params: JSONObject = JSONObject();
        params.put("remissionNumber", programData.getString("colective"));
        params.put("processStatus", getSpinnerValue(spinnerProcessStatus));
        params.put("typeCX", getSpinnerValue(spinnerCxType));
        params.put("date", txtDate?.text);
        params.put("initialHour", txtInitialHour?.text);
        params.put("initialHourCX", txtInitialHourCX?.text);
        params.put("cancelMotive", getSpinnerValue(spinnerCancelMotive));
        params.put("otherCancelMotive", txtOtherCancelMotive?.text);
        params.put("finishHourCX", txtFinishHourCX?.text);
        params.put("generalComments", txtGeneralComments?.text);
        params.put("abourtType", getSpinnerValue(spinnerAbourtType));
        params.put("patientNameConfirm", txtPatientNameConfirm?.text);
        params.put("prequirurgico", getRadioValue(radPrequirurgico!!));
        params.put("motiveCXConsume", getSpinnerValue(spinnerMotiveCXConsume));
        params.put("remissionStatus", getSpinnerValue(spinnerRemissionStatus));
        params.put("perfectProcess", getRadioValue(radPerfectProcess!!));
        params.put("rowIndex", programData.getInt("rowIndex"));

        // validamos los campos obligatorios
        if(params.getString("remissionStatus").isNotEmpty() && params.getString("prequirurgico").isNotEmpty() && params.getString("patientNameConfirm").isNotEmpty() && params.getString("processStatus").isNotEmpty() && params.getString("typeCX").isNotEmpty() && params.getString("initialHour").isNotEmpty() && params.getString("initialHourCX").isNotEmpty() && params.getString("abourtType").isNotEmpty()) {

            // Obtenemos la validación de los campos dependientes
            var validateObject = validateFields(params);

            // Validamos si los campos ya estan diligenciados
            if(validateObject.getBoolean("success")){

                // mostramos la ventana de confirmación
                GlobalMethods.showConfirm( "Registro formulario CX","¿Seguro(a) desea registrar el formulario de CX?", DialogInterface.OnClickListener { dialogInterface, i ->

                    // MOstramos loading para habilitar la ventana de selección de cuentas
                    LoadingScreen.displayLoadingWithText(this, "Registrando información de CX...", false);

                    // definimos el objeto base
                    val mainJson = JSONObject();
                    mainJson.put("method", "registerFormCXInSheet");
                    mainJson.put("params", params);

                    // se realiza la petición
                    HttpTask({

                        // definimos un mensaje general
                        var alerText_ = "No fue posible registrar el formulario CX";
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
                        GlobalMethods.showAlert("Registro formulario CX", alerText_, DialogInterface.OnClickListener { dialogInterface, i ->

                            // Cerramos la vista actual
                            if (isCloseActivity) {

                                // realizamos la apertura de la actividad principal
                                var intent: Intent? = Intent(this, NormalActivity::class.java);

                                // Mostramos la actividad
                                startActivity(intent);

                                // Se llama el metodo de cerar sesión
                                finish();
                            }
                        });
                    }).execute("POST", null, mainJson.toString());
                });
            } else {
                GlobalMethods.showAlert("¡Aviso!", validateObject.getString("message"), null);
            }
        } else {
            GlobalMethods.showAlert("¡Aviso!", "Los campos con asterisco(*) son obligatorios.", null);
        }
    }

    /**
     * Permite validar los campos obligatorios
     */
    fun validateFields(params: JSONObject): JSONObject{

        // Obtenemos el valor de "processStatus"
        var processStatus = params.getString("processStatus");

        // Definimos el objeto a retornar
        var returnObject = JSONObject();
        returnObject.put("success", true);
        returnObject.put("message", "Los campos con asterisco(*) son obligatorios.");

        // se valida si la respuesta es "Con Novedades"
        if(processStatus == "Con Novedades"){

            // validamos si el campo de "La Cirugía Ocurrió De Forma Perfecta"
            if(params.getString("perfectProcess").isEmpty()){
                returnObject.put("success", false);
                returnObject.put("message", "El campo 'La Cirugía Ocurrió De Forma Perfecta' es obligatorio.");
            }
        } else if(processStatus == "Cancelada"){ //Cancelada

            // validamos si el campo de "Motivo De Cancelación"
            if(params.getString("cancelMotive").isEmpty()){
                returnObject.put("success", false);
                returnObject.put("message", "El campo 'Motivo De Cancelación' es obligatorio.");
            }

            // validamos si la respuest es "true"
            if(returnObject.getBoolean("success") && params.getString("cancelMotive") == "Otro" && params.getString("otherCancelMotive").isEmpty()){
                returnObject.put("success", false);
                returnObject.put("message", "El campo 'Otro Motivo De Cancelación' es obligatorio.");
            }
        } else if(processStatus == "Sin Consumo"){ // Sin Consumo

            // validamos si el campo de "Motivo Cx "Sin Consumo""
            if(params.getString("motiveCXConsume").isEmpty()){
                returnObject.put("success", false);
                returnObject.put("message", "El campo 'Motivo Cx Sin Consumo' es obligatorio.");
            }
        }

        // retornamos la respuesta
        return returnObject;
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
     * Permite obtener el valor de un radio button
     */
    fun getRadioValue(radioGroupField: RadioGroup): String{

        // definimos el valor de retorno
        var returnValue = "";
        var radioButtonId = radioGroupField!!.checkedRadioButtonId;

        // se valida si es diferente de null
        if(radioButtonId != null && radioButtonId != -1){
            println(radioButtonId)
            val radio: RadioButton = findViewById(radioButtonId);
            returnValue = radio?.text as String;
        }

        // retornamos el valor
        return returnValue;
    }

    /**
     * Mostramos los campos de sin consumo
     */
    fun showNotConsumeFields(){

        // variable que permite mostrar los campos
        var statusView = View.VISIBLE;

        // se muestra cada uno de los campos
        spinnerMotiveCXConsume?.visibility = statusView;
        lblMotiveCXConsume?.visibility = statusView;
    }

    /**
     * Mostramos los campos de novedades
     */
    fun showNoveltyFields(){

        // variable que permite mostrar los campos
        var statusView = View.VISIBLE;

        // se muestra cada uno de los campos
        radPerfectProcessYes?.visibility = statusView;
        radPerfectProcessNot?.visibility = statusView;
        lblPerfectProcess?.visibility = statusView;
    }

    /**
     * Mostramos los campos de cancelación
     */
    fun showCancelFields(){

        // variable que permite mostrar los campos
        var statusView = View.VISIBLE;

        // se muestra cada uno de los campos
        txtFinishHourCX?.visibility = statusView;
        txtOtherCancelMotive?.visibility = statusView;
        spinnerCancelMotive?.visibility = statusView;
        lblCancelMotive?.visibility = statusView;
        lblOtherCancelMotive?.visibility = statusView;
        lblFinishHourCX?.visibility = statusView;
    }

    /**
     * Ocultamos los campos
     */
    fun hideDefaultFields(){

        // variable que permite ocultar los campos
        var statusView = View.GONE;

        // se oculta cada uno de los campos
        radPerfectProcessYes?.visibility = statusView;
        radPerfectProcessNot?.visibility = statusView;
        spinnerMotiveCXConsume?.visibility = statusView;
        txtOtherCancelMotive?.visibility = statusView;
        spinnerCancelMotive?.visibility = statusView;
        lblCancelMotive?.visibility = statusView;
        lblOtherCancelMotive?.visibility = statusView;
        lblMotiveCXConsume?.visibility = statusView;
        lblPerfectProcess?.visibility = statusView;

        // reseteamos el valor
        spinnerCancelMotive?.setSelection(0);
        radPerfectProcessYes?.setChecked(false);
        radPerfectProcessNot?.setChecked(false);
        spinnerMotiveCXConsume?.setSelection(0);
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
        GlobalMethods.showConfirm("Mensaje de confirmación", "¿Esta seguro(a) que desea cancelar el registro del formulario CX?", DialogInterface.OnClickListener { dialogInterface, i ->

            // realizamos la apertura de la actividad principal
            var intent: Intent? = Intent(this, NormalActivity::class.java);

            // Mostramos la actividad
            startActivity(intent);

            // Se llama el metodo de cerar sesión
            finish();
        });
    }
}