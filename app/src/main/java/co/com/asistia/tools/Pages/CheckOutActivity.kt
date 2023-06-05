package co.com.asistia.tools.Pages

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import co.com.asistia.tools.HomeActivity
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import co.com.asistia.tools.utils.LoadingScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


class CheckOutActivity : AppCompatActivity() {

    // Variables globales
    var txtDate: EditText? = null;
    var txtTime: EditText? = null;
    var tabOptionFormOk: LinearLayout? = null;
    var btnImageSelector: ImageButton? = null;
    var previewImageElement: ImageView? = null;
    var btnDeleteImage: TextView? = null;
    var btnDocumentSelector: ImageButton? = null;
    var previewDocumentElement: TextView? = null;
    var btnDeleteDocument: TextView? = null;
    lateinit var GlobalMethods: GlobalMethods;
    private lateinit var fusedLocationClient: FusedLocationProviderClient;

    // Variable para controlar permisos
    private val MY_PERMISSIONS: Int = 100;
    private val PHOTO_CODE = 200;
    private val SELECT_PICTURE = 300;
    private val SELECT_FILE = 400;

    // variable que almacena los datos de los archivo o imagenes creados
    var MY_PHOTO_SELECTED: JSONObject? = null;
    var MY_FILE_SELECTED: JSONObject? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out);

        // iniciamos la clase
        GlobalMethods = GlobalMethods(this);

        // Inicializamos el toolbar
        var topAppBar = findViewById<View>(R.id.toolbarCheckOut) as androidx.appcompat.widget.Toolbar;
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
        this.setTitle("Registro de salida");

        // obtenemos los datos de la actividad anterir
        val beforeData = this.intent.extras;

        // Obtenemos la url
        val programData = JSONObject(beforeData!!.getString("programData"));

        // validamos que sea diferente de null
        if (programData != null) {

            addTextInLabel(programData.getString("creationDate"), R.id.textCheckOut_creationDate, R.id.textTitleCheckIn_creationDate);
            addTextInLabel(programData.getString("client"), R.id.textCheckOut_client, R.id.textTitleCheckIn_client);
        }

        // Iniciamos los elementos
        txtDate = findViewById(R.id.textCheckOut_inputDate);
        txtTime = findViewById(R.id.textCheckOut_inputTime);
        btnImageSelector = findViewById(R.id.btnImageCheckOut);
        previewImageElement = findViewById(R.id.imageViewCheckOut);
        btnDeleteImage = findViewById(R.id.imageTextDeleteCheckOut);
        btnDocumentSelector = findViewById(R.id.btnDocumentCheckOut);
        previewDocumentElement = findViewById(R.id.documentTextViewCheckOut);
        btnDeleteDocument = findViewById(R.id.documentTextDeleteCheckOut);

        // Asignamos el evento al selector de fecha
        GlobalMethods.initDatePicker(txtDate!!);
        GlobalMethods.initTimePicker(txtTime!!);

        // asignamos el evento al boton de eliminar archivo
        btnDeleteDocument?.setOnClickListener {

            // Ocultamos opciones
            btnDeleteDocument!!.setVisibility(View.GONE);
            previewDocumentElement!!.setText("");
            previewDocumentElement!!.setVisibility(View.GONE);
            MY_FILE_SELECTED = null;
        }

        // asignamos el evento al boton de eliminar imagen
        btnDeleteImage?.setOnClickListener {

            // Ocultamos opciones
            btnDeleteImage!!.setVisibility(View.GONE);
            previewImageElement!!.setVisibility(View.GONE);
            previewImageElement!!.setImageDrawable(null);
            MY_PHOTO_SELECTED = null;
        }

        // refernciamos los campos del menu inferir
        var tabOptionFormCancel: LinearLayout = findViewById(R.id.tabOptionFormCancel);
        tabOptionFormOk= findViewById(R.id.tabOptionFormOk);
        findViewById<TextView>(R.id.textOptionButtomOkForm).setText("Registrar salida");

        // Creamos los eventos para obtener programaciones
        tabOptionFormCancel.setOnClickListener {
            // retornamos a la actividad anterior
            returnActivity();
        };

        // asignamos el evento al boton de checkin
        tabOptionFormOk?.setOnClickListener {

            // mostramos el menu de confirmación
            registerCheckoutInSheet(programData);
        }

        // asignamos el evento click al boton de seleccionar archivos e imagenes
        btnImageSelector!!.setOnClickListener(showSelectorImage);
        btnDocumentSelector!!.setOnClickListener(showSelectorFile);

        // in onCreate() initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // verificamos los permisos necesarioss
        if(verifyStoragePermission()) {
            btnImageSelector!!.setEnabled(true);
            tabOptionFormOk!!.setEnabled(true);
        } else {
            btnImageSelector!!.setEnabled(false);
            tabOptionFormOk!!.setEnabled(false);
        }
    }

    /**
     * Permite registrar la información de checkout
     */
    @SuppressLint("MissingPermission")
    fun registerCheckoutInSheet(programData: JSONObject){

        // obtenemos los valores
        var txtDateValue = txtDate?.text;
        var txtTimeValue = txtTime?.text;

        // validamos si la fecha existe
        if(txtDateValue != null && txtDateValue.isEmpty() == false){

            // validamos si la hora existe
            if(txtTimeValue != null && txtTimeValue.isEmpty() == false){

                //Preguntar si se tiene permisos.
                if (GlobalMethods.checkPermissions()) {

                    fusedLocationClient.lastLocation .addOnSuccessListener { location ->
                        if (location != null) {

                            // mostramos la ventana de confirmación
                            GlobalMethods.showConfirm( "Registro de salida", "¿Seguro(a) desea registrar la salida?",DialogInterface.OnClickListener { dialogInterface, i ->

                                // MOstramos loading para habilitar la ventana de selección de cuentas
                                LoadingScreen.displayLoadingWithText(this,"Registrando salida...",false);

                                // definimos los datos a enviar
                                val json = JSONObject();
                                json.put("rowIndex", programData.getInt("rowIndex"));
                                json.put("stringDate", txtDateValue);
                                json.put("stringTime", txtTimeValue);
                                json.put("locationText", location.latitude.toString() + "," + location.longitude.toString());

                                // validamos si existe datos de un archivo
                                if(MY_FILE_SELECTED != null){
                                    json.put("fileData", MY_FILE_SELECTED);
                                }
                                if(MY_PHOTO_SELECTED != null){
                                    json.put("photoData", MY_PHOTO_SELECTED);
                                }

                                // definimos el objeto base
                                val mainJson = JSONObject();
                                mainJson.put("method", "registerCheckOut");
                                mainJson.put("params", json);

                                // se realiza la petición
                                HttpTask({

                                    // definimos un mensaje general
                                    var alerText_ = "No fue posible registrar la salida";
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
                                        "Registro de salida",
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
     * Permite abrir el selector de archivos
     **/
    val showSelectorFile = View.OnClickListener { view ->

        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Seleccione un archivo"), SELECT_FILE);
    }

    /**
     * Permite abrir el selector de imagenes
     **/
    val showSelectorImage = View.OnClickListener { view ->

        // Definimos las opciones
        val option = arrayOf<CharSequence>("Tomar foto", "Elegir de galeria", "Cancelar")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this);
        builder.setTitle("Eleige una opción")
        builder.setItems(option, DialogInterface.OnClickListener { dialog, which ->
            if (option[which] === "Tomar foto") {
                openCamera();
            } else if (option[which] === "Elegir de galeria") {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                intent.type = "image/*";
                startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), SELECT_PICTURE);
            } else {
                dialog.dismiss();
                btnDeleteImage!!.setVisibility(View.GONE);
                previewImageElement!!.setVisibility(View.GONE);
                previewImageElement!!.setImageDrawable(null);
                MY_PHOTO_SELECTED = null;
            }
        });

        builder.show();
    }

    /**
     * Permite abrir la actividad de imagen
     */
    private fun openCamera() {

        // Se cera la vista para mostar la captura de una foto
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Se muestra la vista
        startActivityForResult(intent, PHOTO_CODE);
    }

    /**
     * Permite realizar una opción al seleccionar la imagen o al elegir una imagen
     **/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            println(data)
            when (requestCode) {
                PHOTO_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap;
                    previewImageElement!!.setImageBitmap(imageBitmap);
                    previewImageElement!!.setVisibility(View.VISIBLE);
                    btnDeleteImage!!.setVisibility(View.VISIBLE);

                    val STRING_LENGTH = 10;
                    val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

                    // definimos el nombre
                    val randomString = (1..STRING_LENGTH)
                        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                        .map(charPool::get)
                        .joinToString("");

                    // Agregamos los datos
                    MY_PHOTO_SELECTED = JSONObject();
                    MY_PHOTO_SELECTED!!.put("data", encodeBitmapImage(imageBitmap));
                    MY_PHOTO_SELECTED!!.put("name", randomString + ".jpg");
                    println(MY_PHOTO_SELECTED!!.getString("name"))
                }
                SELECT_PICTURE -> {
                    val path = data?.data;
                    val filename = getFileNameFromUri(this, path!!);

                    previewImageElement!!.setImageURI(path);
                    previewImageElement!!.setVisibility(View.VISIBLE);
                    btnDeleteImage!!.setVisibility(View.VISIBLE);

                    // Agregamos los datos
                    MY_PHOTO_SELECTED = JSONObject();
                    MY_PHOTO_SELECTED!!.put("data", fileUriToBase64(path));
                    MY_PHOTO_SELECTED!!.put("name", filename);
                }
                SELECT_FILE -> {
                    val path = data?.data;
                    val filename = getFileNameFromUri(this, path!!);

                    // validamos si el tamaños es valido
                    if(validateSizeFile(path!!)) {
                        btnDeleteDocument!!.setVisibility(View.VISIBLE);
                        previewDocumentElement!!.setText(filename);
                        previewDocumentElement!!.setVisibility(View.VISIBLE);

                        // Agregamos los datos
                        MY_FILE_SELECTED = JSONObject();
                        MY_FILE_SELECTED!!.put("data", fileUriToBase64(path));
                        MY_FILE_SELECTED!!.put("name", filename);
                    } else {

                        // Mostramos un alerta
                        GlobalMethods.showAlert("!Aviso¡", "Lo sentimos el archivo seleccionado supera las 24 MB de tamaño, por favor seleccione un archivo con menos peso.", null);
                    }
                }
            }
        }
    }

    /**
     * Validamos el tamaño
     */
    fun validateSizeFile(uri: Uri): Boolean{

        try {
            val megaBytes = (contentResolver.openInputStream(uri)?.readBytes()?.size?: 0) / 1024 / 1024;

            // se Validaq que no supere las 24 MB
            if(megaBytes < 24){
                return true;
            } else {
                return false;
            }
        } catch (e1: IOException) {}

        // Retornamos el valor de false
        return false;
    }

    /**
     * File uri to base64.
     */
    fun fileUriToBase64(uri: Uri): String? {

        try {
            val bytes = contentResolver.openInputStream(uri)?.readBytes();
            System.out.println("OKKK: " + (contentResolver.openInputStream(uri)?.readBytes()?.size?: 0))
            return "data:" + getMimeType(uri) + ";base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (e1: IOException) {}
        return ""
    }

    /**
     * Permite obtener el tipo de archivo
     */
    fun getMimeType(uri: Uri): String? {
        var mimeType: String? = "";
        mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver = this.getContentResolver()
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }

    /**
     *
     */
    private fun readBytes(uri: Uri, resolver: ContentResolver): ByteArray {
        // this dynamically extends to take the bytes you read
        val inputStream: InputStream? = resolver.openInputStream(uri)
        val byteBuffer = ByteArrayOutputStream()

        // this is storage overwritten on each iteration with bytes
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        // we need to know how may bytes were read to write them to the
        // byteBuffer
        var len = 0
        while (inputStream?.read(buffer).also { len = it!! } != -1) {
            byteBuffer.write(buffer, 0, len)
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray()
    }

    /**
     * Convertimos Bitmap a base de datos
     */
    private fun encodeBitmapImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        val b = baos.toByteArray();
        return "data:image/jpg;base64," + Base64.encodeToString(b, Base64.DEFAULT);
    }

    @SuppressLint("Range")
    /**
     * Obtenemos el nombre del archivo
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        val fileName: String?
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        fileName = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        cursor?.close()
        return fileName;
    }

    /**
     * Permite revisar si el usuario ya concedio permisos para seleccionar archivos o tomar una foto
     */
    private fun verifyStoragePermission(): Boolean {

        // Se valida si la versión de andriod es menor a la M,e s decir que no es necesario solicitar permisos, ya que al momento de instalar la aplicación concedio estos
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        // se chequea los permisos necesarios
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        // Se valida si un permiso esta pendiente por mostrar para que lo apruebe o lo deniege
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            )) {

            // referenciamos el contexto actual
            val contextView = findViewById<View>(android.R.id.content);

            // Mostramos un mensaje indicando la lista de permisos necesarios
            Snackbar.make(contextView, "Los permisos son necesarios para poder elegir la foto del jugador", Snackbar.LENGTH_INDEFINITE).setAction("AUTORIZAR") {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    ), MY_PERMISSIONS
                )
            }.show();
        } else {

            // se solicita los permisos necesarios
            requestPermissions(arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ), MY_PERMISSIONS);
        }

        // retornamos que aun no se ha concedido los permisos
        return false;
    }

    /**
     * Gestionar la respuesta post verificación de permisos
     **/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS) {
            if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Permisos aceptados", Toast.LENGTH_SHORT).show();

                // Habilita el boton
                btnImageSelector!!.setEnabled(true);
                tabOptionFormOk!!.setEnabled(true);
            }
        } else {
            showExplanation();
        }
    }

    /**
     * Permite mostrar una interfaz explicando el motivo de los permisos
     */
    private fun showExplanation() {
        val builder = AlertDialog.Builder(this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton(
            "Aceptar"
        ) { dialog, which ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri =
                Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton(
            "Cancelar"
        ) { dialog, which ->
            dialog.dismiss()
            finish()
        }
        builder.show();
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
