package us.tlatoani.mundocore.gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class MundoCoreGradlePlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.plugins.apply 'maven-publish'
        target.plugins.apply 'com.github.johnrengelman.shadow'
        target.plugins.apply 'java'
        target.plugins.apply 'net.minecrell.plugin-yml.bukkit'
        target.plugins.apply 'org.hidetake.ssh'

        target.sourceCompatibility = '1.8'

        target.repositories {
            mavenCentral()
            mavenLocal()

            // Spigot
            maven { url = 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/' }

            // Bungeecord ChatComponent-API (Spigot dependency)
            maven { url = 'https://oss.sonatype.org/content/repositories/snapshots' }

            // Skript (Njol)
            maven { url 'http://maven.njol.ch/repo/' }

            // Skript (Bensku)
            maven { url 'https://jitpack.io' }

            // Paper
            maven { url 'https://repo.destroystokyo.com/repository/maven-public/' }

            // WorldGuard (jesus djrist)
            maven { url 'http://maven.sk89q.com/repo/' }

            // bStats
            maven { url 'https://repo.codemc.org/repository/maven-public' }
        }

        target.dependencies {
            //compileOnly 'org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT'
            //compileOnly 'ch.njol:skript:2.2-SNAPSHOT'
            compileOnly 'commons-io:commons-io:2.5'
        }

        target.sourceSets {
            main {
                java {
                    srcDirs = ['src']
                    outputDir = target.file('build/classes')
                }
                resources {
                    srcDirs = ['resources']
                }
                output.resourcesDir = target.file('build/resources')
            }
        }

        target.build.dependsOn(target.shadowJar)
        target.jar.enabled = false

        MundoExtension extension = new MundoExtension()
        target.extensions.add('mundo', extension)
        target.afterEvaluate {
            if (target.group == 'us.tlatoani') {
                if (!extension.authors.contains('Tlatoani')) {
                    extension.author 'Tlatoani'
                }
                if (extension.website == null) {
                    extension.website 'tlatoani.us/' + target.name.toLowerCase()
                }
                if (extension.mainPackage == null) {
                    extension.mainPackage 'us.tlatoani.' + target.name.toLowerCase()
                }
                if (extension.mainClass == null) {
                    extension.mainClass target.name
                }
            }
            if (extension.command.name == null) {
                extension.command.name target.name.toLowerCase()
            }
            if (extension.permission == null) {
                extension.permission target.name.toLowerCase()
            }
            target.repositories {
                if (extension.bStatsVersion != null) {
                    maven { url = 'https://repo.bstats.org/content/repositories/releases'}
                }
                if (extension.protocolLibVersion != null) {
                    maven { url = 'http://repo.dmulloy2.net/nexus/repository/public/'}
                }
            }

            target.dependencies {
                for (String coreModule : extension.coreModules) {
                    compile 'us.tlatoani:MundoCore-' + coreModule
                }
                if (extension.bStatsVersion != null) {
                    compile 'org.bstats:bstats-bukkit:' + extension.bStatsVersion
                }
                if (extension.protocolLibVersion != null) {
                    compileOnly 'com.comphenix.protocol:ProtocolLib-API:' + extension.protocolLibVersion
                }
                if (extension.spigotVersion != null) {
                    compileOnly ('org.spigotmc:spigot-api:' + extension.spigotVersion) {
                        transitive = false
                    }
                }
                if (extension.skriptVersion != null) {
                    compileOnly 'com.github.SkriptLang:Skript:' + extension.skriptVersion
                }
            }

            target.shadowJar {
                classifier = null
                relocate 'us.tlatoani.mundocore', extension.mainPackage + '.core'
                if (extension.bStatsVersion != null) {
                    relocate 'org.bstats', extension.mainPackage + '.core.bstats'
                }
            }

            target.task('cleanLibs') {
                File jarDir = target.shadowJar.destinationDir
                File[] files
                if (jarDir.exists() && jarDir.isDirectory() && (files = jarDir.listFiles()) != null) {
                    for (File file : files) {
                        if (!file.isDirectory() && file.name.endsWith('.jar') && file.name != target.shadowJar.archiveName) {
                            file.delete()
                        }
                    }
                }
            }

            target.shadowJar.dependsOn(target.cleanLibs)

            if (extension.protocolLibVersion != null) {
                extension.depend 'ProtocolLib'
            }

            target.bukkit {
                name = target.name
                version = target.version
                description = target.description
                authors = extension.authors
                website = extension.website
                main = extension.mainPackage + '.' + extension.mainClass
                depend = extension.depend
                commands {
                    create extension.command.name, {
                        description = extension.command.description
                        permission = extension.permission + '.' + extension.command.permissionSuffix
                        permissionMessage = extension.command.permissionMessage
                    }
                }
                permissions {
                    create extension.permission + '.*', {
                        description = 'Permission for things related to ' + target.name
                        children = [extension.permission + '.' + extension.command.permissionSuffix]
                    }
                    create extension.permission + '.' + extension.command.permissionSuffix, {
                        description = 'Permission to run the /' + extension.command.name + ' command'
                        setDefault('OP')
                    }
                }
                if (extension.spigotVersion.contains('1.13')) {
                    apiVersion = '1.13'
                }
            }

            target.publishing {
                publications {
                    MavenJava(MavenPublication) { publication ->
                        target.shadow.component(publication)
                    }
                }

                repositories {
                    maven {
                        name 'PersonalRepo'
                        url extension.personalRepo
                    }
                }
            }

            if (target.group == 'us.tlatoani' && extension.mundoServerIdentity != null) {
                target.remotes {
                    mundoServer {
                        host = 'tlatoani.us'
                        user = 'ubuntu'
                        identity = target.file(extension.mundoServerIdentity)
                    }
                }

                target.task('createMundoServerJSONFile').doLast {
                    target.file('build/mundo_server').mkdirs()
                    File jsonFile = target.file('build/mundo_server/addon.json')
                    jsonFile.text = '{"capitalizedName":"' + target.name + '"}'
                }

                target.task('createVersionSummaryFile').doLast {
                    target.file('build/mundo_server').mkdirs()
                    File summaryFile = target.file('build/mundo_server/version_summary.txt')
                    summaryFile.text = extension.versionSummary
                }

                target.task([dependsOn: 'createMundoServerJSONFile'], 'releaseMundoServerJSONFile').doLast {
                    target.ssh.run {
                        session(target.remotes.mundoServer) {
                            execute 'mkdir -p mundo_server/addons/' + target.name.toLowerCase()
                            put from: target.file('build/mundo_server/addon.json').toString(), into: 'mundo_server/addons/' + target.name.toLowerCase() + '/addon.json'
                            execute 'tmux send-keys -t mundo_server \'' + target.name.toLowerCase() + ' reload jsonfile\' Enter'
                        }
                    }
                }

                target.task([dependsOn: ['build', 'releaseMundoServerJSONFile']], 'releaseSnapshot').doLast {
                    target.ssh.run {
                        session(target.remotes.mundoServer) {
                            put from: target.shadowJar.destinationDir.toString() + '/' + target.shadowJar.archiveName, into: 'mundo_server/addons/' + target.name.toLowerCase() + '/snapshot.jar'
                            execute 'tmux send-keys -t mundo_server \'' + target.name.toLowerCase() + ' release snapshot ' + target.version + '\' Enter'
                        }
                    }
                }

                target.task([dependsOn: ['build', 'releaseMundoServerJSONFile', 'createVersionSummaryFile', 'publishMavenJavaPublicationToPersonalRepoRepository']], 'releaseBeta').doLast {
                    target.file('build/mundo_server').mkdirs()
                    File jsonFile = target.file('build/mundo_server/version.json')
                    jsonFile.text = '{"version_string":"' + target.version + '","stage":"beta","location":"maven","release_time":' + System.currentTimeMillis() + '}'
                    target.ssh.run {
                        session(target.remotes.mundoServer) {
                            execute 'mkdir -p mundo_server/addons/' + target.name.toLowerCase() + '/versions/' + target.version.toString().toLowerCase()
                            put from: target.file('build/mundo_server/version.json').toString(), into: 'mundo_server/addons/' + target.name.toLowerCase() + '/versions/' + target.version.toString().toLowerCase() + '/version.json'
                            put from: target.file('build/mundo_server/version_summary.txt').toString(), into: 'mundo_server/addons/' + target.name.toLowerCase() + '/versions/' + target.version.toString().toLowerCase() + '/summary.txt'
                            execute 'tmux send-keys -t mundo_server \'' + target.name.toLowerCase() + ' release beta ' + target.version + '\' Enter'
                        }
                    }
                }

                target.task([dependsOn: ['build', 'releaseMundoServerJSONFile', 'createVersionSummaryFile', 'publishMavenJavaPublicationToPersonalRepoRepository']], 'releaseStable').doLast {
                    target.file('build/mundo_server').mkdirs()
                    File jsonFile = target.file('build/mundo_server/version.json')
                    jsonFile.text = '{"version_string":"' + target.version + '","stage":"stable","location":"maven","release_time":' + System.currentTimeMillis() + '}'
                    target.ssh.run {
                        session(target.remotes.mundoServer) {
                            execute 'mkdir -p mundo_server/addons/' + target.name.toLowerCase() + '/versions/' + target.version.toString().toLowerCase()
                            put from: target.file('build/mundo_server/version.json').toString(), into: 'mundo_server/addons/' + target.name.toLowerCase() + '/versions/' + target.version.toString().toLowerCase() + '/version.json'
                            put from: target.file('build/mundo_server/version_summary.txt').toString(), into: 'mundo_server/addons/' + target.name.toLowerCase() + '/versions/' + target.version.toString().toLowerCase() + '/summary.txt'
                            execute 'tmux send-keys -t mundo_server \'' + target.name.toLowerCase() + ' release stable ' + target.version + '\' Enter'
                        }
                    }
                }
            }
        }
    }
}
