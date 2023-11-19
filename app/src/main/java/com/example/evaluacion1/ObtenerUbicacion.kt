package com.example.evaluacion1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Spannable.Factory
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evaluacion1.ui.theme.Evaluacion1Theme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class GpsVM : ViewModel(){
    //val PantallaActual = mutableStateOf(Pantalla.GPS)
    val latitud = mutableStateOf(0.0)
    val longitud = mutableStateOf(0.0)

    var permisoUbicacionOK:() -> Unit = {}
}

class ObtenerUbicacion : ComponentActivity() {
    
    val gpsVM:GpsVM by viewModels()

    val lanzadorPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        if(
            (it[android.Manifest.permission.ACCESS_FINE_LOCATION]?:false)
            or
            (it[android.Manifest.permission.ACCESS_COARSE_LOCATION]?:false)
        ){
            gpsVM.permisoUbicacionOK()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val nombre = intent.getStringExtra("nombre")!!
            val latitud = intent.getStringExtra("latitud")!!
            val longitud = intent.getStringExtra("longitud")!!

            PantallaGpsUI(gpsVM, lanzadorPermisos, nombre )


        }
    }
}


class FaltaPermisosSeguridad(mensaje:String): Exception(mensaje)
@Composable
fun PantallaGpsUI(gpsVM: GpsVM,
                  lanzadorPermisos: ActivityResultLauncher<Array<String>>,
                  nombre:String
                 ){

    val contexto =  LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            gpsVM.permisoUbicacionOK = {

                obtenerUbicacion(contexto) {
                    gpsVM.latitud.value = it.latitude
                    gpsVM.longitud.value = it.longitude
                }

            }

            lanzadorPermisos.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        }) {
            Text("OBTENER UBICACIÓN")
        }
        Spacer(Modifier.height(5.dp))
        Text("Latitud : ${gpsVM.latitud.value}  -- Longitud : ${gpsVM.longitud.value}")
        Spacer(Modifier.height(50.dp))

        AndroidView(
            factory = {
                MapView(it).apply{

                    setTileSource(TileSourceFactory.MAPNIK)
                    org.osmdroid.config.Configuration.getInstance().userAgentValue = contexto.packageName
                    controller.setZoom(15.0)
                }
            }, update = {
                it.overlays.removeIf{true}
                it.invalidate()

                val geoPoint = GeoPoint(gpsVM.latitud.value, gpsVM.longitud.value)
                it.controller.animateTo(geoPoint)

                val marcador = Marker(it)
                marcador.position = geoPoint
                marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                it.overlays.add(marcador)
            }
        )
        Spacer(Modifier.height(50.dp))
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Button(onClick = {

            val intent = Intent(contexto, RegistrarFoto::class.java)
            intent.putExtra("nombre", nombre)
            intent.putExtra("latitud", gpsVM.latitud.value.toString())
            intent.putExtra("longitud", gpsVM.longitud.value.toString())
            contexto.startActivity(intent)


        }) {
            Text(text = "Guardar ubicación")
        }
    }
}


fun obtenerUbicacion(contexto: Context, onSuccess:(ubicacion: Location)-> Unit){
    try {
        val servicio = LocationServices.getFusedLocationProviderClient(contexto)
        val tarea = servicio.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        tarea.addOnSuccessListener {
            onSuccess(it)
        }

    }catch (se:SecurityException){
        throw FaltaPermisosSeguridad("Faltan los permisos de seguridad")
    }

}