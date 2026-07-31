package com.ubuterm.distro

data class InstallPlan(
    val name: String,
    val description: String,
    val version: String,
    val arch: String,
    val sizeMb: Long,
    val packages: List<String>
) {
    companion object {
        fun fromYaml(raw: String): InstallPlan {
            val lines = raw.lineSequence().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
            var name = ""
            var description = ""
            var version = ""
            var arch = ""
            var sizeMb = 0L
            val packages = mutableListOf<String>()
            var inPackages = false
            for (line in lines) {
                when {
                    line == "packages:" -> inPackages = true
                    line.startsWith("- ") -> if (inPackages) packages.add(line.removePrefix("- ").trim())
                    line.startsWith("name:") -> name = line.removePrefix("name:").trim().trim('"')
                    line.startsWith("description:") -> description = line.removePrefix("description:").trim().trim('"')
                    line.startsWith("version:") -> version = line.removePrefix("version:").trim().trim('"')
                    line.startsWith("arch:") -> arch = line.removePrefix("arch:").trim().trim('"')
                    line.startsWith("size_mb:") -> sizeMb = line.removePrefix("size_mb:").trim().toLongOrNull() ?: 0L
                }
            }
            return InstallPlan(name, description, version, arch, sizeMb, packages)
        }
    }
}
