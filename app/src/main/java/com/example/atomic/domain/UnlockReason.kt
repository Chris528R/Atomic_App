package com.example.atomic.domain

enum class UnlockReason(val title: String, val allowedMinutes: Int) {
    WORK("Trabajo", 15),
    IMPORTANT("Mensaje importante", 10),
    ENTERTAINMENT("Entretenimiento", 5),
    BORED("Estoy aburrido", 2),
    OTHER("Otro", 2),
    FREE_TIME("Horario libre", 60);

    companion object {
        fun fromTitle(title: String): UnlockReason? {
            return entries.find { it.title == title }
        }
    }
}
