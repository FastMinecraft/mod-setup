package dev.fastmc.modsetup

import dev.architectury.plugin.ArchitectPluginExtension
import dev.fastmc.loader.ModLoaderExtension
import dev.fastmc.remapper.FastRemapperExtension
import dev.luna5ama.jaroptimizer.JarOptimizerExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.dsl.ArtifactHandler
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.file.CopySpec
import org.gradle.api.java.archives.Manifest
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.net.URI
import kotlin.reflect.KProperty

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
sealed class ProjectConfigure(name: String, val project: Project) {
    protected val rootProject
        get() = project.rootProject

    init {
        project.extraProperties["mod-setup.$name"] = this
    }

    abstract fun configure()

    protected operator fun Project.getValue(obj: Any, property: KProperty<*>): String {
        return this.property(property.name) as String
    }

    protected operator fun <T> NamedDomainObjectProvider<T>.invoke(block: T.() -> Unit) {
        this.configure(block)
    }

    protected val Project.sourceSets
        get() = extensions.getByName("sourceSets") as SourceSetContainer

    protected fun Project.sourceSets(block: SourceSetContainer.() -> Unit) {
        this.extensions.configure("sourceSets", block)
    }

    protected val Project.architectury
        get() = extensions.getByName("architectury") as ArchitectPluginExtension

    protected fun Project.architectury(block: ArchitectPluginExtension.() -> Unit) {
        this.extensions.configure("architectury", block)
    }

    protected val Project.architecturyProject
        get() = this.extensions.getByType(ArchitecturyProjectExtension::class.java)

    protected val Project.loom
        get() = extensions.getByName("loom") as LoomGradleExtensionAPI

    protected fun Project.loom(block: LoomGradleExtensionAPI.() -> Unit) {
        this.extensions.configure("loom", block)
    }

    protected val Project.base
        get() = extensions.getByType(BasePluginExtension::class.java)

    protected fun Project.base(block: BasePluginExtension.() -> Unit) {
        this.extensions.configure(BasePluginExtension::class.java, block)
    }

    protected val Project.idea
        get() = extensions.getByName("idea") as IdeaModel

    protected fun Project.idea(block: IdeaModel.() -> Unit) {
        this.extensions.configure("idea", block)
    }

    protected val Project.java
        get() = extensions.getByName("java") as JavaPluginExtension

    protected fun Project.java(block: JavaPluginExtension.() -> Unit) {
        this.extensions.configure("java", block)
    }

    protected val Project.modLoader
        get() = extensions.getByType(ModLoaderExtension::class.java)

    protected fun Project.modLoader(block: ModLoaderExtension.() -> Unit) {
        this.extensions.configure(ModLoaderExtension::class.java, block)
    }

    protected val Project.jarOptimizer
        get() = extensions.getByType(JarOptimizerExtension::class.java)

    protected fun Project.jarOptimizer(block: JarOptimizerExtension.() -> Unit) {
        this.extensions.configure(JarOptimizerExtension::class.java, block)
    }

    protected val Project.fastRemapper
        get() = extensions.getByType(FastRemapperExtension::class.java)

    protected fun Project.fastRemapper(block: FastRemapperExtension.() -> Unit) {
        this.extensions.configure(FastRemapperExtension::class.java, block)
    }


    protected fun Project.dependencies(block: DependencyHandler.() -> Unit) {
        this.dependencies.block()
    }

    protected fun Project.repositories(block: RepositoryHandler.() -> Unit) {
        this.repositories.block()
    }

    protected fun Project.configurations(block: ConfigurationContainer.() -> Unit) {
        this.configurations.block()
    }

    protected fun RepositoryHandler.maven(url: String) {
        this.maven { it.url = URI(url) }
    }

    protected val TaskContainer.jar
        get() = named<Jar>("jar")

    protected val TaskContainer.remapJar
        get() = named<RemapJarTask>("remapJar")

    protected val TaskContainer.processResources
        get() = named<AbstractCopyTask>("processResources")

    protected val TaskContainer.modLoaderJar
        get() = named<Jar>("modLoaderJar")

    protected val Provider<out Jar>.outputManifest
        get() = this.map { project.zipTree(it.archiveFile) }
            .map { zipTree -> zipTree.find { it.name == "MANIFEST.MF" }!! }

    protected val Jar.outputManifest
        get() = project.provider { project.zipTree(this.archiveFile) }
            .map { zipTree -> zipTree.find { it.name == "MANIFEST.MF" }!! }

    protected inline fun Project.pluginManager(block: PluginManager.() -> Unit) {
        this.pluginManager.apply(block)
    }

    protected fun DependencyHandler.project(path: String): ProjectDependency {
        return project(mapOf("path" to path)) as ProjectDependency
    }

    protected fun DependencyHandler.project(path: String, configuration: String): Dependency {
        return project(mapOf("path" to path, "configuration" to configuration))
    }

