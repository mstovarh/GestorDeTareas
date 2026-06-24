package com.example.gestordetareas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val locationPermissionCode = 100

    // Inicializa la pantalla del mapa.
    // Configura el botón de regreso y carga el fragmento de Google Maps
    // para que la aplicación pueda mostrar la ubicación del usuario o una ubicación de referencia.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val btnBackFromMap = findViewById<Button>(R.id.btnBackFromMap)

        btnBackFromMap.setOnClickListener {
            finish()
        }

        // Se obtiene el fragmento del mapa definido en activity_map.xml.
        // Cuando el mapa esté listo, se ejecutará automáticamente onMapReady().
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    // Se ejecuta cuando Google Maps ya está listo para usarse.
    // Guarda la instancia del mapa y comienza el proceso para obtener la ubicación actual.
    override fun onMapReady(map: GoogleMap) {
        // Se guarda la instancia del mapa para poder agregar marcadores,
        // mover la cámara y activar la ubicación del usuario.
        googleMap = map
        getCurrentLocation()
    }

    // Obtiene la ubicación actual del dispositivo usando los servicios de ubicación de Google.
    // Primero valida el permiso de ubicación y, si está concedido, intenta obtener la ubicación real.
    // Si la ubicación no está disponible o ocurre un error, activa el fallback con marcador de referencia.
    private fun getCurrentLocation() {
        // Antes de consultar la ubicación, se valida si el usuario concedió el permiso.
        // Si no existe permiso, la app lo solicita y detiene temporalmente el proceso.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
            return
        }

        // Activa el botón visual de ubicación dentro del mapa.
        googleMap.isMyLocationEnabled = true

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val cancellationTokenSource = CancellationTokenSource()

        // Se intenta obtener la ubicación actual con alta precisión.
        // En emuladores puede devolver null si no hay ubicación simulada configurada.
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->

            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)

                googleMap.clear()

                googleMap.addMarker(
                    MarkerOptions()
                        .position(currentLocation)
                        .title("Tu ubicación actual")
                )

                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)
                )
            } else {
                // Si el emulador o dispositivo no entrega ubicación,
                // se muestra un marcador de referencia para evitar una pantalla sin acción.
                showDefaultLocation()
            }

        }.addOnFailureListener {
            // Si ocurre un error al obtener la ubicación,
            // también se usa el fallback con marcador de referencia.
            showDefaultLocation()
        }
    }

    // Muestra una ubicación de referencia cuando no se puede obtener la ubicación real.
    // Este método evita que el mapa quede vacío y agrega un marcador fijo como comportamiento alternativo.
    private fun showDefaultLocation() {
        // Ubicación de referencia usada cuando no se puede obtener la ubicación real.
        // Esto asegura que el comportamiento de la app coincida con lo documentado.
        val defaultLocation = LatLng(11.5444, -72.9072)

        googleMap.clear()

        googleMap.addMarker(
            MarkerOptions()
                .position(defaultLocation)
                .title("Ubicación de referencia")
        )

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f)
        )

        Toast.makeText(
            this,
            "No se pudo obtener la ubicación. Se muestra una referencia",
            Toast.LENGTH_LONG
        ).show()
    }

    // Recibe la respuesta del usuario frente a la solicitud del permiso de ubicación.
    // Si el permiso es aceptado, vuelve a intentar obtener la ubicación actual.
    // Si el permiso es rechazado, muestra el marcador de referencia como fallback.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario acepta el permiso, se vuelve a intentar obtener la ubicación.
                getCurrentLocation()
            } else {
                // Si el usuario niega el permiso, se muestra el marcador de referencia.
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                showDefaultLocation()
            }
        }
    }
}