package com.logixs.logixsqr

data class PostQrModel (
    val resultado: Int,
    val mensaje: String
)
data class PendientesDeEntregaXusuarioVM (
    val id_mercadoEnvioFlex: Int,
    val id_envio: String,
    val NickName:String,
    val jReceiver_address:String
)

