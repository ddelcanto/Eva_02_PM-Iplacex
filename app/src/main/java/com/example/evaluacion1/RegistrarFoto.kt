package com.example.evaluacion1

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
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
import java.io.File
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
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            val nombre = intent.getStringExtra("nombre")!!
            val latitud = intent.getStringExtra("latitud")!!
            val longitud = intent.getStringExtra("longitud")!!

            AppUI(lanzadorPermisos, cameraCtl, nombre, latitud, longitud)

        }
    }
}

@Composable
fun AppUI(lanzadorPermisos:ActivityResultLauncher<Array<String>>,
          cameraCtl:LifecycleCameraController,
          nombre:String,
          latitud:String,
          longitud:String){

    val appVM:AppVM = viewModel()

    when(appVM.PantallaActual.value){
        Pantalla.FORM ->{
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
                val intent = Intent(contexto, ObtenerUbicacion::class.java)
                intent.putExtra("nombre", nombre)
                intent.putExtra("latitud", latitud)
                intent.putExtra("longitud", longitud)
                contexto.startActivity(intent)
                //contexto.startActivity(Intent(contexto, ObtenerUbicacion::class.java))
            }){
                Text(text = "Abrir GPS")
            }
            Spacer(modifier =  Modifier.height(10.dp))
            DisplayPublicImages(contexto)
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ){


    }

}

/*
@Composable
fun DisplayImagesFromDirectory(contexto: Context) {

    val imagesDirectory = contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFiles = imagesDirectory?.listFiles()?.toList() ?: listOf()

    LazyColumn {
        items(imageFiles) { file ->
            val imageUri = Uri.fromFile(file)
            val imageBitmap = uri2Image(Uri.fromFile(file), contexto).asImageBitmap()
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = "Imagen tomada por CameraX",
                modifier = Modifier
                    .clickable {
                        val intent = Intent(contexto, VisorImagen::class.java)
                        intent.putExtra("imageUri", imageUri)
                        contexto.startActivity(intent)
                    }
            )
            Spacer(Modifier.height(20.dp))
        }
    }


}
*/


@Composable
fun DisplayPublicImages(contexto: Context) {
    val imageList = remember { mutableStateOf<List<Uri>>(listOf()) }

    // Carga las imágenes cuando el Composable entra en la Composición
    LaunchedEffect(key1 = true) {
        imageList.value = queryImages(contexto)
    }

    LazyColumn {
        items(imageList.value) { imageUri ->
            val imageBitmap = uri2Image(imageUri, contexto).asImageBitmap()
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = "Imagen de la galería pública",
                modifier = Modifier
                    .clickable {
                        val intent = Intent(contexto, VisorImagen::class.java)
                        intent.putExtra("imageUri", imageUri)
                        contexto.startActivity(intent)
                    }
                    .fillMaxWidth()
                    .height(200.dp) // Ajustar según sea necesario
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

// Función para consultar imágenes del almacenamiento público del dispositivo
fun queryImages(contexto: Context): List<Uri> {
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

fun CapturarFoto(cameraCtl: LifecycleCameraController,
                 contexto: Context,
                 onImagenGuardar:(uri:Uri)-> Unit
) {
    // Creamos el nombre de archivo utilizando un formato de fecha y hora para evitar duplicados
    val filename = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // Esta línea asegura que se guarde en la carpeta de imágenes públicas
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

            override fun onError(exception: ImageCaptureException) {
                Log.e("Error al capturar la foto", exception.message ?: "Error desconocido")
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
    Column() {
        Button(onClick = {


            CapturarFoto(cameraCtl, contexto) { uri ->
                formularioVM.foto.value = uri
                appVM.PantallaActual.value = Pantalla.FORM
                // Aquí puedes hacer algo más con la URI de la imagen, como mostrarla en la UI.
            }


        /*
            CapturarFoto(cameraCtl, CrearImagenPrivada(contexto), contexto ){
                formularioVM.foto.value = it
                appVM.PantallaActual.value = Pantalla.FORM
            }
        */
        }) {
            Text("Capturar Foto")
        }
    }
}




fun dialogoInformacionFoto(contexto: Context, msgInfo: String, titulo: String ) {
    val builder = AlertDialog.Builder(contexto)
    builder.setTitle(titulo)
    builder.setMessage(msgInfo)
    builder.setPositiveButton("OK") { dialog, which ->
       // contexto.startActivity(Intent(contexto, MainActivity::class.java)) /* REDIRECCIONAMOS a la vista de LISTA PRODUCTOS*/
    }
    builder.setCancelable(false)

    builder.show()
}