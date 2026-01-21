package ro.pub.cs.systems.eim.practicaltest02v6.network

import android.util.Log
import ro.pub.cs.systems.eim.practicaltest02v6.general.Constants
import ro.pub.cs.systems.eim.practicaltest02v6.general.Utilities
import ro.pub.cs.systems.eim.practicaltest02v6.general.BitcoinInformation
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.Socket
import java.time.LocalDateTime

class CommunicationThread(private val serverThread: ServerThread, private val socket: Socket) : Thread() {

    override fun run() {
        try {
            // 1. Obtinem fluxurile de intrare/iesire
            val requestReader = Utilities.getReader(socket)
            val responseWriter = Utilities.getWriter(socket)

            // 2. Citim cererea de la client (Orasul si Tipul Informatiei)
            val time  = LocalDateTime.now()

            // 3. Verificam Cache-ul (HashMap din ServerThread)
            var data = serverThread.getData()[time]

            if (data == null) {
                Log.i(Constants.TAG, "Data not in cache for $time. Fetching from web...")

                // 4. Cerinta 3b: Daca nu avem date, le descarcam cu OkHttp
                val client = OkHttpClient()
                val url = Constants.API_URL

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    // 5. Parsam JSON-ul primit de la OpenWeather
                    val content = JSONObject(responseBody)

                    // Structura JSON OpenWeather: main { temp, pressure, humidity }, wind { speed }, weather [ { main } ]
                    val main = content.getJSONObject("main")
                    val wind = content.getJSONObject("wind")
                    val weatherArray = content.getJSONArray("weather")
                    val weatherObject = weatherArray.getJSONObject(0)

                    val temperature = main.getString("temp")
                    val pressure = main.getString("pressure")
                    val humidity = main.getString("humidity")
                    val windSpeed = wind.getString("speed")
                    val condition = weatherObject.getString("main")

                    // 6. Cream obiectul si il salvam in Cache (Cerinta 3a)
                    val weatherForecastInformation = BitcoinInformation(
                        temperature, windSpeed, condition, pressure, humidity
                    )

                    Log.i(Constants.TAG, "Obiect creat cu succes: $weatherForecastInformation")
                    serverThread.setData(city, weatherForecastInformation)
                    data = weatherForecastInformation // Actualizam variabila locala
                }
            } else {
                Log.i(Constants.TAG, "Data found in cache for $city!")
            }

            // 7. Cerinta 3d: Trimitem raspunsul cu tot cu ETICHETA (Label)
            if (data != null) {
                val result = when (informationType) {
                    "all" -> data.toString()
                    // MODIFICARE: Adaugam textul explicativ inainte de valoare
                    "temperature" -> "Temperature: ${data.temperature} C"
                    "wind" -> "Wind Speed: ${data.windSpeed} m/s"
                    "condition" -> "Condition: ${data.condition}"
                    "humidity" -> "Humidity: ${data.humidity} %"
                    "pressure" -> "Pressure: ${data.pressure} hPa"
                    else -> "Wrong information type"
                }
                // Trimitem rezultatul
                responseWriter.println(result)
            }

        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "Data processing error: " + ioException.message)
        } catch (jsonException: JSONException) {
            Log.e(Constants.TAG, "JSON parsing error: " + jsonException.message)
        } finally {
            try {
                socket.close()
            } catch (ioException: IOException) {
                Log.e(Constants.TAG, "Error closing socket: " + ioException.message)
            }
        }
    }
}