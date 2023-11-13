package com.example.evaluacion1.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [Producto::class], version=1)
abstract class ProductoDatabase: RoomDatabase() {
    abstract fun productoDao(): ProductoDao

    companion object {

        @Volatile
        private var BASE_DATOS: ProductoDatabase? = null
        fun getInstance(contexto: Context): ProductoDatabase {
            return BASE_DATOS ?: synchronized(this) {
                Room.databaseBuilder(
                    contexto.applicationContext,
                    ProductoDatabase::class.java,
                    "ProductosDB.bd"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { BASE_DATOS = it }
            }
        }
    }
}