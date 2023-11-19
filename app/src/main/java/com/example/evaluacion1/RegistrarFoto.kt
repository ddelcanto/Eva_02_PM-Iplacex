package com.example.evaluacion1

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import java.io.InputStream

enum class Pantalla {
    FORM,
    CAMARA
}


class AppVM : ViewModel(){

    val PantallaActual = mutableStateOf(Pantalla.FORM)

    //Clase que no devuelve datos pero puede ser sobreescrita
    var onPermisoCamaraOK:() -> Unit = {}

}

class FormularioVM : ViewModel(){
    val foto = mutableStateOf<Uri?>(null)
}


class RegistrarFoto : ComponentActivity() {

    val camaraVM: AppVM by viewModels()

    lateinit var cameraCtl:LifecycleCameraController

    val lanzadorPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
            if(it[android.Manifest.permission.CAMERA]?:false){
                camaraVM.onPermisoCamaraOK()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraCtl = LifecycleCameraController(this)
        cameraCtl.bindToLifecycle(this)
        cameraCtl.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA


        setContent {
            //Se setean los valores que vienen de la actividad anterior.
            val nombre = intent.getStringExtra("nombre")!!
            val latitud = intent.getStringExtra("latitud")!!
            val longitud = intent.getStringExtra("longitud")!!

            AppUI(lanzadorPermisos, cameraCtl, nombre, latitud, longitud)

        }
    }
}

/*Se reciben los datos para ser enviados a cada UI*/
@Composable
fun AppUI(lanzadorPermisos:ActivityResultLauncher<Array<String>>,
          cameraCtl:LifecycleCameraController,
          nombre:String,
          latitud:String,
          longitud:String){

    val appVM:AppVM = viewModel()

    when(appVM.PantallaActual.value){
        Pantalla.FORM ->{
            //Pasada de parametros para ser mostrados en UI
            PantallaFormUI(nombre, latitud, longitud)
        }
        Pantalla.CAMARA ->{
           PantallaCamaraUI(lanzadorPermisos, cameraCtl)
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormUI(nombre:String, latitud:String, longitud:String){

    val appVM:AppVM = viewModel()
    val (nombreLocacion, setNombreLocacion) = remember { mutableStateOf("") }
    val (latitud_, setLatitud) = remember { mutableStateOf("") }
    val (longitud_, setLongitud) = remember { mutableStateOf("") }

    val contexto = LocalContext.current

    /*Se crear las columnas y componentes necesarios para mostrar la información*/
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier =  Modifier.height(30.dp))
            TextField(
                value = nombre,
                onValueChange = setNombreLocacion,
                readOnly = true,
                label = { Text(text = "Nombre del lugar") }
            )
            Spacer(modifier =  Modifier.height(5.dp))
            TextField(
                value = latitud,
                onValueChange = setLatitud,
                readOnly = true,
                label = { Text(text = "Latitud") }
            )
            Spacer(modifier =  Modifier.height(5.dp))
            TextField(
                value = longitud,
                onValueChange = setLongitud,
                readOnly = true,
                label = { Text(text = "Longitud") }
            )
            Spacer(modifier =  Modifier.height(5.dp))
            Button(onClick={
                appVM.PantallaActual.value = Pantalla.CAMARA
            }){
                Text(text = "Abrir CAMARA")
            }

            Button(onClick={
                /*Se envían los datos a la actividad de ObtenerUbicación.kt*/
                val intent = Intent(contexto, ObtenerUbicacion::class.java)
                intent.putExtra("nombre", nombre)
                intent.putExtra("latitud", latitud)
                intent.putExtra("longitud", longitud)
                contexto.startActivity(intent)
            }){
                Text(text = "Abrir GPS")
            }

            Spacer(modifier =  Modifier.height(10.dp))

            //Se llama a la funcion que obtiene todas las imagenes guardadas en la carpeta publica
            MostrarImagenesPublicas(contexto)

        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ){


    }

}

@Composable
fun MostrarImagenesPublicas(contexto: Context) {
    val listaImagenes = remember { mutableStateOf<List<Uri>>(listOf()) }

    LaunchedEffect(key1 = true) {
        listaImagenes.value = obtenerImagenes(contexto)
    }

    LazyColumn {
        /*Reviso cada item y es representada en la UI, se hace la transformacion de URI a Bitmap
        * Tambien se captura la accion al momento de presionar la imagen, esta gatilla el envio de informacion
        * a la actividad de VisorImagen.kt
        * */
        items(listaImagenes.value) { imageUri ->
            val imageBitmap = uri2Image(imageUri, contexto).asImageBitmap()
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = "",
                modifier = Modifier
                    .clickable {
                        val intent = Intent(contexto, VisorImagen::class.java)
                        intent.putExtra("imageUri", imageUri)
                        contexto.startActivity(intent)
                    }
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

// Se crea funcion para obtener las imagenes de la carpeta publica.
fun obtenerImagenes(contexto: Context): List<Uri> {
    val imageUris = mutableListOf<Uri>()
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

    contexto.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            imageUris.add(contentUri)
        }
    }
    return imageUris
}


/*Funcion para transformar de URI a Bitmap. Se vio en clases*/
fun uri2Image(uri: Uri, contexto: Context): Bitmap {
    val contentResolver = contexto.contentResolver
    var inputStream: InputStream? = null
    var bitmap: Bitmap? = null
    try {
        inputStream = contentResolver.openInputStream(uri)
        bitmap = BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        inputStream?.close()
    }
    return bitmap!!
}

/*Funcion para capturar y almacenar la imagen*/
fun CapturarFoto(cameraCtl: LifecycleCameraController,
                 contexto: Context,
                 onImagenGuardar:(uri:Uri)-> Unit
) {
    //Se crea imagen con nombre GUID aleatorio
    val filename = UUID.randomUUID().toString() + ".jpg"

    //Se guarda en carpeta publica
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val opciones = OutputFileOptions.Builder(contexto.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues).build()

    cameraCtl.takePicture(
        opciones,
        ContextCompat.getMainExecutor(contexto),
        object : OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let {
                    onImagenGuardar(it)
                }
            }
            //Manejo de error en LOG
            override fun onError(exception: ImageCaptureException) {
                Log.e("Error al capturar la foto", exception.message ?: "Error")
            }
        }
    )
}
@Composable
fun PantallaCamaraUI(lanzadorPermisos:ActivityResultLauncher<Array<String>>,
                     cameraCtl:LifecycleCameraController){

    val formularioVM: FormularioVM = viewModel()
    val appVM:AppVM = viewModel()
    val contexto = LocalContext.current

    lanzadorPermisos.launch(arrayOf(android.Manifest.permission.CAMERA))
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
        PreviewView(it).apply {
            controller = cameraCtl
        }
    })
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Button(onClick = {
            //Llamado a funcion que captura imagen pasando los aprametros establecidos
            CapturarFoto(cameraCtl, contexto) { uri ->
                formularioVM.foto.value = uri
                appVM.PantallaActual.value = Pantalla.FORM
            }
        }) {
            Text("Capturar Foto")
        }
    }
}
