package dev.fastmc.modsetup

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject

abstract class AtPatchTask @Inject constructor(private val patches: Map<String, String>, private val jarTask: Jar) : DefaultTask() {
    init {
        jarTask.finalizedBy(this)
    }

    @get:InputFiles
    val inputs: FileCollection
        get() = jarTask.outputs.files

    @TaskAction
    fun patch() {
        inputs.asSequence()
            .filter { it.isFile }
            .filter { it.extension == "jar" }
            .filter { it.name.contains("unpatched") }
            .forEach {
                patchJarFile(it)
            }
    }

    private fun patchJarFile(file: File) {
        val zipFile = ZipFile(file)
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

        val outputFile = File(file.parent, file.name.replace("unpatched", "release"))
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