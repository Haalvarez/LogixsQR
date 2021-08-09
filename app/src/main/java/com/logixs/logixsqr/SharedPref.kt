package com.logixs.logixsqr

import android.content.Context
import android.content.SharedPreferences


object SharedPref {

    val USUARIO_ID = "id_usuario"
    val USUARIO_NOMBRE = "nombre"
    val USUARIO_APELLIDO = "apellido"
    val USUARIO_APODO = "apodo"
    val USUARIO_EMPRESA = "empresa"
    val USUARIO_PATH = "path"
    val USUARIO_PERFIL = "perfil"
    val NICKNAME_ML = "nickname"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("logixs", Context.MODE_PRIVATE)
    }

    private fun getSharedPreferenceEditor(context: Context): SharedPreferences.Editor {
        return getSharedPreferences(context).edit()
    }


    private fun getInt(context: Context, name: String, valorDefault: Int): Int {
        return getSharedPreferences(context).getInt(name, valorDefault)
    }

    private fun getBoolean(context: Context, name: String, valorDefault: Boolean): Boolean {
        return getSharedPreferences(context).getBoolean(name, valorDefault)
    }

    private fun getString(context: Context, name: String, valorDefault: String): String? {
        return getSharedPreferences(context).getString(name, valorDefault)
    }

    private fun getLong(context: Context, name: String, valorDefault: Long): Long {
        return getSharedPreferences(context).getLong(name, valorDefault)
    }

    private fun getFloat(context: Context, name: String, valorDefault: Float): Float {
        return getSharedPreferences(context).getFloat(name, valorDefault)
    }

    private fun setInt(context: Context, name: String, valor: Int) {
        getSharedPreferenceEditor(context).putInt(name, valor).commit()
    }

    private fun setString(context: Context, name: String, valor: String) {
        getSharedPreferenceEditor(context).putString(name, valor).commit()
    }

    private fun setBoolean(context: Context, name: String, valor: Boolean) {
        getSharedPreferenceEditor(context).putBoolean(name, valor).commit()
    }

    private fun setLong(context: Context, name: String, valor: Long) {
        getSharedPreferenceEditor(context).putLong(name, valor).commit()
    }

    private fun setFloat(context: Context, name: String, valor: Float) {
        getSharedPreferenceEditor(context).putFloat(name, valor).commit()
    }

    private fun remove(context: Context, name: String) {
        getSharedPreferenceEditor(context).remove(name).commit()
    }

    fun clear(context: Context) {
        getSharedPreferenceEditor(context).clear().commit()
    }

    fun getIdUsuario(context: Context): String? {
        return getString(context, USUARIO_ID, "")
    }

    fun setIdUsuario(context: Context, id: String) {
        setString(context, USUARIO_ID, id)
    }

    fun getNombreUsuario(context: Context): String? {
        return getString(context, USUARIO_NOMBRE, "")
    }

    fun setNombreUsuario(context: Context, nombre: String) {
        setString(context, USUARIO_NOMBRE, nombre)
    }

    fun getApellidoUsuario(context: Context): String? {
        return getString(context, USUARIO_APELLIDO, "")
    }

    fun setApellidoUsuario(context: Context, apellido: String) {
        setString(context, USUARIO_APELLIDO, apellido)
    }

    fun getApodoUsuario(context: Context): String? {
        return getString(context, USUARIO_APODO, "")
    }

    fun setApodoUsuario(context: Context, apodo: String) {
        setString(context, USUARIO_APODO, apodo)
    }

    fun getEmpresaUsuario(context: Context): String? {
        return getString(context, USUARIO_EMPRESA, "")
    }

    fun setEmpresaUsuario(context: Context, empresa: String) {
        setString(context, USUARIO_EMPRESA, empresa)
    }

    fun getPathUsuario(context: Context): String? {
        return getString(context, USUARIO_PATH, "")
    }

    fun setPathUsuario(context: Context, path: String) {
        setString(context, USUARIO_PATH, path)
    }

    fun getPerfilUsuario(context: Context): String? {
        return getString(context, USUARIO_PERFIL, "")
    }

    fun setPerfilUsuario(context: Context, perfil: String) {
        setString(context, USUARIO_PERFIL, perfil)
    }

    fun getNickNameMl(context: Context): String? {
        return getString(context, NICKNAME_ML, "")
    }

    fun setNickNameMl(context: Context, nickname: String) {
        setString(context, NICKNAME_ML, nickname)
    }

    fun borrarTodo(context: Context) {
        remove(context,USUARIO_ID)
        remove(context,USUARIO_NOMBRE)
        remove(context,USUARIO_APELLIDO)
        remove(context,USUARIO_APODO)
        remove(context,USUARIO_EMPRESA)
        remove(context,USUARIO_PATH)
        remove(context,USUARIO_PERFIL)
        remove(context,NICKNAME_ML)
    }
}