package com.example.evaluacion1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.evaluacion1.ui.theme.Evaluacion1Theme

class VisorImagen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        //Se recibe la URI de la imagen
        val imageUri: Uri = intent.getParcelableExtra("imageUri")!!

        setContent {
            Evaluacion1Theme {
                ImagenPantallaCompleta(uri = imageUri)
            }
        }
    }
}
@Composable
fun ImagenPantallaCompleta(uri: Uri) {

    val contexto = LocalContext.current
    val imageBitmap = uri2Image(uri, contexto).asImageBitmap()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        //La imagen se muestra y se rota en 90°
        Image(
            bitmap = imageBitmap,
            contentDescription = "Full-screen image",
            modifier = Modifier
                .fillMaxSize().fillMaxSize()
                .graphicsLayer {
                    rotationZ = 90f
                },
            contentScale = ContentScale.Inside

        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Button(onClick={
            contexto.startActivity(Intent(contexto, RegistrarFoto::class.java))
        }){
            Text(text = "Atrás")
        }
    }



}