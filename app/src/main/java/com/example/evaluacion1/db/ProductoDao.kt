package com.example.evaluacion1.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductoDao {
    /*Query que obtiene todos los registros de la BD ordenados por el campo COMPRADO
    de forma ASC.,
    * No en la cesta es 0 y en la cesta es 1 */
    @Query("SELECT * FROM Producto order by comprado asc ")
    fun getAll(): List<Producto>

    @Query("SELECT count(*) FROM Producto ")
    fun contar():Int

    @Insert
    fun insertar(producto:Producto)

    @Update
    fun actualizar(producto:Producto)
    @Delete
    fun eliminar(producto:Producto)


}