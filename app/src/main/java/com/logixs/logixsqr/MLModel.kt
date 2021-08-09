package com.logixs.logixsqr

import com.google.gson.JsonArray
import com.google.gson.JsonObject


data class MLModel (
    val id: Long,
    val nickname: String,
    val registrationDate: String,
    val countryID: String,
    val address: JsonObject,
    val userType: String,
    val tags: JsonArray,
    val logo: Any? = null,
    val points: Long,
    val siteID: String,
    val permalink: String,
    val sellerReputation: JsonObject,
    val buyerReputation: JsonObject,
    val status: JsonObject
)