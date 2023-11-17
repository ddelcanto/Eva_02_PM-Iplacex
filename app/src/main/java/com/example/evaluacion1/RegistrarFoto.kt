package com.example.evaluacion1

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.util.UUID

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
    val formularioVM: FormularioVM by viewModels()

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

    val contexto = LocalContext.current

    Column() {
        Button(onClick = {
            appVM.PantallaActual.value = Pantalla.CAMARA
        }) {
            Text("Capturar Foto")
        }
        formularioVM.foto.value?.let{
            Image(
                painter= BitmapPainter(uri2Image(it, contexto)),
                contentDescription = "Imagen tomada por CameraX"
            )
        }

    }
}

fun uri2Image(uri:Uri, contexto:Context)= BitmapFactory.decodeStream(
    contexto.contentResolver.openInputStream(uri)
).asImageBitmap()



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