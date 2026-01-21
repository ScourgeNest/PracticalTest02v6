package ro.pub.cs.systems.eim.practicaltest02v6.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import ro.pub.cs.systems.eim.practicaltest02v6.general.BitcoinInformation
import ro.pub.cs.systems.eim.practicaltest02v6.general.Constants
import ro.pub.cs.systems.eim.practicaltest02v6.general.Utilities
import java.io.IOException
import java.net.Socket
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

class CommunicationThread(private val serverThread: ServerThread, private val socket: Socket) : Thread() {

    override fun run() {
        try {
            // 1. Obtinem fluxurile de intrare/iesire
            val requestReader = Utilities.getReader(socket)
            val responseWriter = Utilities.getWriter(socket)

            // 2. Citim cererea de la client (Orasul si Tipul Informatiei)
            val time  = LocalDateTime.now()
            val moneda = requestReader.readLine()
            val unit: TemporalUnit = ChronoUnit.SECONDS

            // 3. Verificam Cache-ul (HashMap din ServerThread)
            var data = serverThread.getData()[moneda]

            if (data == null || data.time.plus(10, unit) < LocalDateTime.now()) {
                Log.i(Constants.TAG, "Data not in cache for $time. Fetching from web...")

                // 4. Cerinta 3b: Daca nu avem date, le descarcam cu OkHttp
                val client = OkHttpClient()
                val url = "${Constants.API_URL}$moneda"

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    // 5. Parsam JSON-ul primit de la Bitcoin
                    val content = JSONObject(responseBody)
                    Log.i(Constants.TAG, "Obiect creat cu succes: $content")

                    val inform = content.getJSONObject("Data")
                    val btc = inform.getJSONObject("BTC-$moneda")
                    val pret = btc.getString("VALUE")

                    Log.i(Constants.TAG, "Obiect creat cu succes: $pret")

                    val bitc = BitcoinInformation(LocalDateTime.now(), pret)

                        serverThread.setData(moneda, bitc)

//                    Log.i(Constants.TAG, "Obiect creat cu succes: $weatherForecastInformation")
//                    serverThread.setData(city, weatherForecastInformation)
//                    data = weatherForecastInformation // Actualizam variabila locala
                    data = bitc
                }
            } else {
                Log.i(Constants.TAG, "Data found in cache for $moneda!")
            }

            // 7. Cerinta 3d: Trimitem raspunsul cu tot cu ETICHETA (Label)
            if (data != null) {
                // Trimitem rezultatul
                responseWriter.println(data.value)
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