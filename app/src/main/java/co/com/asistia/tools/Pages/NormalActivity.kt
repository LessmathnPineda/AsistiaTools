package co.com.asistia.tools.Pages

import DefaultMessageObject
import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import co.com.asistia.tools.Adapters.*
import co.com.asistia.tools.CircleTransform
import co.com.asistia.tools.LocationUpdatesService
import co.com.asistia.tools.MainActivity
import co.com.asistia.tools.R
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.util.ArrayList


class NormalActivity : AppCompatActivity() {
    // Variables globales
    private lateinit var auth: FirebaseAuth;
    lateinit var GlobalMethods: GlobalMethods;
    // creating variable for searchview
    lateinit var searchView: SearchView;
    val PERMISSION_ID = 42;
    val BACKGROUND_LOCATION_PERMISSION_API_30_REQUEST_CODE = 200;
    private var broadcastReceiver: BroadcastReceiver? = null;
    // A reference to the service used to get location updates.
    private var mService: LocationUpdatesService? = null;
    // Tracks the bound state of the service.
    private var mBound: Boolean = false;
    lateinit var swipeRefreshLayout: SwipeRefreshLayout;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal);

        // Deshabilitamos todas las notificaciones
        NotificationManagerCompat.from(this).cancelAll();

        // iniciamos la clase
        GlobalMethods = GlobalMethods(this);

        // Incializamos la instancia con Firebase autentication y database realtime
        auth = Firebase.auth;
        val account = GoogleSignIn.getLastSignedInAccount(this);

        // Obtenemos la imagen del usuario
        val userPhotoUrl = account?.photoUrl;
        var userUrl = "https://ssl.gstatic.com/accounts/ui/avatar_2x.png";

        // validamos si No existe una imagen por defecto
        if(userPhotoUrl != null){
            userUrl = userPhotoUrl.toString();
        }

        // Referenciamos el listview
        var lvData: RecyclerView = findViewById(R.id.lvData);
        // configuramos el adapter
        lvData?.setHasFixedSize(true)
        lvData?.layoutManager = LinearLayoutManager(this);

        // creamos una lista muteable para permitir agregar items
        var blankOptions_: MutableList<DefaultMessageObject> = ArrayList();

        // AGregamos la opción vacia -
        blankOptions_?.add(DefaultMessageObject("Sin información", "No existen programaciones para mostrar"));

        // Actualizamos el adapatador
        lvData?.adapter = DefaultAdapter(blankOptions_, this);

        // Iniciamos el campo de busqueda
        searchView = findViewById(R.id.searchBarNormal);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutNormal);

        // Ocultamos por defecto el loading
        swipeRefreshLayout.isRefreshing = false;

        // Personalizamos el loading
        swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);

        // Inciamos el menu superior e inferior
        GlobalMethods.initMenuToolbar("Lista de formularios", userUrl, account?.givenName.toString(), PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
            // mostramos la ventana de confirmación
            signOutEvent();

            true;
        });
        GlobalMethods.initButtomMenuToolbar(account?.email.toString(), lvData, searchView, swipeRefreshLayout);

        // Obtenemos los datos de programaciones
        GlobalMethods.getMyProgramming(account?.email.toString(), lvData);

        // Inciamos el campos de busqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                // Aplicamos el filtro
                applyFilter(lvData, query!!);
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Aplicamos el filtro
                applyFilter(lvData, newText!!);
                return false
            }
        });

        // creamos una instancia para recibir la respuesta obtenida
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                Log.d("CALLBACK", "DARIO")
                Log.d("Dario", intent?.action.toString())
                when (intent?.action) {
                    "NotifyUserAsistia" -> {
                        try {
                            // Obtenemos los datos
                            val lat = intent.getStringExtra("pinned_location_lat");
                            val long = intent.getStringExtra("pinned_location_long");

                            // definimos los datos a enviar
                            val json = JSONObject();
                            json.put("latitude", lat);
                            json.put("longitude", long);
                            json.put("name", account?.displayName);
                            json.put("email", account?.email);

                            // definimos el objeto base
                            val mainJson = JSONObject();
                            mainJson.put("method", "updateLocationInSheet");
                            mainJson.put("params", json);

                            // se realiza la petición
                            HttpTask( {
                                if (it == null) {
                                    println("Error de conexión");
                                    return@HttpTask;
                                }
                            } ).execute("POST", null, mainJson.toString());
                        } catch (e: Exception) {
                            Log.d("ERROR_Register", e.toString())
                        }
                    }
                }
            }
        }

        //Preguntar si se tiene permisos.
        if (GlobalMethods.checkPermissions()){

            // permite obtener la ubicación
            GlobalMethods.readCurrentLocation(mServiceConnection);
        } else {
            // Se soliicta que autorize permisos
            GlobalMethods.sendRequestPermissions();
        }
    }

    /**
     * Permite aplicar el respectivo filtro
     */
    fun applyFilter(lvData: RecyclerView, query: String){

        // Obtenemos el adapter
        var adapterString = lvData?.adapter.toString();

        // validamos si el adaptador es el de por defecto
        if(adapterString.indexOf("MyProgramAdapter") != -1){
            (lvData?.adapter as MyProgramAdapter).getFilter().filter(query);
        } else if(adapterString.indexOf("LinkAdapter") != -1){
            (lvData?.adapter as LinkAdapter).getFilter().filter(query);
        }
    }

    /***
     * Cuando se detiene el activity
     */
    override fun onStop() {
        if (mBound) {
            // Desvane del servicio. Esto indica al servicio que esta actividad ya no es
            // en primer plano, y el servicio puede responder promocionando a un primer plano
            unbindService(mServiceConnection);
            mBound = false
        }
        super.onStop();
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("NotifyUserAsistia");

        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it);
            LocalBroadcastManager.getInstance(this).registerReceiver(it, intentFilter);
        }
    }

    /**
     * Monitorea el estado de la conexión al servicio.
     */
    private var mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: LocationUpdatesService.LocalBinder = service as LocationUpdatesService.LocalBinder;
            mService = binder.service;
            mBound = true;
            // Verifique que el usuario no haya revocado permisos yendo a Configuración.
            mService?.requestLocationUpdates();

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null;
            mBound = false;
        }
    }

    /**
     * Permite cerrar el servicios
     */
    private fun closeService(){

        // se destrute el servicio
        mService?.removeLocationUpdates();

        // Eliminamos el registro
        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it);
        }

        // se valida si esta activo el servivcio
        if (mBound) {
            // Desvane del servicio. Esto indica al servicio que esta actividad ya no es
            // en primer plano, y el servicio puede responder promocionando a un primer plano
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    /**
     * Permite Cerrar sesión
     **/
    fun signOutEvent (){

        // mostramos la ventana de confirmación
        GlobalMethods.showConfirm("Cerrar sesión","¿Seguro(a) desea cerrar sesión y dejar de obtener la ubicación?", DialogInterface.OnClickListener { dialogInterface, i ->

            // Eliminamos el servicio
            closeService();

            // Cerramos sesión para que permita elegir otra cuenta
            Firebase.auth.signOut();

            // Iniciamos la pagina de login
            showLoginPage();
        });

    }
    /**
     * Permite verificar el tipo de perfil, y si ya autorizo politicas
     */
    private fun showLoginPage() {

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(this, MainActivity::class.java);

        // Mostramos la actividad
        startActivity(intent);

        // Cerramos esta actividad
        finish();
    }


    /***
     * Permite recibir la respuesta despues de autorizar o rechazar los permisos
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_ID -> {

                for (i in 0 until permissions.size) {
                    val permission = permissions[i]
                    val granted = grantResults[i] === PackageManager.PERMISSION_GRANTED
                    if (permission == Manifest.permission.ACCESS_FINE_LOCATION && granted) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        ) {
                            //foreground location has been granted, ask user for access to location in background
                            requestBackgroundLocationPermissionApi30(
                                BACKGROUND_LOCATION_PERMISSION_API_30_REQUEST_CODE
                            )
                        }
                    }
                }
            }
            BACKGROUND_LOCATION_PERMISSION_API_30_REQUEST_CODE -> {

                // Se realiza la consulta de la ultima ubicación
                GlobalMethods.initializeService(mServiceConnection);
            }
        }
    }

    /**
     * Se solicita permisos se permisos de fondo
     */
    private fun requestBackgroundLocationPermissionApi30(backgroundLocationRequestCode: Int) {
        if (GlobalMethods.checkSinglePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Actualizar configuración de ubicación")
            .setMessage("Para obtener la ubicación en segundo plano se requiere de la autorización de permisos")
            .setPositiveButton("Actualizar configuración",
                DialogInterface.OnClickListener { dialog, which -> //this code will open app's settings so user can grant background location permission
                    val permissions = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    ActivityCompat.requestPermissions(this, permissions, backgroundLocationRequestCode);
                })
            .setNegativeButton("No gracias", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss();
                closePrompt();
            }).create().show();
    }

    /**
     * Se cierra la ventana
     */
    private fun closePrompt() {
        finish();
    }

}