package ro.pub.cs.systems.eim.practicaltest02v6.network

import android.util.Log
import ro.pub.cs.systems.eim.practicaltest02v6.general.BitcoinInformation
import ro.pub.cs.systems.eim.practicaltest02v6.general.Constants
import java.io.IOException
import java.net.ServerSocket
import java.time.LocalDateTime

// MODIFICARE 1: Constructorul primeste acum portul (Int), nu EditText-ul
class ServerThread(private val port: Int) : Thread() {

    private var serverSocket: ServerSocket? = null

    // MODIFICARE 2 (Cerinta 3a): Cache-ul local
    // Cheia este orasul (String), Valoarea este obiectul meteo
    private val data = HashMap<String, BitcoinInformation>()

    fun startServer() {
        start()
        Log.v(Constants.TAG, "startServer() invoked on port $port")
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.message)
        }
    }

    // MODIFICARE 3: Metode sincronizate pentru lucrul cu Cache-ul
    // Este vital sa fie @Synchronized pentru ca mai multi clienti pot scrie/citi simultan
    @Synchronized
    fun setData(moneda: String, bitcoinInformation: BitcoinInformation) {
        this.data[moneda] = bitcoinInformation
    }

    @Synchronized
    fun getData(): HashMap<String, BitcoinInformation> {
        return this.data
    }

    override fun run() {
        try {
            // MODIFICARE 4: Deschidem socket-ul pe portul primit in constructor
            serverSocket = ServerSocket(port)

            while (!currentThread().isInterrupted) {
                Log.v(Constants.TAG, "Waiting for clients...")
                val socket = serverSocket!!.accept()
                Log.v(Constants.TAG, "A client has connected: " + socket.inetAddress)

                // MODIFICARE 5: Instantiem CommunicationThread
                // Ii dam socket-ul SI o referinta catre acest ServerThread ("this")
                val communicationThread = CommunicationThread(this, socket)
                communicationThread.start()
            }
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.message)
        }
    }
}