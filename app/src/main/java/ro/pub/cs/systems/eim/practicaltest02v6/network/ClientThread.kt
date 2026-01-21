package ro.pub.cs.systems.eim.practicaltest02v6.network

import android.util.Log
import android.widget.TextView
import ro.pub.cs.systems.eim.practicaltest02v6.general.Constants
import ro.pub.cs.systems.eim.practicaltest02v6.general.Utilities
import java.io.IOException
import java.net.Socket

class ClientThread(
    private val address: String,
    private val port: Int,
    private val moneda: String,
    private val responseTextView: TextView // Referinta la UI pentru a afisa rezultatul
) : Thread() {

    override fun run() {
        try {
            // 1. Deschidem conexiunea catre Server
            val socket = Socket(address, port)
            Log.v(Constants.TAG, "Connected to server: $address:$port")

            val reader = Utilities.getReader(socket)
            val writer = Utilities.getWriter(socket)

            // 2. Trimitem datele cerute de protocolul aplicatiei
            // Atentie: Ordinea trebuie sa fie aceeasi ca la citirea in Server (CommunicationThread)!
            writer.println(moneda)

            // 3. Citim raspunsul de la Server
            var newText: String?
            while (reader.readLine().also { newText = it } != null) {
                val finalMessage = newText

                // 4. Actualizam interfata grafica (UI)
                // Deoarece suntem pe un thread secundar, folosim .post {}
                responseTextView.post {
                    responseTextView.append(finalMessage + "\n")
                }
            }

            // Inchidem conexiunea
            socket.close()

        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "Client error: " + ioException.message)
            // Afisam eroarea si pe ecran pentru debug rapid la colocviu
            responseTextView.post {
                responseTextView.append("Error: ${ioException.message}\n")
            }
        }
    }
}