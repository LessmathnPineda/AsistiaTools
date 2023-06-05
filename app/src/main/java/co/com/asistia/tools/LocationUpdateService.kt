package co.com.asistia.tools

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*


class LocationUpdatesService : Service() {

    private val EXTRA_STARTED_FROM_NOTIFICATION = "started_from_notification"
    private val mBinder = LocalBinder()

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = (1000 * 60 * 3).toLong();
    //private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000;

    /**
     * La tarifa más rápida para actualizaciones de ubicación activa. Las actualizaciones nunca serán más frecuentes
     * que este valor.
     */
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private val NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private var mChangingConfiguration = false

    /**
     * Contains parameters used by [com.google.android.gms.location.FusedLocationProviderApi].
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Devolución de llamada para cambios en la ubicación.
     */
    private var mLocationCallback: LocationCallback? = null;
    private var mServiceHandler: Handler? = null

    /**
     * La ubicación actual.
     */
    private var currentLocation: Location? = null;
    private val TAG = "CALLBACK";

    /**
     * Permite inicializar la clase
     */
    override fun onCreate() {
        super.onCreate();

        // Deshabilitamos todas las notificaciones
        NotificationManagerCompat.from(this).cancelAll();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult!!);

                locationResult?.let { mLocationResult ->
                    onNewLocation(mLocationResult.lastLocation)
                }
            }
        }
        //Log.i("RUBEN1", "onCreate Location")

        // Se crea el servivicio y se obtiene la ubicacion
        createLocationRequest();

        // Se crea un hilo
        /*val handlerThread = HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = Handler(handlerThread.looper);*/
    }

    /**
     * Permite iniciar el comando
     **/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "Location Service started")
        val startedFromNotification =
            intent?.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false)

        // Llegamos aquí porque el usuario decidió eliminar las actualizaciones de ubicación de la notificación.
        if (startedFromNotification!!) {
            println("FALLLLO")
            removeLocationUpdates()
            stopSelf()
        }
        // Le dice al sistema que no intente recrear el servicio después de haber sido asesinado.
        return Service.START_NOT_STICKY;
    }

    /**
     * Se captura los cambios realizados
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.e(TAG, "in onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.e(TAG, "in onBind()")
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }

    override fun onRebind(intent: Intent?) {
        Log.i(TAG, "in onRebind()")
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Last client unbound from service")

        // Llamado cuando el último cliente (MainActivity en caso de esta muestra) no alcanza de esto
        // Servicio. Si se llama a este método debido a un cambio de configuración en MainActivity, nosotros
        // hacer nada. De lo contrario, hacemos que este servicio sea un servicio en primer plano.
        if (!mChangingConfiguration) {
            Log.i(TAG, "Starting foreground service")

            // referenciamos al instancia
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager;

            // creamos la notificación
            val notifyService = serviceNotification(notificationManager);

            // Formamos a que el servivio este disponible simpre
            startForeground(NOTIFICATION_ID, notifyService);

            // Eliminamos la notificación sin perder la funcionalidad en segundo plano
            //notificationManager.cancel(NOTIFICATION_ID);
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mServiceHandler?.removeCallbacksAndMessages(null);
    }

    /**
     * Permite recibir las solicitides de actualizaciones
     */
    fun requestLocationUpdates() {
        Log.e(TAG, "Requesting location updates")
        startService(Intent(applicationContext, LocationUpdatesService::class.java));
        try {
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest!!,
                mLocationCallback!!,
                Looper.myLooper()!!
            );
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }

    }

    /**
     * Remueve la actualización de actualización
     */
    fun removeLocationUpdates() {
        Log.e(TAG, "Removing location updates")
        try {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback!!);
            stopSelf()
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission. Could not remove updates. $unlikely")
        }
    }

    /**
     * Obtenemos la ultima ubicación
     */
    private fun getLastLocation() {
        Log.e(TAG, "getLastLocation")
        try {
            mFusedLocationClient?.lastLocation
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        currentLocation = task.result
                    } else {
                        Log.e(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }

    /**
     * Se procesa la nueva ubicación
     */
    fun onNewLocation(location: Location) {
        Log.e(TAG,"New location: " + location)

        currentLocation = location;

        // Se valida si se ejecuta en segundo plano
        if (isAppIsInBackground(applicationContext)) {
            Log.e(TAG, "App In Background " + location.provider);
        } else {
            Log.e(TAG, "App In Foreground " + location.provider);
        }

        // se envia los datos a la interfaz
        val pushNotificationIntent = Intent("NotifyUserAsistia");
        pushNotificationIntent.putExtra("pinned_location_name", location.provider);
        pushNotificationIntent.putExtra("pinned_location_lat", location.latitude.toString());
        pushNotificationIntent.putExtra("pinned_location_long", location.longitude.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotificationIntent);
    }

    /**
     * Se crea la función que muestra la notificación cuando se esta consultando
     **/
    private fun serviceNotification(mNotificationManager: NotificationManager): Notification {

        // Get an instance of the Notification manager
        //val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager;

        //val intent = Intent(this, LocationUpdatesService::class.java);
        val intent: PendingIntent = Intent(this, LocationUpdatesService::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        }

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        //intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //val name = getString(R.string.app_name)
            val name = "Asistía";

            // Create the channel for the notification
            val mChannel = NotificationChannel(
                "location_service_channel",
                name,
                NotificationManager.IMPORTANCE_HIGH
            )

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager?.createNotificationChannel(mChannel);
        }

        val builder = NotificationCompat.Builder(this)
            .setContentTitle("Asistencia técnica")
            //.setContentText("Obteniendo datos de ubicación...")
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentIntent(intent)
            //.setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.logo_verde);
            builder.setColor(getResources().getColor(R.color.purple_500));
        } else {
            builder.setSmallIcon(R.drawable.logo_verde);
        }

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("location_service_channel") // Channel ID
        } else {
            builder.priority = Notification.PRIORITY_MIN;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            builder.priority = Notification.PRIORITY_MIN;
        }

        // Compilamos la notificaciones
        return builder.build();
        //return getNotifcationComponent("Asistía - Geolocalización", "Obteniendo datos de ubicación...");
    }

    /**
     * Permite crear una notificación
     */
    private fun getNotifcationComponent(title: String, body: String): Notification {

        //Este método muestra notificaciones compatibles con Android Api Level < 26 o >=26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Mostrar notificacion en Android Api level >=26
            val CHANNEL_ID = "location_service_channel";
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notification: Notification.Builder = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.logo_verde)
                .setAutoCancel(true)
            //NotificationManagerCompat.from(this).notify(1, notification.build())
            return notification.build();
        } else {

            //Mostrar notificación para Android Api Level Menor a 26
            val NOTIFICATION_CHANNEL_ID = "location_service_channel"
            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.logo_verde)
                    .setAutoCancel(true);
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            return notificationBuilder.build();
        }
    }

    /**
     * Permite crear la solicitud de ubicación
     */
    private fun createLocationRequest() {

        // Creamos el servivio para obtener la ubicación
        mLocationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // se obtiene la ultima ubicación
        getLastLocation();
        Log.e(TAG, "createLocationRequest")
    }

    inner class LocalBinder : Binder() {
        internal val service: LocationUpdatesService
            get() = this@LocationUpdatesService
    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true

        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.let {
                val runningProcesses = it.runningAppProcesses
                for (processInfo in runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.packageName) {
                                isInBackground = false
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isInBackground;
    }

}