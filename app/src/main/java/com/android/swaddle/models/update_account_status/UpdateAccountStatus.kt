package com.android.swaddle.models.update_account_status

import java.io.Serializable

data class UpdateAccountStatus(
    val data: List<Any>,
    val message: String,
    val status: Boolean
) : Serializable