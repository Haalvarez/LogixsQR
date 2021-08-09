package com.logixs.logixsqr.database

import androidx.room.*


@Dao
interface EnvioDao {
    @Query("SELECT * FROM envio")
    fun getAll(): List<Envio>

    @Query("SELECT * FROM envio WHERE seccion=:valor")
    fun getAllBySeccion(valor:String): MutableList<Envio>

    @Query("DELETE FROM envio WHERE seccion=:valor")
    fun deleteBySeccion(valor:String)

    @Query("SELECT * FROM envio WHERE id_envio IN (:idsEnvios)")
    fun loadAllByIds(idsEnvios: IntArray): List<Envio>

    @Insert
    fun insertAll(vararg users: Envio)

    @Delete
    fun delete(user: Envio)
}

