package com.example.evaluacion1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.evaluacion1.db.Producto
import com.example.evaluacion1.db.ProductoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.stringResource

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO){
            /*Obtengo los valores desde SQLite, en el caso de no tener alguno se agregan 4 de forma predeterminada*/
            val productoDao = ProductoDatabase.getInstance(this@MainActivity).productoDao()
            if (productoDao.contar() < 1 ) {
                productoDao.insertar( Producto( 1, "Nintendo SNES", 0 ))
                productoDao.insertar( Producto( 2, "Nintendo NES", 0 ))
                productoDao.insertar( Producto( 3, "Nintendo SWITCH", 0 ))
                productoDao.insertar( Producto( 4, "Nintendo 64", 0 ))
            }
        }
        setContent {
            //Llamo al GUI principal
            ListaProductosUI()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProductosUI(){

    val contexto = LocalContext.current
    val (productos, setProductos) =  remember { mutableStateOf(emptyList<Producto>())}

    //Lanzo la corrutina para llenar la tabla principal de la vista
    LaunchedEffect(productos){
        withContext(Dispatchers.IO){
            val dao = ProductoDatabase.getInstance( contexto ).productoDao()
            setProductos(dao.getAll())
        }
    }
    /*
    * En la ROW siguiente se setea la configuracion visual y se inserta una imagen
    * desde DRAWABLE.
    * Se utiliza stringResource para llamar al recurso String correspondiente.
    *
    * */
    Row(
    ){
        Column(
            modifier = Modifier.fillMaxSize().padding(30.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = stringResource(R.string.nombre_main))
            Image(
                painter = painterResource(id = R.drawable._901662),
                contentDescription = "",
                modifier = Modifier.fillMaxWidth().padding(30.dp)
            )
        }
    }
    Row(){
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            items(productos){producto ->
                ProductoItemUI(producto){
                    setProductos(emptyList<Producto>())
                }
            }
        }
    }
    /* Boton para direccionar a la vista de registro de un nuevo producto
    * Se utiliza stringResource para llamar al recurso String correspondiente.
    * */
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Button(onClick={
            contexto.startActivity(Intent(contexto, RegistrarProducto::class.java))
        }){
            Text(text = stringResource(R.string.reg_btn_reg_main))
        }

        Button(onClick={
            contexto.startActivity(Intent(contexto, RegistrarFoto::class.java))
        }){
            Text(text ="IR A REGISTRAR FOTOS")
        }

    }
}

@Composable
fun ProductoItemUI(producto:Producto, guardar:() -> Unit){
    val contexto = LocalContext.current
    val alcanceCorr = rememberCoroutineScope()

    /* Se setean las variables con los datos del recurso STRINGS*/
    val msg_quitar = stringResource(R.string.msg_quitar)
    val msg_agregar = stringResource(R.string.msg_agregar)
    val msg_eliminar = stringResource(R.string.msg_eliminar)
    val msg_alerta_titulo = stringResource(R.string.msg_alerta_titulo)


    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical= 20.dp , horizontal = 20.dp)
    ){
        if(producto.comprado == 1){
            Icon(
                Icons.Filled.Check,
                contentDescription = "Comprado",
                tint = Color.Green,
                modifier = Modifier.clickable {
                    alcanceCorr.launch(Dispatchers.IO){
                        /*En el siguiente codigo se setea el valor a la variable y se procede a
                        * grabar en la BD obteniendo la instancia basado en el contexto actual.
                        * En la misma linea llamamos a la clase productoDao e invocamos el metodo actualizar,
                        * a este se le pasa el objeto PRODUCTO*/

                        producto.comprado = 0
                        ProductoDatabase.getInstance( contexto ).productoDao().actualizar(producto)
                        withContext(Dispatchers.Main) {
                            /*Se llama a la funcion que genera el ALERT, se le pasan los parametros correspondientes*/
                            dialogoInformacionMain(contexto, msg_quitar, msg_alerta_titulo)
                        }
                        guardar()
                    }
                }
            )
        }else{
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = "Comprar",
                tint = Color.Blue,
                modifier = Modifier.clickable {
                    alcanceCorr.launch(Dispatchers.IO){
                        /*En el siguiente codigo se setea el valor a la variable y se procede a
                        * grabar en la BD obteniendo la instancia basado en el contexto actual.
                        * En la misma linea llamamos a la clase productoDao e invocamos el metodo actualizar,
                        * a este se le pasa el objeto PRODUCTO*/
                        producto.comprado = 1
                        ProductoDatabase.getInstance( contexto ).productoDao().actualizar(producto)
                        withContext(Dispatchers.Main) {
                            /*Se llama a la funcion que genera el ALERT, se le pasan los parametros correspondientes*/
                            dialogoInformacionMain(contexto, msg_agregar, msg_alerta_titulo)
                        }
                        guardar()
                    }
                }
            )
        }
        Spacer(modifier =  Modifier.width(20.dp))
        Text(
            text = producto.nombreProducto.toString(),
            modifier = Modifier.weight(2f)
        )
        Icon(
            Icons.Filled.Delete,
            contentDescription = "Eliminar",
            tint = Color.Red,
            modifier = Modifier.clickable {
                alcanceCorr.launch(Dispatchers.IO){
                    /*En el siguiente codigo se setea el valor a la variable y se procede a
                       * grabar en la BD obteniendo la instancia basado en el contexto actual.
                       * En la misma linea llamamos a la clase productoDao e invocamos el metodo eliminar,
                       * a este se le pasa el objeto PRODUCTO*/
                    ProductoDatabase.getInstance( contexto ).productoDao().eliminar(producto)
                    withContext(Dispatchers.Main) {
                        /*Se llama a la funcion que genera el ALERT, se le pasan los parametros correspondientes*/
                        dialogoInformacionMain(contexto, msg_eliminar, msg_alerta_titulo)
                    }
                    guardar()
                }
            }
        )
    }
}

/*Funcion para crear el ALERT que se gatilla despues de cada accion
    Se requiere el contexto, mensaje y el titulo de alert.
    Los recursos son obtenidos desde STRING
* */
fun dialogoInformacionMain(contexto: Context, msgInfo: String, titulo: String ) {
    val builder = AlertDialog.Builder(contexto)
    builder.setTitle(titulo)
    builder.setMessage(msgInfo)
    builder.setPositiveButton("OK") { dialog, which ->

    }
    builder.setCancelable(false)

    builder.show()
}
