package ro.pub.cs.systems.eim.practicaltest02v6

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ro.pub.cs.systems.eim.practicaltest02v6.network.ClientThread
import ro.pub.cs.systems.eim.practicaltest02v6.network.ServerThread

class PracticalTest02v6MainActivity : AppCompatActivity() {

    // 1. Declaram variabilele pentru elementele vizuale
    private lateinit var serverPortEditText: EditText
    private lateinit var serverConnectButton: Button
    private lateinit var clientAddressEditText: EditText
    private lateinit var clientPortEditText: EditText
    private lateinit var clientMonedaEditText: EditText
    private lateinit var clientGetWeatherButton: Button
    private lateinit var clientResultTextView: TextView

    // 2. Referinta catre thread-ul Serverului (pentru a-l opri la final)
    private var serverThread: ServerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Legam codul de fisierul XML (asigura-te ca XML-ul tau are acest nume!)
        setContentView(R.layout.activity_practical_test02v6_main)

        // 3. Initializam controalele (le gasim dupa ID-urile din XML)
        serverPortEditText = findViewById(R.id.server_port_edit_text)
        serverConnectButton = findViewById(R.id.server_connect_button)

        clientAddressEditText = findViewById(R.id.client_address_edit_text)
        clientPortEditText = findViewById(R.id.client_port_edit_text)
        clientMonedaEditText = findViewById(R.id.client_moneda_edit_text)
        clientGetWeatherButton = findViewById(R.id.client_get_weather_button)
        clientResultTextView = findViewById(R.id.client_result_text_view)

        // -----------------------------------------------------------------------
        // LOGICA PENTRU SERVER (Partea de sus a ecranului)
        // -----------------------------------------------------------------------
        serverConnectButton.setOnClickListener {
            val serverPort = serverPortEditText.text.toString()

            if (serverPort.isEmpty()) {
                Toast.makeText(this, "Te rog introdu un port pentru server!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pornim serverul doar daca nu ruleaza deja
            if (serverThread == null || !serverThread!!.isAlive) {
                serverThread = ServerThread(serverPort.toInt())
                serverThread!!.startServer()
                Toast.makeText(this, "Server pornit pe portul $serverPort", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Serverul ruleaza deja!", Toast.LENGTH_SHORT).show()
            }
        }

        // -----------------------------------------------------------------------
        // LOGICA PENTRU CLIENT (Partea de jos a ecranului)
        // -----------------------------------------------------------------------
        clientGetWeatherButton.setOnClickListener {
            val clientAddress = clientAddressEditText.text.toString()
            val clientPort = clientPortEditText.text.toString()
            val city = clientMonedaEditText.text.toString()

            // Validari ca sa nu crape aplicatia daca uiti un camp gol
            if (clientAddress.isEmpty() || clientPort.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Completeaza toate campurile de la Client!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Resetam textul rezultatelor
            clientResultTextView.text = ""

            // Pornim un thread de client care se conecteaza la server
            val clientThread = ClientThread(
                clientAddress,
                clientPort.toInt(),
                city,
                informationType,
                clientResultTextView
            )
            clientThread.start()
        }
    }

    // Aceasta metoda se apeleaza cand inchizi aplicatia de tot
    override fun onDestroy() {
        Log.i(Constants.TAG, "[MAIN] Aplicatia se inchide, oprim serverul...")
        serverThread?.stopServer()
        super.onDestroy()
    }
}