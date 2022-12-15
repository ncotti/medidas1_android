package medidas_1.lux

import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import medidas_1.lux.R


class MainActivity : AppCompatActivity() {

    private val canWriteSettings: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(this)

    private var enabled = true

    companion object {
        private const val REQUEST_CODE_WRITE_SETTINGS_PERMISSION = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Se piden los permisos de la aplicación
        if (this.canWriteSettings) {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        } else {
            startManageWriteSettingsPermission()
        }

        // Se inicializa el sensor de luz
        LightSensorManager.setHandler(updateLightValue)
        buttonEnable.setOnClickListener {
            if (this.enabled == true) {
                buttonEnable.text = "Enable"
                this.enabled = false
            }

            else {
                buttonEnable.text = "Disable"
                this.enabled = true
            }
        }
    }

    // Resto de estados de la App no se usan
    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // Función para leer el brillo actual de la pantalla. En desuso
    private fun readBrightness() : Int {
        return Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )
    }

    // Pedir permisos para escribir configuraciones del sistema
    private fun startManageWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${this.packageName}")
            ).let {
                startActivityForResult(it, REQUEST_CODE_WRITE_SETTINGS_PERMISSION)
            }
        }
    }

    private fun changeBrightness(brightness : Int) {
        if (this.enabled == true) {
            Settings.System.putInt(
                getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                brightness
            )
            val layoutpars: ViewGroup.LayoutParams
            layoutpars = this.getWindow().getAttributes()
            layoutpars.screenBrightness = brightness.toFloat() / 255
            getWindow().setAttributes(layoutpars)
        }
    }

    // Handler para cuando se detectó un nuevo dato del sensor de luz.
    val updateLightValue: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            var lux : Float
            var brightness : Int
            var tuple : Pair<Float, Int>

            tuple = inputMessage.obj as Pair<Float, Int>

            lux = tuple.first
            brightness = tuple.second

            labelLight.text = lux.toString()
            labelBrightness.text = brightness.toString()
            changeBrightness(brightness)
        }

    }

}