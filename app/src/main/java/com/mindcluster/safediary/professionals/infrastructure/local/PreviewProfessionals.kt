package com.mindcluster.safediary.professionals.infrastructure.local

import com.mindcluster.safediary.professionals.domain.model.Professional

/** UI fixtures only: no remote directory or professional credentials are queried. */
object PreviewProfessionals {
    val all = listOf(
        Professional("laura", "Dra. Laura Gómez", "LG", "Psicología clínica · TCC y trauma", listOf("Ansiedad y estrés", "TCC", "Estrés académico"), "Acompañamiento centrado en ansiedad, estrés académico y herramientas prácticas de bienestar. Un espacio de escucha para avanzar a tu ritmo.", 120, "4.9", 128, "Mañana, 15:00", "Español e inglés"),
        Professional("sarah", "Dra. Sarah Jenkins", "SJ", "Atención plena y terapia compasiva", listOf("Atención plena", "Ansiedad y estrés"), "Un enfoque compasivo para reconocer tus emociones y cultivar una relación más amable contigo.", 80, "5.0", 94, "Mañana, 10:00", "Español e inglés"),
        Professional("liam", "Ps. Liam Thorne", "LT", "Bienestar integral y manejo del estrés", listOf("Ansiedad y estrés", "Bienestar integral"), "Herramientas de regulación emocional y conciencia corporal para acompañar los desafíos cotidianos.", 65, "4.8", 82, "Hoy, 18:00"),
        Professional("andres", "Dr. Andrés Ríos", "AR", "Psicología clínica · Bienestar emocional", listOf("Bienestar integral", "Atención plena"), "Escucha cercana y estrategias de autocuidado adaptadas a tus necesidades y objetivos.", 100, "4.9", 67, "Hoy, 16:00")
    )
    fun find(id: String) = all.firstOrNull { it.id == id } ?: all.first()
}
