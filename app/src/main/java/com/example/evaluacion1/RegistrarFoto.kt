package com.example.evaluacion1

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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

    val nombreLugar = mutableStateOf("")
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
            AppUI(lanzadorPermisos, cameraCtl)
        }
    }
}

@Composable
fun AppUI(lanzadorPermisos:ActivityResultLauncher<Array<String>>,
          cameraCtl:LifecycleCameraController){

    val appVM:AppVM = viewModel()
    val gpsVM : GpsVM = viewModel()

    when(appVM.PantallaActual.value){
        Pantalla.FORM ->{
            PantallaFormUI()
        }

        Pantalla.CAMARA ->{
            PantallaCamaraUI(lanzadorPermisos, cameraCtl)
        }

    }

}

@Composable
fun PantallaFormUI(){

    val formularioVM: FormularioVM = viewModel()
    val appVM:AppVM = viewModel()
    val gpsVM:GpsVM = viewModel()

    val contexto = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        formularioVM.foto.value?.let{
            DisplayImagesFromDirectory(contexto)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Button(onClick={
            appVM.PantallaActual.value = Pantalla.CAMARA
        }){
            Text(text = "Abrir CAMARA")
        }

        Button(onClick={
            contexto.startActivity(Intent(contexto, ObtenerUbicacion::class.java))
        }){
            Text(text = "Abrir GPS")
        }
    }

}

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

fun CrearImagenPrivada(contexto:Context):File = File(
    contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
    UUID.randomUUID().toString()+".jpg"
)

fun CapturarFoto(cameraCtl: LifecycleCameraController,
                 archivo: File,
                 contexto: Context,
                 onImagenGuardar:(uri:Uri)-> Unit
){
    val opciones = OutputFileOptions.Builder(archivo).build()

    cameraCtl.takePicture(
        opciones,
        ContextCompat.getMainExecutor(contexto),
        object: OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let{
                    onImagenGuardar(it)
                }
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("Error al capturar la foto :(", exception.message?:"Error")
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

            CapturarFoto(cameraCtl, CrearImagenPrivada(contexto), contexto ){
                formularioVM.foto.value = it
                appVM.PantallaActual.value = Pantalla.FORM
            }

        }) {
            Text("Capturar Foto")
        }
    }
}




/*GPS*/








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