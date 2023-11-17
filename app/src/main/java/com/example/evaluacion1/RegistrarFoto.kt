package com.example.evaluacion1

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

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
    val appVM:AppVM = viewModel()
    Column() {
        Button(onClick = {
            appVM.PantallaActual.value = Pantalla.CAMARA
        }) {
            Text("Capturar Foto")
        }
    }

}

@Composable
fun PantallaCamaraUI(lanzadorPermisos:ActivityResultLauncher<Array<String>>,
                     cameraCtl:LifecycleCameraController){
    lanzadorPermisos.launch(arrayOf(android.Manifest.permission.CAMERA))
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
        PreviewView(it).apply {
            controller = cameraCtl
        }
    })

}