    protected fun DependencyHandler.add(
        configuration: String,
        dependencyNotation: Any,
        block: ExternalModuleDependency.() -> Unit
    ) {
        val dependency = create(dependencyNotation) as ExternalModuleDependency
        dependency.block()
        add(configuration, dependency)
    }

    protected inline fun <reified T : Task> TaskContainer.named(name: String): TaskProvider<T> {
        return this.named(name, T::class.java)
    }

    protected inline fun <reified T : Task> TaskContainer.named(
        name: String,
        noinline block: T.() -> Unit
    ): TaskProvider<T> {
        return this.named(name, T::class.java, block)
    }

    protected inline fun <reified T> NamedDomainObjectCollection<in T>.named(name: String): NamedDomainObjectProvider<T> {
        return this.named(name, T::class.java)
    }

    protected inline fun <reified T> NamedDomainObjectCollection<in T>.named(
        name: String,
        noinline block: T.() -> Unit
    ): NamedDomainObjectProvider<T> {
        return this.named(name, T::class.java, block)
    }

    protected inline fun <reified T> PolymorphicDomainObjectContainer<in T>.register(name: String): NamedDomainObjectProvider<T> {
        return this.register(name, T::class.java)
    }

    protected inline fun <reified T> PolymorphicDomainObjectContainer<in T>.register(
        name: String,
        noinline block: T.() -> Unit
    ): NamedDomainObjectProvider<T> {
        return this.register(name, T::class.java, block)
    }

    protected inline fun <reified T> PolymorphicDomainObjectContainer<in T>.create(name: String): T {
        return this.create(name, T::class.java)
    }

    protected inline fun <reified T> PolymorphicDomainObjectContainer<in T>.create(
        name: String,
        noinline block: T.() -> Unit
    ): T {
        return this.create(name, T::class.java, block)
    }

    protected fun AbstractCopyTask.fromConfiguration(name: String) {
        fromConfiguration(project.provider { project.configurations.named(name) }.flatten())
    }

    protected fun AbstractCopyTask.fromConfiguration(name: String, block: CopySpec.() -> Unit) {
        fromConfiguration(project.provider { project.configurations.named(name) }.flatten(), block)
    }

    protected fun AbstractCopyTask.fromConfiguration(configuration: Provider<Configuration>) {
        dependsOn(configuration)
        from(configuration.flatMap { it.asCopySource() })
    }

    protected fun AbstractCopyTask.fromConfiguration(
        configuration: Provider<Configuration>,
        block: CopySpec.() -> Unit
    ) {
        dependsOn(configuration)
        from(configuration.flatMap { it.asCopySource() }, block)
    }

    protected fun AbstractCopyTask.fromConfiguration(configuration: Configuration) {
        dependsOn(configuration)
        from(configuration.asCopySource())
    }

    protected fun AbstractCopyTask.fromConfiguration(configuration: Configuration, block: CopySpec.() -> Unit) {
        dependsOn(configuration)
        from(configuration.asCopySource(), block)
    }

    protected fun Configuration.asCopySource(): Provider<List<Any>> {
        return this.elements.map { set ->
            set.map { it.asFile }.map { if (it.isDirectory) it else project.zipTree(it) }
        }
    }


    protected fun AbstractCopyTask.fromJarTask(jarTaskName: String) {
        fromJarTask(project.provider { project.tasks.named<Jar>(jarTaskName) }.flatten())
    }

    protected fun AbstractCopyTask.fromJarTask(jarTaskName: String, block: CopySpec.() -> Unit) {
        fromJarTask(project.provider { project.tasks.named<Jar>(jarTaskName) }.flatten(), block)
    }

    protected fun AbstractCopyTask.fromJarTask(jarTask: Provider<out Jar>) {
        dependsOn(jarTask)
        from(jarTask.map { project.zipTree(it.archiveFile) })
    }

    protected fun AbstractCopyTask.fromJarTask(jarTask: Provider<Jar>, block: CopySpec.() -> Unit) {
        dependsOn(jarTask)
        from(jarTask.map { project.zipTree(it.archiveFile) }, block)
    }

    protected fun AbstractCopyTask.fromJarTask(jarTask: Jar) {
        dependsOn(jarTask)
        from(project.zipTree(jarTask.archiveFile))
    }

    protected fun AbstractCopyTask.fromJarTask(jarTask: Jar, block: CopySpec.() -> Unit) {
        dependsOn(jarTask)
        from(project.zipTree(jarTask.archiveFile), block)
    }

    protected fun ArtifactHandler.add(
        configuration: NamedDomainObjectProvider<Configuration>,
        jar: Provider<out AbstractArchiveTask>
    ) {
        add(configuration.name, jar)
    }

    protected fun ArtifactHandler.add(configuration: Configuration, jar: Provider<out AbstractArchiveTask>) {
        add(configuration.name, jar)
    }

    protected fun Manifest.attributes(vararg args: Pair<String, Any>) {
        attributes(args.toMap())
    }
}
