package tech.ketc.numeri.domain.repository

import java.text.SimpleDateFormat
import java.util.*

val currentDateStr: String
    get() = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())