package me.shohag.system_service_events.model

data class LogModel(
    val id: Long = System.currentTimeMillis(),
    var logMsg : String = ""

)
