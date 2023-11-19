package com.example.evaluacion1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.evaluacion1.ui.theme.Evaluacion1Theme

class VisorImagen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) // Oculta la barra de título

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) // Establece la actividad en modo de pantalla completa

        val imageUri: Uri = intent.getParcelableExtra("imageUri")!!

        setContent {
            Evaluacion1Theme {
                FullScreenImage(uri = imageUri)
            }
        }
    }
}
@Composable
fun FullScreenImage(uri: Uri) {
    val contexto = LocalContext.current
    val imageBitmap = uri2Image(uri, contexto).asImageBitmap()
    // Composable Image that shows the bitmap
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Full-screen image",
            modifier = Modifier
                .fillMaxSize().fillMaxSize()
                .graphicsLayer {
                    rotationZ = 90f
                },
            contentScale = ContentScale.Inside
            // This will crop the image if it's not the same aspect ratio as the screen
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