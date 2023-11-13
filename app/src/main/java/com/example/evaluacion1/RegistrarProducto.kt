package com.example.evaluacion1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.evaluacion1.db.Producto
import com.example.evaluacion1.db.ProductoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class RegistrarProducto : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PantallaRegistroProducto { nombreProducto, comprado ->
                regProd(nombreProducto, comprado)
            }
        }
    }


    /*Funcion creada para registrar el producto*/
    fun regProd(nombreProducto: String, comprado: Int) {

        val contexto = this
        /*Se lanza la corrutina la que realiza el insert en la BD.
        * Se utiliza la clase Random para obtener una ID de producto aleatoria dentro de un rango.*/
        lifecycleScope.launch(Dispatchers.IO) {
            val productoDao = ProductoDatabase.getInstance(this@RegistrarProducto).productoDao()
            val nuevoProducto = Producto(Random.nextInt(1, 10000), nombreProducto, comprado)
            productoDao.insertar(nuevoProducto)
            // Aca cambiamos al hilo principal para actualizar la UI.
            withContext(Dispatchers.Main) {
                /*Se llama a la funcion que genera el ALERT, se le pasan los parametros correspondientes*/
                dialogoInformacionRegistrar(contexto, resources.getString(R.string.msg_reg_prod), resources.getString(R.string.msg_alerta_titulo) )
            }
        }
    }

}

/*En esta funcion se setea los elementos visuales y sus respectivos STRING son cargados desde el resource.*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistroProducto(regProd: (String, Int) -> Unit) {

    val contexto = LocalContext.current
    val (nombreProducto, setNombreProducto) = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Image(
            painter = painterResource(id = R.drawable._0608883),
            contentDescription = "",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier =  Modifier.height(30.dp))
        TextField(
            value = nombreProducto,
            onValueChange = setNombreProducto,
            label = { Text(text = stringResource(R.string.reg_ing_nombre)) }
        )
        Spacer(modifier =  Modifier.height(30.dp))
        Button(onClick={
            regProd(nombreProducto, 0)
            setNombreProducto("")
        }){
            Text(text = stringResource(R.string.reg_btn_reg))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Button(onClick={
            contexto.startActivity(Intent(contexto, MainActivity::class.java))
        }){
            Text(text = stringResource(R.string.reg_btn_lista))
        }
    }

}

/*Funcion para crear el ALERT que se gatilla despues de cada accion
    Se requiere el contexto, mensaje y el titulo de alert.
    Los recursos son obtenidos desde STRING
* */
fun dialogoInformacionRegistrar(contexto: Context, msgInfo: String, titulo: String ) {
    val builder = AlertDialog.Builder(contexto)
    builder.setTitle(titulo)
    builder.setMessage(msgInfo)
    builder.setPositiveButton("OK") { dialog, which ->
        contexto.startActivity(Intent(contexto, MainActivity::class.java)) /* REDIRECCIONAMOS a la vista de LISTA PRODUCTOS*/
    }
    builder.setCancelable(false)

    builder.show()
}
