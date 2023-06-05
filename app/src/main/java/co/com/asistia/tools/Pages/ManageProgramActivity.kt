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
import org.json.JSONArray
import org.json.JSONObject

class ManageProgramActivity : AppCompatActivity() {

    // Variables globales
    lateinit var myActivity: ManageProgramActivity;
    lateinit var GlobalMethods: GlobalMethods;
    lateinit var spinnerRegional: Spinner;
    lateinit var spinnerCities: Spinner;
    lateinit var spinnerClients: Spinner;
    lateinit var spinnerJj: Spinner;
    lateinit var spinnerOm: Spinner;
    lateinit var spinnerCover: Spinner;
    lateinit var spinnerPlanning: Spinner;
    lateinit var spinnerMonths: Spinner;
    lateinit var spinnerHouse: Spinner;
    lateinit var spinnerLineNeg: Spinner;
    lateinit var spinnerResponsibleEmail: Spinner;

    var textColective: EditText? = null;
    var textObservations: EditText? = null;
    var textCreationDate: EditText? = null;
    var textCreationTime: EditText? = null;
    var textSystem: EditText? = null;
    var textCompanyAT: EditText? = null;
    var textSpecialist: EditText? = null;
    var textPatientName: EditText? = null;
    var textPagador: EditText? = null;
    var textPatientCC: EditText? = null;
    var textAppoitmentNumber: EditText? = null;
    var textRequestNumber: EditText? = null;
    var textUrgenceHours: EditText? = null;
    var textConfirmEmailHour: EditText? = null;
    var textResponsibleName: EditText? = null;
    var labelResponsibleName: TextView? = null;

    // Definimos las lista de valores
    var regionalList: Array<String>? = null;
    var citiesList: Array<String>? = null;
    var clientsList: Array<String>? = null;
    var jjList: Array<String>? = null;
    var omList: Array<String>? = null;
    var coverList: Array<String>? = null;
    var planningList: Array<String>? = null;
    var monthsList: Array<String>? = null;
    var houseList: Array<String>? = null;
    var usersList: Array<String>? = null;
    var lineNegList = arrayOf("- Seleccione una opción -", "Trauma", "CMF", "Medicina Deportiva", "Reemplazos Articulares", "Otra");

