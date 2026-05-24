package com.example.atomic.data

data class PositiveHabit(
    val id: String,
    val name: String, 
    val targetPackageName: String, 
    val suggestedHour: Int? = null 
)

val myGoodHabits = listOf(
    PositiveHabit("1", "Aprender Idiomas", "com.duolingo"),
    PositiveHabit("2", "Lectura", "com.amazon.kindle", suggestedHour = 20),
    PositiveHabit("3", "Organización", "notion.id")
)
