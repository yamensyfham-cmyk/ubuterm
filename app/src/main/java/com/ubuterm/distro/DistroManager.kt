package com.ubuterm.distro

import android.content.Context
import android.util.Log
import com.ubuterm.settings.PreferencesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class DistroManager(private val context: Context) {

    val rootfsDir: File
        get() = File(context.getExternalFilesDir(null), "rootfs")

    val archivesDir: File
        get() = File(context.getExternalFilesDir(null), "archives")

    val installedMarker: File
        get() = File(rootfsDir, ".ubuterm-installed")

    val versionMarker: File
        get() = File(rootfsDir, ".ubuterm-version")

    suspend fun isInstalled(): Boolean = withContext(Dispatchers.IO) {
        PreferencesStore.installed.first() && installedMarker.exists()
    }

    suspend fun install(profile: InstallPlan, archive: File, onProgress: (String) -> Unit): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (isInstalled()) error("Distribution already installed. Remove it first.")
                if (rootfsDir.exists()) rootfsDir.deleteRecursively()
                rootfsDir.mkdirs()
                archivesDir.mkdirs()

                onProgress("Verifying checksum")
                verifyChecksum(archive)

                onProgress("Extracting rootfs (${profile.sizeMb} MB expected)")
                extractArchive(archive, rootfsDir)

                onProgress("Writing bootstrap scripts")
                writeBootstrapScripts()

                onProgress("Running first boot setup")
                runFirstBoot()

                versionMarker.writeText(profile.name)
                PreferencesStore.markInstalled(profile.name)
                onProgress("Install complete")
            }
        }

    suspend fun remove(keepBackup: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (keepBackup) {
                val backup = File(archivesDir, "rootfs-backup-${System.currentTimeMillis()}.tar.zst")
                tarRootfs(rootfsDir, backup)
            }
            rootfsDir.deleteRecursively()
            PreferencesStore.markRemoved()
        }
    }

    suspend fun repair(onProgress: (String) -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!isInstalled()) error("Nothing to repair")
            onProgress("Reconfiguring dpkg")
            runInRootfs("dpkg --configure -a")
            onProgress("Fixing permissions")
            runInRootfs("find / -xdev -type f -name '*.so' -exec chmod 755 {} +")
            onProgress("Cleaning apt caches")
            runInRootfs("apt-get clean")
            onProgress("Repair complete")
        }
    }

    private fun verifyChecksum(archive: File) {
        val expected = File(archive.parentFile, "${archive.name}.sha256")
        if (!expected.exists()) return
        val actual = sha256(archive)
        val wanted = expected.readText().trim().split(' ')[0]
        if (!actual.equals(wanted, ignoreCase = true)) error("Checksum mismatch: $actual != $wanted")
    }

    private fun extractArchive(archive: File, target: File) {
        if (!archive.exists()) error("Archive missing: ${archive.name}")
        val builder = ProcessBuilder(
            listOf("tar", "--exclude", "dev/*", "--exclude", "proc/*", "--exclude", "sys/*", "-xf", archive.absolutePath, "-C", target.absolutePath)
        )
        builder.redirectErrorStream(true)
        val proc = builder.start()
        proc.inputStream.readBytes()
        val code = proc.waitFor()
        if (code != 0) error("Extract failed with code $code")
    }

    private fun writeBootstrapScripts() {
        rootfsDir.mkdirs()
        runInRootfs("mkdir -p /home/user /etc/apt /var/lib/dpkg /var/cache/apt")
        runInRootfs("echo 'deb http://ports.ubuntu.com/ubuntu-ports jammy main restricted universe multiverse' > /etc/apt/sources.list")
        runInRootfs("echo 'user:x:1000:1000::/home/user:/bin/bash' > /etc/passwd")
        runInRootfs("echo 'user:x:1000:' > /etc/group")
    }

    private fun runFirstBoot() {
        runInRootfs("apt-get update -qq && apt-get install -y --no-install-recommends bash coreutils procps ca-certificates curl nano locales sudo || true")
        runInRootfs("echo 'en_US.UTF-8 UTF-8' > /etc/locale.gen; locale-gen || true")
        runInRootfs("ln -sf /usr/share/zoneinfo/UTC /etc/localtime || true")
    }

    private fun tarRootfs(source: File, dest: File) {
        val builder = ProcessBuilder(listOf("tar", "-czf", dest.absolutePath, "-C", source.parentFile.absolutePath, source.name))
        builder.redirectErrorStream(true)
        val proc = builder.start()
        proc.inputStream.readBytes()
        if (proc.waitFor() != 0) error("Backup failed")
    }

    private fun runInRootfs(command: String) {
        val sh = File(rootfsDir, "bin/sh")
        if (!sh.exists()) return
        val builder = ProcessBuilder(sh.absolutePath, "-c", command)
        builder.redirectErrorStream(true)
        val proc = builder.start()
        val out = proc.inputStream.readBytes().toString(Charsets.UTF_8)
        val code = proc.waitFor()
        if (code != 0) Log.w(TAG, "rootfs cmd failed ($code): $command\n$out")
    }

    private fun sha256(file: File): String {
        val builder = ProcessBuilder("sha256sum", file.absolutePath)
        builder.redirectErrorStream(true)
        val proc = builder.start()
        val out = proc.inputStream.readBytes().toString(Charsets.UTF_8)
        proc.waitFor()
        return out.trim().split(' ').firstOrNull() ?: ""
    }

    companion object {
        private const val TAG = "DistroManager"
    }
}
