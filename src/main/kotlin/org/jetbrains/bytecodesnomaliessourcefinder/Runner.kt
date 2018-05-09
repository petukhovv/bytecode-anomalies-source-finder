package org.jetbrains.bytecodesnomaliessourcefinder

import com.fasterxml.jackson.core.type.TypeReference
import org.jetbrains.bytecodesnomaliessourcefinder.io.FileWriter
import org.jetbrains.bytecodesnomaliessourcefinder.io.JsonFilesReader
import org.jetbrains.bytecodesnomaliessourcefinder.structures.AnomalyItem
import org.jetbrains.bytecodesnomaliessourcefinder.structures.BytecodeToSourceItem
import java.io.File

typealias Anomalies = List<List<Any>>

object Runner {
    private const val SOURCE_BYTECODE_MAP_FILE = "bytecode_to_source_map.json"

    private fun findSourceFile(anomalyClassPath: String, repoFolder: String): Pair<String?, String?> {
        val anomalyClassPathSplited = anomalyClassPath.split("/")
        val sourceBytecodeMapFile = File("$repoFolder/${anomalyClassPathSplited[0]}/${anomalyClassPathSplited[1]}/$SOURCE_BYTECODE_MAP_FILE")

        if (!sourceBytecodeMapFile.exists()) {
            println("MAP FILE NOT FOUND")
            return Pair(null, null)
        }

        val bytecodeToSourceItemReference = object: TypeReference<Map<String, BytecodeToSourceItem>>() {}
        val bytecodeToSourceMap = JsonFilesReader.readFile<Map<String, BytecodeToSourceItem>>(sourceBytecodeMapFile, bytecodeToSourceItemReference)
        val classAbsolutePath = File("$repoFolder/$anomalyClassPath").absolutePath

        if (bytecodeToSourceMap.contains(classAbsolutePath)) {
            val sourceFile = bytecodeToSourceMap[classAbsolutePath]!!.file
            val psiFile = "${sourceFile.replace("/sources/", "/cst/")}.json"

            println("$sourceFile — $psiFile")
            return Pair(sourceFile, psiFile)
        }

        println("$anomalyClassPath NOT FOUND IN MAP FILE")
        return Pair("", "")
    }

    private fun buildBytecodeToSourceMap(anomalies: Anomalies, repoFolder: String): List<AnomalyItem> {
        val anomaliesStatistic = mutableListOf<AnomalyItem>()

        anomalies.forEach {
            val anomalyPath = it[0].toString()
            val anomalyValue = it[1].toString().toFloat()
            val anomalyJsonFilename = anomalyPath.split("/").last()
            val anomalyClassName = anomalyJsonFilename.replace(".class.json", "").split(".").last()
            val anomalyClassPath = anomalyPath.replace(anomalyJsonFilename, "$anomalyClassName.class")
            val (anomalySourceFile, anomalyPsiFile) = findSourceFile(anomalyClassPath, repoFolder)

            anomaliesStatistic.add(
                    AnomalyItem(anomalyPath, anomalyClassPath, anomalyValue, anomalySourceFile, anomalyPsiFile)
            )
        }

        return anomaliesStatistic
    }

    fun run(anomaliesFile: String, repoFolder: String, anomaliesWithSourcesFile: String) {
        val anomaliesReference = object: TypeReference<Anomalies>() {}
        val anomalies = JsonFilesReader.readFile<Anomalies>(File(anomaliesFile), anomaliesReference)
        val anomaliesStatistic = buildBytecodeToSourceMap(anomalies, repoFolder)

        FileWriter.write(anomaliesWithSourcesFile, anomaliesStatistic)
    }
}