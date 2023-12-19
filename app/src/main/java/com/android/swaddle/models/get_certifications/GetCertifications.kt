package com.android.swaddle.models.get_certifications

import java.sql.Array

data class GetCertifications(
    val data: ArrayList<CertificationsData>,
    val message: String,
    val status: Boolean
)