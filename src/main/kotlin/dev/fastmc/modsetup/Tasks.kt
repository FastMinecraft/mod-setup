package dev.fastmc.modsetup

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject

abstract class AtPatchTask @Inject constructor(private val patches: Map<String, String>, private val jarTask: Jar) :
    DefaultTask() {
    init {
        jarTask.finalizedBy(this)
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    @get:InputFile
    val inputFile by lazy { jarTask.outputs.files.singleFile!! }

    @get:OutputFile
    val outputFile by lazy { File(inputFile.parent, inputFile.name.replace("remapped", "patched")) }

    @TaskAction
    fun patch() {
        val zipFile = ZipFile(inputFile)
        val oldText = zipFile.getInputStream(zipFile.getEntry("META-INF/accesstransformer.cfg"))
            .readBytes()
            .decodeToString()
        val text = patches.entries.fold(oldText) { prev, it ->
            prev.replace(it.key, it.value)
        }

        val cacheZipEntries = zipFile.entries().asSequence()
            .filter { it.name != "META-INF/accesstransformer.cfg" }
            .map { ZipEntry(it.name) to zipFile.getInputStream(it).readBytes() }
            .toList()

        ZipOutputStream(outputFile.outputStream().buffered(1024 * 1024)).use { zip ->
            cacheZipEntries.forEach { (zipEntry, bytes) ->
                zip.putNextEntry(zipEntry)
                zip.write(bytes)
                zip.closeEntry()
            }

            zip.putNextEntry(ZipEntry("META-INF/accesstransformer.cfg"))
            zip.write(text.encodeToByteArray())
            zip.closeEntry()
        }
    }
}