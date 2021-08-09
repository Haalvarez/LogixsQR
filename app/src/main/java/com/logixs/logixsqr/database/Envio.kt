package com.logixs.logixsqr.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Envio(
    @PrimaryKey() val id: String,
    @ColumnInfo(name = "id_envio") val idEnvio: String,
    @ColumnInfo(name = "seccion") val seccion: String
)
