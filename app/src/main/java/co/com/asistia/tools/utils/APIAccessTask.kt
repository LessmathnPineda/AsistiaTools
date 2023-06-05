package co.com.asistia.tools.utils


import android.os.AsyncTask;
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
/**
 * Clase para manejar las peticiones http
 */
class HttpTask(callback: (String?) -> Unit) : AsyncTask<String, Unit, String>()  {
    // Variable globales
    var callback = callback;
    val TIMEOUT = 6 * 60 * 1000; // 6 minutos como maximo de tiempo de espera

    override fun doInBackground(vararg params: String): String? {

        // Definimos la url por defecto
        //val baseUrl_ = "https://script.google.com/a/asistia.com.co/macros/s/AKfycbz9VsEffj-7oMLyvs84E7jwIh8T4A6Mfy18loGLxfO2/dev";
        val baseUrl_ = "https://script.google.com/macros/s/AKfycbzSarZ64loQzx30i0V_QFIkfoX8SomNFvkV2BY4xpYB9rvkV2PY/exec";

        //val url = URL(params[1]);
        val url = URL(baseUrl_);
        val httpClient = url.openConnection() as HttpURLConnection;
        httpClient.setReadTimeout(TIMEOUT);
        httpClient.setConnectTimeout(TIMEOUT);
        httpClient.requestMethod = params[0];
        httpClient.doOutput = true;
        httpClient.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // agregamos los datos de autenticación
        //var oAuthToken_ = "ya29.A0ARrdaM9_NM3dyFT4bSjEh0YLQi8vr3UIBUrN6rfzEjZohMx87xEDPqGn27ZwNUOudIdMxE40yFjvaGC5dBxc_aSm0uGQwmo4eTW_9rmqku4K8k_0jk591litA9Na4iJl2vKM9CsnVKUXlVIJZJwIq6QG-JH5HgYUNnWUtBVEFTQVRBU0ZRRl91NjFWZDd4LUFvVnFVbk1PTlBsZGNyemQ3QQ0165";
        //httpClient.setRequestProperty("Authorization", "Bearer " + oAuthToken_);

        try {
            if (params[0] == "POST") {
                // Estalecemos la propiedades unicas de un medoto POST
                //httpClient.instanceFollowRedirects = false;
                httpClient.doInput = true;
                httpClient.useCaches = false;

                // Se realiza la conexión
                httpClient.connect();
                val os = httpClient.getOutputStream();
                val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"));
                writer.write(params[2]);
                writer.flush();
                writer.close();
                os.close();
            }
            if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                val stream = BufferedInputStream(httpClient.inputStream);
                val data: String = readStream(inputStream = stream);
                return data;
            } else {
                println("ERROR_HTTP ${httpClient.responseCode}");
            }
        } catch (e: Exception) {
            println("ERROR_HTTP1 ${e.printStackTrace()}");
        } finally {
            httpClient.disconnect();
        }

        return null;
    }

    /**
     * Permite obtener los datos que retorna el webservices
     */
    fun readStream(inputStream: BufferedInputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { stringBuilder.append(it) }
        return stringBuilder.toString()
    }

    /**
     * Al finalizar el consumo del webservice se ejecuta la función del callback
     */
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result);
        callback(result);
    }
}