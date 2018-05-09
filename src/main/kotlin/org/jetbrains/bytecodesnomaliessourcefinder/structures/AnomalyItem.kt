package org.jetbrains.bytecodesnomaliessourcefinder.structures

data class AnomalyItem(
        val jsonPath: String,
        val classPath: String,
        val anomalyValue: Float,
        val sourceFile: String?,
        val psiFile: String?
)