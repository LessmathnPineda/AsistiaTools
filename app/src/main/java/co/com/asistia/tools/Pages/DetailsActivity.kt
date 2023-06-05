package co.com.asistia.tools.Pages

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import co.com.asistia.tools.R
import org.json.JSONObject


class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details);

        // Inicializamos el toolbar
        var topAppBar = findViewById<View>(R.id.toolbarDetails) as androidx.appcompat.widget.Toolbar;
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
        this.setTitle("Detalles de la programación");

        // obtenemos los datos de la actividad anterir
        val beforeData = this.intent.extras;

        // Obtenemos la url
        val programData = JSONObject(beforeData!!.getString("programData"));

        // validamos que sea diferente de null
        if (programData != null) {

            // actualizamos los datos
            addTextInLabel(programData.getString("colective"), R.id.textDetail_colective, R.id.textTitleDetails_colective);
            addTextInLabel(programData.getString("regional"), R.id.textDetail_regional, R.id.textTitleDetails_regional);
            addTextInLabel(programData.getString("observations"), R.id.textDetail_observations, R.id.textTitleDetails_observations);
            addTextInLabel(programData.getString("city"), R.id.textDetail_city, R.id.textTitleDetails_city);
            addTextInLabel(programData.getString("creationDate"), R.id.textDetail_creationDate, R.id.textTitleDetails_creationDate);
            addTextInLabel(programData.getString("client"), R.id.textDetail_client, R.id.textTitleDetails_client);
            addTextInLabel(programData.getString("systemJJ"), R.id.textDetail_systemJJ, R.id.textTitleDetails_systemJJ);
            addTextInLabel(programData.getString("systemOM"), R.id.textDetail_systemOM, R.id.textTitleDetails_systemOM);
            addTextInLabel(programData.getString("system"), R.id.textDetail_system, R.id.textTitleDetails_system);
            addTextInLabel(programData.getString("companyAT"), R.id.textDetail_companyAT, R.id.textTitleDetails_companyAT);
            addTextInLabel(programData.getString("specialist"), R.id.textDetail_specialist, R.id.textTitleDetails_specialist);
            addTextInLabel(programData.getString("patientName"), R.id.textDetail_patientName, R.id.textTitleDetails_patientName);
            addTextInLabel(programData.getString("pagador"), R.id.textDetail_pagador, R.id.textTitleDetails_pagador);
            addTextInLabel(programData.getString("lineNeg"), R.id.textDetail_lineNeg, R.id.textTitleDetails_lineNeg);
            addTextInLabel(programData.getString("house"), R.id.textDetail_house, R.id.textTitleDetails_house);
            addTextInLabel(programData.getString("patientCC"), R.id.textDetail_patientCC, R.id.textTitleDetails_patientCC);
            addTextInLabel(programData.getString("appoitmentNumber"), R.id.textDetail_appoitmentNumber, R.id.textTitleDetails_appoitmentNumber);
            addTextInLabel(programData.getString("requestNumber"), R.id.textDetail_requestNumber, R.id.textTitleDetails_requestNumber);
            addTextInLabel(programData.getString("monthOne"), R.id.textDetail_monthOne, R.id.textTitleDetails_monthOne);
            addTextInLabel(programData.getString("urgenceHours"), R.id.textDetail_urgenceHours, R.id.textTitleDetails_urgenceHours);
            addTextInLabel(programData.getString("coberture"), R.id.textDetail_coberture, R.id.textTitleDetails_coberture);
            addTextInLabel(programData.getString("confirmEmailHour"), R.id.textDetail_confirmEmailHour, R.id.textTitleDetails_confirmEmailHour);
            addTextInLabel(programData.getString("planning"), R.id.textDetail_planning, R.id.textTitleDetails_planning);
            addTextInLabel(programData.getString("externSupport"), R.id.textDetail_externSupport, R.id.textTitleDetails_externSupport);
            addTextInLabel(programData.getString("responsibleEmail"), R.id.textDetail_responsibleEmail, R.id.textTitleDetails_responsibleEmail);
            addTextInLabel(programData.getString("responsibleName"), R.id.textDetail_responsibleName, R.id.textTitleDetails_responsibleName);
            addTextInLabel(programData.getString("assignDate"), R.id.textDetail_assignDate, R.id.textTitleDetails_assignDate);
            addTextInLabel(programData.getString("assignStatus"), R.id.textDetail_assignStatus, R.id.textTitleDetails_assignStatus);
            addTextInLabel(programData.getString("assignAuthorize"), R.id.textDetail_assignAuthorize, R.id.textTitleDetails_assignAuthorize);
            addTextInLabel(programData.getString("realArriveDate"), R.id.textDetail_realArriveDate, R.id.textTitleDetails_realArriveDate);
            addTextInLabel(programData.getString("arriveDate"), R.id.textDetail_arriveDate, R.id.textTitleDetails_arriveDate);
            addTextInLabel(programData.getString("arriveLocation"), R.id.textDetail_arriveLocation, R.id.textTitleDetails_arriveLocation);
            addTextInLabel(programData.getString("realExitDate"), R.id.textDetail_realExitDate, R.id.textTitleDetails_realExitDate);
            addTextInLabel(programData.getString("exitDate"), R.id.textDetail_exitDate, R.id.textTitleDetails_exitDate);
            addTextInLabel(programData.getString("exitLocation"), R.id.textDetail_exitLocation, R.id.textTitleDetails_exitLocation);
            addTextInLabel(programData.getString("supportImage"), R.id.textDetail_supportImage, R.id.textTitleDetails_supportImage);
            addTextInLabel(programData.getString("supportDoc"), R.id.textDetail_supportDoc, R.id.textTitleDetails_supportDoc);
            addTextInLabel(programData.getString("technicalName"), R.id.textDetail_technicalName, R.id.textTitleDetails_technicalName);
            addTextInLabel(programData.getString("remissionNumber"), R.id.textDetail_remissionNumber, R.id.textTitleDetails_remissionNumber);
            addTextInLabel(programData.getString("processStatus"), R.id.textDetail_processStatus, R.id.textTitleDetails_processStatus);
            addTextInLabel(programData.getString("typeCX"), R.id.textDetail_typeCX, R.id.textTitleDetails_typeCX);
            addTextInLabel(programData.getString("date"), R.id.textDetail_date, R.id.textTitleDetails_date);
            addTextInLabel(programData.getString("initialHour"), R.id.textDetail_initialHour, R.id.textTitleDetails_initialHour);
            addTextInLabel(programData.getString("initialHourCX"), R.id.textDetail_initialHourCX, R.id.textTitleDetails_initialHourCX);
            addTextInLabel(programData.getString("cancelMotive"), R.id.textDetail_cancelMotive, R.id.textTitleDetails_cancelMotive);
            addTextInLabel(programData.getString("otherCancelMotive"), R.id.textDetail_otherCancelMotive, R.id.textTitleDetails_otherCancelMotive);
            addTextInLabel(programData.getString("finishHourCX"), R.id.textDetail_finishHourCX, R.id.textTitleDetails_finishHourCX);
            addTextInLabel(programData.getString("generalComments"), R.id.textDetail_generalComments, R.id.textTitleDetails_generalComments);
            addTextInLabel(programData.getString("abourtType"), R.id.textDetail_abourtType, R.id.textTitleDetails_abourtType);
            addTextInLabel(programData.getString("patientNameConfirm"), R.id.textDetail_patientNameConfirm, R.id.textTitleDetails_patientNameConfirm);
            addTextInLabel(programData.getString("prequirurgico"), R.id.textDetail_prequirurgico, R.id.textTitleDetails_prequirurgico);
            addTextInLabel(programData.getString("motiveCXConsume"), R.id.textDetail_motiveCXConsume, R.id.textTitleDetails_motiveCXConsume);
            addTextInLabel(programData.getString("remissionStatus"), R.id.textDetail_remissionStatus, R.id.textTitleDetails_remissionStatus);
            addTextInLabel(programData.getString("perfectProcess"), R.id.textDetail_perfectProcess, R.id.textTitleDetails_perfectProcess);
        }

        // refernciamos los campos del menu inferir
        var tabOptionGoHome: LinearLayout = findViewById(R.id.tabOptionGoHome);

        // Creamos los eventos para salir
        tabOptionGoHome.setOnClickListener {
            // retornamos a la actividad anterior
            finish();
        };
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
                finish();
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Permite ejecutar un evento al oprimir la fecha de atras
     */
    override fun onBackPressed() {

        // Se llama el metodo de cerar sesión
        finish();
    }
}