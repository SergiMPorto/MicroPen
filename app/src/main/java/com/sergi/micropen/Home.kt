package com.sergi.micropen

import android.app.ProgressDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.location.LocationManager

class Home : AppCompatActivity() {

    private lateinit var grabar: ImageView
    private lateinit var escribir: ImageView
    private lateinit var btnLocation: Button
    private lateinit var tvLocation: TextView
    private val countryToLanguageMap = mapOf(
        "AT" to "de", // Austria
        "BE" to "nl", // Bélgica
        "BG" to "bg", // Bulgaria
        "HR" to "hr", // Croacia
        "CY" to "el", // Chipre
        "CZ" to "cs", // República Checa
        "DK" to "da", // Dinamarca
        "EE" to "et", // Estonia
        "FI" to "fi", // Finlandia
        "FR" to "fr", // Francia
        "DE" to "de", // Alemania
        "GR" to "el", // Grecia
        "HU" to "hu", // Hungría
        "IE" to "en", // Irlanda
        "IT" to "it", // Italia
        "LV" to "lv", // Letonia
        "LT" to "lt", // Lituania
        "LU" to "fr", // Luxemburgo
        "MT" to "mt", // Malta
        "NL" to "nl", // Países Bajos
        "PL" to "pl", // Polonia
        "PT" to "pt", // Portugal
        "RO" to "ro", // Rumanía
        "SK" to "sk", // Eslovaquia
        "SI" to "sl", // Eslovenia
        "ES" to "es", // España
        "SE" to "sv", // Suecia
        "GB" to "en", // Reino Unido
        "IN" to "hi", // India
        "AE" to "ar", // Emiratos Árabes Unidos
        "SA" to "ar", // Arabia Saudita
        "CN" to "zh", // China
        "JP" to "ja"  // Japón
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        grabar = findViewById(R.id.Record)
        grabar.setOnClickListener {
            startActivity(Intent(this, Grabar::class.java))
        }

        escribir = findViewById(R.id.text)
        escribir.setOnClickListener {
            startActivity(Intent(this, Escribir::class.java))
        }
        tvLocation = findViewById(R.id.tvLocation)
        btnLocation = findViewById(R.id.localitation)
        btnLocation.setOnClickListener {
            requeriendoPermisoUbicaciion()
        }
    }
            /*CoroutineScope(Dispatchers.Main).launch {
                val location = LocationManager().getUserLocation(this@Home)
                location?.let { loc ->
                    // Dispatch geocoding to background thread
                    val addresses = withContext(Dispatchers.IO) {
                        val geocoder = Geocoder(this@Home, Locale.getDefault())
                        geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    }
                    // Continue on the main thread after getting addresses
                    if (addresses.isNullOrEmpty()) {
                        Toast.makeText(
                            this@Home,
                            "Sin dirección no disponible.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val address = addresses[0]
                        val countryCode = address.countryCode
                        val languageCode = countryToLanguageMap[countryCode]

                        if (languageCode != null) {
                            updateLanguage(languageCode)
                            tvLocation.text =
                                "${address.getAddressLine(0)} - El idioma de esta localización es: ${Locale(languageCode).displayName}"
                            Toast.makeText(
                                this@Home,
                                "Language set to: ${Locale(languageCode).displayName}",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@Home,
                                "Lenguaje no disponible para el país: $countryCode",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } ?: Toast.makeText(this@Home, "Localización no disponible.", Toast.LENGTH_LONG).show()
                 Toast.makeText(this@Home, "Asegurese de activar la ubicación en el dispositivo.", Toast.LENGTH_LONG).show()

            }
        }
    }*/

    private fun requeriendoPermisoUbicaciion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Necesitamos permisos para obtener la ubicación", Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            activarLocalizacion()
        }
    }

    private fun activarLocalizacion() {
        if (localizacionDisponible()) {
            obtenerLocalizacion()
        } else {
            Toast.makeText(this, "Por favor, activa la ubicación en el dispositivo.", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun localizacionDisponible(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun obtenerLocalizacion() {
        CoroutineScope(Dispatchers.Main).launch {
            val location = LocationManager().getUserLocation(this@Home)
            location?.let { loc ->
                processLocation(loc)
            } ?: run {
                Toast.makeText(this@Home, "Ubicación no disponible.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun processLocation(loc: Location) = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(this@Home, Locale.getDefault())
        val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
        withContext(Dispatchers.Main) {
            addresses?.firstOrNull()?.let { address ->
                val countryCode = address.countryCode
                updateUIWithLocationData(address, countryCode)
            } ?: Toast.makeText(this@Home, "Dirección no encontrada.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUIWithLocationData(address: Address, countryCode: String) {
        val languageCode = countryToLanguageMap[countryCode]
        languageCode?.let {
            updateLanguage(it)
            tvLocation.text = "${address.getAddressLine(0)} - El idioma de esta localización es: ${Locale(it).displayName}"
            Toast.makeText(this, "Idioma configurado en: ${Locale(it).displayName}", Toast.LENGTH_LONG).show()
        } ?: Toast.makeText(this, "Lenguaje no disponible para el código de país: $countryCode", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requeriendoPermisoUbicaciion()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                startActivity(Intent(this, Login::class.java))
                return true
            }

            R.id.nav_profile -> {
                startActivity(Intent(this, Perfil::class.java))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    /* private fun downloadLanguageModels() {
        val languageCodeList = TranslateLanguage.getAllLanguages()
        val totalLanguages = languageCodeList.size
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Descargando paquetes de idioma...")
            setCancelable(false)
            show()
        }
        var languagesDownloaded = 0
        languageCodeList.forEach { languageCode ->
            downloadModel(languageCode) { success ->
                if (success) {
                    languagesDownloaded++
                    progressDialog.progress = (languagesDownloaded.toFloat() / totalLanguages * 100).toInt()
                    if (languagesDownloaded == totalLanguages) {
                        progressDialog.dismiss()
                        makeText(this@Home, "Todos los paquetes de idioma se han descargado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun downloadModel(languageCode: String, onComplete: (Boolean) -> Unit) {
        val translatorOptions = TranslatorOptions.Builder().setSourceLanguage(languageCode).build()
        val translator = Translation.getClient(translatorOptions)
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
            .addOnSuccessListener {
                makeText(this@Home, "Paquete de idioma para $languageCode descargado con éxito", Toast.LENGTH_SHORT).show()
                onComplete(true)
            }
            .addOnFailureListener {
                makeText(this@Home, "Error al descargar paquete de idioma para $languageCode", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
    }*/

    fun updateLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        Toast.makeText(this, "Lenguage actualizado a ${locale.displayLanguage}.", Toast.LENGTH_SHORT)
            .show()
    }


    fun listAllLocales() {
        Locale.getAvailableLocales().forEach { locale ->
            if (!locale.country.isBlank()) {  // Filtramos para asegurarnos que tenga asignado un país
                println("Country: ${locale.displayCountry}, Language: ${locale.displayLanguage} - Country Code: ${locale.country}, Language Code: ${locale.language}")
            }
        }


    }
}