    // Obtenemos la url
    var PROGRAM_DATA: JSONObject? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_program);

        // definimos la actividad actual
        myActivity = this;

        // iniciamos la clase
        GlobalMethods = GlobalMethods(this);

        // Inicializamos el toolbar
        var topAppBar = findViewById<View>(R.id.toolbarProgram) as androidx.appcompat.widget.Toolbar;
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
        spinnerRegional = findViewById(R.id.sltProgramForm_regional);
        spinnerCities = findViewById(R.id.sltProgramForm_city);
        spinnerClients = findViewById(R.id.sltProgramForm_client);
        spinnerJj = findViewById(R.id.sltProgramForm_systemJJ);
        spinnerOm = findViewById(R.id.sltProgramForm_systemOM);
        spinnerCover = findViewById(R.id.sltProgramForm_coberture);
        spinnerPlanning = findViewById(R.id.sltProgramForm_planning);
        spinnerMonths = findViewById(R.id.sltProgramForm_monthOne);
        spinnerHouse = findViewById(R.id.sltProgramForm_house);
        spinnerLineNeg = findViewById(R.id.sltProgramForm_lineNeg);
        spinnerResponsibleEmail = findViewById(R.id.sltProgramForm_responsibleEmail);

        textColective = findViewById(R.id.textProgramForm_colective);
        textObservations = findViewById(R.id.textProgramForm_observations);
        textCreationDate = findViewById(R.id.textProgramForm_creationDate);
        textCreationTime = findViewById(R.id.textProgramForm_creationTime);
        textSystem = findViewById(R.id.textProgramForm_system);
        textCompanyAT = findViewById(R.id.textProgramForm_companyAT);
        textSpecialist = findViewById(R.id.textProgramForm_specialist);
        textPatientName = findViewById(R.id.textProgramForm_patientName);
        textPagador = findViewById(R.id.textProgramForm_pagador);
        textPatientCC = findViewById(R.id.textProgramForm_patientCC);
        textAppoitmentNumber = findViewById(R.id.textProgramForm_appoitmentNumber);
        textRequestNumber = findViewById(R.id.textProgramForm_requestNumber);
        textUrgenceHours = findViewById(R.id.textProgramForm_urgenceHours);
        textConfirmEmailHour = findViewById(R.id.textProgramForm_confirmEmailHour);
        textResponsibleName = findViewById(R.id.textProgramForm_responsibleName);
        labelResponsibleName = findViewById(R.id.labelProgramForm_responsibleName);

        // Ocultamos el campos de nombre
        labelResponsibleName!!.visibility = View.GONE;
        textResponsibleName!!.visibility = View.GONE;

        // Asignamos el evento al selector de fecha
        GlobalMethods.initDatePicker(textCreationDate!!);
        GlobalMethods.initTimePicker(textCreationTime!!);
        GlobalMethods.initTimePicker(textUrgenceHours!!);
        GlobalMethods.initTimePicker(textConfirmEmailHour!!);

        // Inciiamos la unica lista que conocemos el valor
        GlobalMethods.addOptionsInSpinner(spinnerLineNeg, lineNegList!!, null);

        // obtenemos los datos de la actividad anterir
        val beforeData = this.intent.extras;

        // validamos que sea diferente de null
        if (beforeData != null) {

            // obtenemos los datos como objeto
            PROGRAM_DATA = JSONObject(beforeData!!.getString("programData"));

            // Establecemos el titulo
            this.setTitle("Editar programación");

        } else {
            // Establecemos el titulo
            this.setTitle("Crear programación");
        }

        // Obtenemos los parametros
        getParameters();

        // refernciamos los campos del menu inferir
        var tabOptionFormCancel: LinearLayout = findViewById(R.id.tabOptionFormCancel);
        var tabOptionFormOk: LinearLayout = myActivity.findViewById(R.id.tabOptionFormOk);
        findViewById<TextView>(R.id.textOptionButtomOkForm).setText("Guardar programación");

        // Creamos los eventos para obtener programaciones
        tabOptionFormCancel.setOnClickListener {
            // retornamos a la actividad anterior
            returnActivity();
        };

        // asignamos el evento al boton de checkin
        tabOptionFormOk?.setOnClickListener {

            // mostramos el menu de confirmación
            registerProgramInSheet();
        }
    }

    /**
     * Permite registrar la información del usuario
     */
    @SuppressLint("MissingPermission")
    fun registerProgramInSheet(){

        // definimos la fecha de creación
        var creationDate_ = textCreationDate?.text.toString();

        // validamos si existe la hora y tambien la fecha
        //if(creationDate_!!.isNotEmpty()){

            // Obtenemos la fecha en el formato valido "MM/dd/yyyy"
            //var arrayDate = creationDate_.split("/");
            //creationDate_ = arrayDate[1] + "/" + arrayDate[0] + "/" + arrayDate[2];
        //}

        // validamos si existe la hora y tambien la fecha
        if(creationDate_!!.isNotEmpty() && textCreationTime?.text!!.isNotEmpty()){
            creationDate_ = creationDate_ + " " + textCreationTime?.text;
        }

        // Obtenemos el correo del usuario
        var userEmail_ = GlobalMethods.getSpinnerValue(spinnerResponsibleEmail);

        // validamos que no sea vacio el valor
        if(userEmail_.isNotEmpty()){

            // obtenemos el nombre como array
            var arrayName_ = userEmail_.split("/");

            // validamos qu en efecto exista un nombre
            if(arrayName_ != null && arrayName_.size > 0){
                userEmail_ = arrayName_[0];
            }
        }

        // obtenemos los valores
        var params: JSONObject = JSONObject();
        params.put("colective", textColective?.text);
        params.put("regional", GlobalMethods.getSpinnerValue(spinnerRegional));
        params.put("observations", textObservations?.text);
        params.put("city", GlobalMethods.getSpinnerValue(spinnerCities));
        params.put("creationDate", creationDate_);
        params.put("client", GlobalMethods.getSpinnerValue(spinnerClients));
        params.put("systemJJ", GlobalMethods.getSpinnerValue(spinnerJj));
        params.put("systemOM", GlobalMethods.getSpinnerValue(spinnerOm));
        params.put("system", textSystem?.text);
        params.put("companyAT", textCompanyAT?.text);
        params.put("specialist", textSpecialist?.text);
        params.put("patientName", textPatientName?.text);
        params.put("pagador", textPagador?.text);
        params.put("lineNeg", GlobalMethods.getSpinnerValue(spinnerLineNeg));
        params.put("house", GlobalMethods.getSpinnerValue(spinnerHouse));
        params.put("patientCC", textPatientCC?.text);
        params.put("appoitmentNumber", textAppoitmentNumber?.text);
        params.put("requestNumber", textRequestNumber?.text);
        params.put("monthOne", GlobalMethods.getSpinnerValue(spinnerMonths));
        params.put("urgenceHours", textUrgenceHours?.text);
        params.put("coberture", GlobalMethods.getSpinnerValue(spinnerCover));
        params.put("confirmEmailHour", textConfirmEmailHour?.text);
        params.put("planning", GlobalMethods.getSpinnerValue(spinnerPlanning));
        params.put("responsibleEmail", userEmail_);
        params.put("responsibleName", textResponsibleName?.text);

        // definimos el texto de confirmación
        var confirmText = "¿Seguro(a) desea crear esta nueva programación?";

        // validamos si existen datos del usuario
        if(PROGRAM_DATA != null){

            // Actualizamos datos de etxto y valores a enviar
            confirmText = "¿Seguro(a) desea modificar esta programación?";
            params.put("rowIndex", PROGRAM_DATA!!.getInt("rowIndex"));
        }

        // validamos los campos obligatorios
        if(params.getString("colective").isNotEmpty() && params.getString("regional").isNotEmpty() && params.getString("city").isNotEmpty() && params.getString("creationDate").isNotEmpty() && params.getString("client").isNotEmpty() && params.getString("companyAT").isNotEmpty() && params.getString("specialist").isNotEmpty() && params.getString("patientName").isNotEmpty() && params.getString("pagador").isNotEmpty() && params.getString("lineNeg").isNotEmpty() && params.getString("house").isNotEmpty() && params.getString("appoitmentNumber").isNotEmpty() && params.getString("requestNumber").isNotEmpty() && params.getString("monthOne").isNotEmpty()) {

            // mostramos la ventana de confirmación
            GlobalMethods.showConfirm( "Gestión de programación",confirmText, DialogInterface.OnClickListener { dialogInterface, i ->

                // MOstramos loading para habilitar la ventana de selección de cuentas
                LoadingScreen.displayLoadingWithText(this, "Registrando programación...", false);

                // definimos el objeto base
                val mainJson = JSONObject();
                mainJson.put("method", "saveProgrammingDataInSheet");
                mainJson.put("params", params);

                // se realiza la petición
                HttpTask({

                    // definimos un mensaje general
                    var alerText_ = "No fue posible almacenar los datos de la programación";
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
                    GlobalMethods.showAlert("Gestión de programación", alerText_, DialogInterface.OnClickListener { dialogInterface, i ->

                        // Cerramos la vista actual
                        if (isCloseActivity) {

                            // realizamos la apertura de la actividad principal
                            var intent: Intent? = Intent(this, HomeActivity::class.java);

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

    /***
     * Permite obtener la lista de parametros
     */
    fun getParameters(){

        // MOstramos loading para habilitar la ventana de selección de cuentas
        LoadingScreen.displayLoadingWithText(myActivity,"Obteniendo parámetros...",false);

        // definimos el objeto base
        val mainJson = JSONObject();
        mainJson.put("method", "getParameters");

        // se realiza la petición
        HttpTask( {

            // definimos un mensaje general
            var paramsObject_ = JSONObject("{\"regional\":[],\"cities\":[],\"clients\":[],\"jj\":[],\"om\":[],\"house\":[],\"cover\":[],\"planning\":[],\"months\":[],\"asignStatus\":[]}");

            // Se valida que no exista error
            if (it != null) {
                // retornamos la respuesta
                var responseJson = JSONObject(it);

                // validamos si la respuesta es correcta
                if (responseJson.getBoolean("success")) {

                    // obtenemos los datos de respuesta
                    paramsObject_ = responseJson.getJSONObject("returnValue");
                }
            }

            // obtenemos la lista de valores
            regionalList = createArrayParameters(paramsObject_.getJSONArray("regional"));
            citiesList = createArrayParameters(paramsObject_.getJSONArray("cities"));
            clientsList = createArrayParameters(paramsObject_.getJSONArray("clients"));
            jjList = createArrayParameters(paramsObject_.getJSONArray("jj"));
            omList = createArrayParameters(paramsObject_.getJSONArray("om"));
            coverList = createArrayParameters(paramsObject_.getJSONArray("cover"));
            planningList = createArrayParameters(paramsObject_.getJSONArray("planning"));
            monthsList = createArrayParameters(paramsObject_.getJSONArray("months"));
            houseList = createArrayParameters(paramsObject_.getJSONArray("house"));
            usersList = createArrayParameters(paramsObject_.getJSONArray("users"));

            // Iniciamos las lsitas de valores
            GlobalMethods.addOptionsInSpinner(spinnerRegional, regionalList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerCities, citiesList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerClients, clientsList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerJj, jjList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerOm, omList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerCover, coverList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerPlanning, planningList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerMonths, monthsList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerHouse, houseList!!, null);
            GlobalMethods.addOptionsInSpinner(spinnerResponsibleEmail, usersList!!) { spinner: Spinner, items: Array<String>, pos: Int ->

                // obtenemos el valor
                var selectedValue_ = items[pos].toString().replace("- Seleccione una opción -", "");

                // validamos si exiset un dato seleccionado
                if(selectedValue_.isNotEmpty()){
                    labelResponsibleName!!.visibility = View.VISIBLE;
                    textResponsibleName!!.visibility = View.VISIBLE;

                    // obtenemos el nombre como array
                    var arrayName = selectedValue_.split("/");

                    // validamos qu en efecto exista un nombre
                    if(arrayName != null && arrayName.size > 1){
                        selectedValue_ = arrayName[1];
                    }
                } else {
                    // Ocultamos el campos de nombre
                    labelResponsibleName!!.visibility = View.GONE;
                    textResponsibleName!!.visibility = View.GONE;
                }

                // se valida si existe algun valor elegido
                textResponsibleName!!.setText(selectedValue_);
            };

            // validamos si existen los datos para edición
            if(PROGRAM_DATA != null){

                // Se agregan los datos
                loadDataInForm(PROGRAM_DATA!!);
            }

            // Ocultamos el loading
            LoadingScreen.hideLoading();
        } ).execute("POST", null, mainJson.toString());
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
     * Permite adicionar los datos respectivo
     */
    fun loadDataInForm(programData: JSONObject){

        // adicionamos el valor en los selects
        addSelectValue(spinnerRegional, regionalList!!, programData!!.getString("regional"));
        addSelectValue(spinnerCities, citiesList!!, programData!!.getString("city"));
        addSelectValue(spinnerClients, clientsList!!, programData!!.getString("client"));
        addSelectValue(spinnerJj, jjList!!, programData!!.getString("systemJJ"));
        addSelectValue(spinnerOm, omList!!, programData!!.getString("systemOM"));
        addSelectValue(spinnerCover, coverList!!, programData!!.getString("coberture"));
        addSelectValue(spinnerPlanning, planningList!!, programData!!.getString("planning"));
        addSelectValue(spinnerMonths, monthsList!!, programData!!.getString("monthOne"));
        addSelectValue(spinnerHouse, houseList!!, programData!!.getString("house"));
        addSelectValue(spinnerLineNeg, lineNegList!!, programData!!.getString("lineNeg"));

        // Agregamso el texto a las demas opciones
        textColective?.setText(programData!!.getString("colective"));
        textObservations?.setText(programData!!.getString("observations"));
        textCreationDate?.setText(programData!!.getString("onlyDate"));
        textCreationTime?.setText(programData!!.getString("onlyHour"));
        textSystem?.setText(programData!!.getString("system"));
        textCompanyAT?.setText(programData!!.getString("companyAT"));
        textSpecialist?.setText(programData!!.getString("specialist"));
        textPatientName?.setText(programData!!.getString("patientName"));
        textPagador?.setText(programData!!.getString("pagador"));
        textPatientCC?.setText(programData!!.getString("patientCC"));
        textAppoitmentNumber?.setText(programData!!.getString("appoitmentNumber"));
        textRequestNumber?.setText(programData!!.getString("requestNumber"));
        textUrgenceHours?.setText(programData!!.getString("urgenceHours"));
        textConfirmEmailHour?.setText(programData!!.getString("confirmEmailHour"));

        // Obtenemos el nombre del usuario
        var responsibleName_ = programData!!.getString("responsibleName");

        // validamos si esta diligenciado
        if(responsibleName_.isNotEmpty()){

            // Agregamos el valor del responsable
            addSelectValue(spinnerResponsibleEmail, usersList!!, programData!!.getString("responsibleEmail") + "/" + responsibleName_);
            labelResponsibleName!!.visibility = View.VISIBLE;
            textResponsibleName!!.visibility = View.VISIBLE;
        }
        textResponsibleName?.setText(responsibleName_);
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

    /**
     * Permite crear una lista de opciones
     */
    fun createArrayParameters(items: JSONArray): Array<String>{

        val list = ArrayList<String>()
        list.add("- Seleccione una opción -");
        for (i in 0 until items.length()) {
            list.add(items.getString(i));
        }

        // retornamos la lista
        return list.toTypedArray();
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
        GlobalMethods.showConfirm("Mensaje de confirmación", "¿Esta seguro(a) que desea cancelar la creación o actualización de esta programación?", DialogInterface.OnClickListener { dialogInterface, i ->

            // realizamos la apertura de la actividad principal
            var intent: Intent? = Intent(this, HomeActivity::class.java);

            // Mostramos la actividad
            startActivity(intent);

            // Se llama el metodo de cerar sesión
            finish();
        });
    }
}