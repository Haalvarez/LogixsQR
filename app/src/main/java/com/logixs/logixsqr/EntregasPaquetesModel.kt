package com.logixs.logixsqr

import java.io.Serializable

data class EntregasPaquetesModel (
    val IdEnvio: String,
    val AddressLine : String,
    val DistanciaEnMetros : Int,
    val distanciaFormateada : String,
    val telefonoRecibe : String,
    val NombreRecibe : String,
    val IdMl : String,
    val Sender_id : String,
    val NicknameVendedor : String,
    val Lat : String,
    val Lng : String,
    val DiaEntregaEstimada : String
) : Serializable
