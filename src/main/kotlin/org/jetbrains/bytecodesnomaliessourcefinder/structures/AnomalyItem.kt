package org.jetbrains.bytecodesnomaliessourcefinder.structures

data class AnomalyItem(
        private val jsonPath: String,
        private val classPath: String,
        private val anomalyValue: Float,
        private val sourceFile: String?,
        private val psiFile: String?
)