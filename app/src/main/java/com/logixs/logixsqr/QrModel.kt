package com.logixs.logixsqr

data class QrModel (
    val id: String,
    val sender_id: Long,
    val hash_code: String,
    val security_digit: String
)