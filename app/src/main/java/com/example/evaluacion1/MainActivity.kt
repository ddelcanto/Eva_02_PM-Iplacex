package com.example.evaluacion1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppUI()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUI(){

    val contexto = LocalContext.current
    val (nombreLocacion, setNombreLocacion) = remember { mutableStateOf("") }

    Column(

        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ){

        Spacer(modifier =  Modifier.height(30.dp))
        TextField(
            value = nombreLocacion,
            onValueChange = setNombreLocacion,
            label = { Text(text = "Ingresar nombre del lugar") }
        )
        Spacer(modifier =  Modifier.height(30.dp))
        Button(onClick={

            //Le paso valores por INTENT para que se vean al cargar RegistrarFoto.kt
            val intent = Intent(contexto, RegistrarFoto::class.java)
            intent.putExtra("nombre", nombreLocacion)
            intent.putExtra("latitud", "Sin datos")
            intent.putExtra("longitud", "Sin datos")
            contexto.startActivity(intent)
        }){
            Text(text ="REGISTRAR VACACIONES")
        }

    }
}

