package co.com.asistia.tools

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import co.com.asistia.tools.Pages.NormalActivity
import co.com.asistia.tools.utils.GlobalMethods
import co.com.asistia.tools.utils.HttpTask
import co.com.asistia.tools.utils.LoadingScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    // Variables globales
    private lateinit var auth: FirebaseAuth;
    private lateinit var googleSignInClient: GoogleSignInClient;
    lateinit var buttonGoogle : Button;
    lateinit var GlobalMethods: GlobalMethods;

    override fun onCreate(savedInstanceState: Bundle?) {

        // Mostramos loading para habilitar la ventana de selección de cuentas
        LoadingScreen.displayLoadingWithText(this, "Validando acceso...", false);

        // Actualizamos el tema base
        setTheme(R.style.Theme_AsistíaTools);

        // Incializamos la instancia con Firebase autentication y database realtime
        auth = Firebase.auth;

        // Deshabilitamos todas las notificaciones
        NotificationManagerCompat.from(this).cancelAll();

        // iniciamos la clase
        GlobalMethods = GlobalMethods(this);

        // obtenemos el id del usuario
        val userId = auth?.uid;

        // validaos si existe el ID de usuario
        if(userId != null){

            // obtenemos la cuenta logueada
            val account = GoogleSignIn.getLastSignedInAccount(this);

            // Mostramos la página inicial
            showHomePage(account?.email.toString());
        } else {

            // Ocultamos loading
            LoadingScreen.hideLoading();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inicializamos el boton
        buttonGoogle = findViewById(R.id.btnGoogleSignIn);

        // Configurar el inicio de sesión de Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id_app))
            .requestEmail()
            .build();

        // Referenciamos el cliente para se uso del autenticador de Google
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // asignamos el evento al boton
        buttonGoogle.setOnClickListener{

            // MOstramos loading para habilitar la ventana de selección de cuentas
            LoadingScreen.displayLoadingWithText(this,"Habilitando selector de cuentas...",false);

            // Cerramos sesión para que permita elegir otra cuenta
            Firebase.auth.signOut();
            googleSignInClient.signOut();

            // iniciamos el actividad para mostrar y seleccionar cuentas
            resultLauncher.launch(googleSignInClient.signInIntent);
            LoadingScreen.hideLoading();
        };
    }

    /**
     * Permite verificar el tipo de perfil, y si ya autorizo politicas
     */
    private fun showHomePage(userEmail: String) {

        // definimos los datos a enviar
        val json = JSONObject();
        json.put("email", userEmail);

        // definimos el objeto base
        val mainJson = JSONObject();
        mainJson.put("method", "verifyRoleInApp");
        mainJson.put("params", json);

        // realizamos la apertura de la actividad principal
        var intent: Intent? = Intent(this, HomeActivity::class.java);

        // se realiza la petición
        HttpTask( {

            // definimos el estado
            var isShowAlert_ = true;

            if (it == null) {
                println("Error de conexión");
            } else {

                // retornamos la respuesta
                var responseJson = JSONObject(it);

                // validamos si la respuesta es correcta
                if (responseJson.getBoolean("success")) {

                    // obtenemos los datos de respuesta
                    var returnObject = responseJson.getJSONObject("returnValue");

                    // obtenemos el perfil
                    var profileName_ = returnObject.getString("profile");

                    // se valida si el perfil es difeente de "none"
                    if(profileName_ != null && profileName_ != "none") {

                        // se indica que todo esta bien y puede continuar
                        isShowAlert_ = false;

                        // se valida si el perfil es "normal"
                        if(profileName_ == "normal") {

                            // realizamos la apertura de la actividad principal
                            intent = Intent(this, NormalActivity::class.java);
                        }

                        // Ocultamos loading
                        LoadingScreen.hideLoading();

                        // Mostramos la actividad
                        startActivity(intent);

                        // Cerramos esta actividad
                        finish();
                    }
                }
            }

            // se valida si se debe de mostrar el error
            if(isShowAlert_){

                // Cerramos sesión para que permita elegir otra cuenta
                Firebase.auth.signOut();

                // Ocultamos loading
                LoadingScreen.hideLoading();

                // Mostramos alerta
                GlobalMethods.showAlertCustom("Acceso no autorizado", "El usuario con el que esta ingresando no tiene permisos para acceder a la aplicación.");
            }
            println(it)
        } ).execute("POST", null, mainJson.toString());

    }

    /**
     * Permite recibir la respuesta luego de iniciar sesión
     **/
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // There are no request codes
        if (result.resultCode == Activity.RESULT_OK) {

            // Mostramos loading para iniciar sesión
            LoadingScreen.displayLoadingWithText(this,"Iniciando sesión...",false);

            // There are no request codes
            val data: Intent? = result.data;

            // se crea una tarea patener los datos del inicio de sesión
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data);

            // obtenemos los datos relacionados al usuario activo
            handleSignInResult(task);
        } else {
            println("ERROR: " + result.resultCode)
        }
    }

    /**
     * Me permite cargara los datos de inicio de sesión
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java);

            // se valida si es distinta de null
            if(account != null) {

                // obtenemos la credencial para autenticarnos
                val credential = GoogleAuthProvider.getCredential(account.idToken, null);

                // Nos autenticamos en Firebase
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                    // Mostramos la ventana inicial
                    showHomePage(it.result.user?.email.toString());
                }
            }
        } catch (e: ApiException) {
            LoadingScreen.hideLoading();
        }
    }

    /**
     * Cremaos el evento que se ejecuta al momento de realizar clic
     **/
    val listenerEvents = View.OnClickListener { view ->
        when (view.getId()) {
            R.id.btnGoogleSignIn -> {
                //Toast.makeText(this, "Joolla", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Permite ejecutar un evento al oprimir la fecha de atras
     */
    override fun onBackPressed() {}
}