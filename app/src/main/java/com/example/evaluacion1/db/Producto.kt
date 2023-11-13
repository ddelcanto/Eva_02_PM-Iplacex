package com.example.evaluacion1.db
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Producto(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "nombreProducto") var nombreProducto: String?,
        @ColumnInfo(name = "comprado") var comprado: Int?
)