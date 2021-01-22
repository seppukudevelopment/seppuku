# ![Seppuku](res/seppuku_full.png)

![Website](https://img.shields.io/website?down_color=lightgrey&down_message=offline&up_color=darkgreen&up_message=online&url=https%3A%2F%2Fseppuku.pw%2F)
![Discord](https://img.shields.io/discord/579516739092480000?color=lightblue)
![License](https://img.shields.io/github/license/seppukudevelopment/seppuku)
![GitHub Version](https://img.shields.io/github/v/release/seppukudevelopment/seppuku)
![GitHub Lines](https://img.shields.io/tokei/lines/github/seppukudevelopment/seppuku)
![GitHub Contributors](https://img.shields.io/github/contributors/seppukudevelopment/seppuku?color=lightgrey)
![GitHub Language](https://img.shields.io/github/languages/top/seppukudevelopment/seppuku?color=9900ee)
![Downloads](https://img.shields.io/github/downloads/seppukudevelopment/seppuku/total?color=9900ee)

Seppuku is a free, open-source and lightweight Minecraft 1.12.2 [Forge](https://files.minecraftforge.net/) mod, and soon to be for more recent versions.

Originally oriented towards the 9B9T and 2B2T anarchy servers; it is a fully featured client-side mod with an external plugin system, unique exploits, and a [solid Discord community](https://discord.gg/UzWBZPe).

Checkout the [guide](https://seppuku.pw/guide.html) for help.

# Requirements
- **JDK 8** ([AdoptOpenJDK](https://adoptopenjdk.net/) or [Corretto](https://aws.amazon.com/corretto/) is recommended)
- **[Git](https://git-scm.com)** (optional)

# Building

### IntelliJ and Eclipse
**Using an IDE like IntelliJ or Eclipse is strongly recommended**
1. Download either [IntelliJ](https://www.jetbrains.com/idea/) or [Eclipse](https://www.eclipse.org/)
2. Clone (or download) the repository: `git clone https://github.com/seppukudevelopment/seppuku`
3. Import the project (steps for [IntelliJ](https://www.jetbrains.com/help/idea/gradle.html#gradle_import_project_start) and [Eclipse](https://stackoverflow.com/questions/10722773/import-existing-gradle-git-project-into-eclipse))
4. Run the Gradle task `clean` via your IDE
5. Run the Gradle task `setupDecompWorkspace` via your IDE
6. Edit `build.gradle` and change the field `buildmode` to `RELEASE` (e.g. `def buildmode = "RELEASE"`)
7. Run the Gradle task `build` via your IDE

The newly built jar file can be found in `build/libs/`.

### Linux, Unix and Mac
1. Clone (or download) the repository: `git clone https://github.com/seppukudevelopment/seppuku`
2. Run `./gradlew clean`
3. Run `./gradlew setupDecompWorkspace`
4. Edit `build.gradle` and change the field `buildmode` to `RELEASE` (e.g. `def buildmode = "RELEASE"`)
5. Run `./gradlew build`

The newly built jar file can be found in `build/libs/`.

### Windows
1. Clone (or download) the repository: `git clone https://github.com/seppukudevelopment/seppuku`
2. Run `./gradlew.bat clean`
3. Run `./gradlew.bat setupDecompWorkspace`
4. Edit `build.gradle` and change the field `buildmode` to `RELEASE` (e.g. `def buildmode = "RELEASE"`)
5. Run `./gradlew.bat build`

The newly built jar file can be found in `build/libs/`.

# Debugging
- Use the JVM argument `-Dfml.coreMods.load=me.rigamortis.seppuku.impl.fml.core.SeppukuLoadingPlugin`
- Ensure the field `buildmode` in `build.gradle` is set to `IDE` (e.g `def buildmode = "IDE"`)
- If any error occurs try running the Gradle task `clean` (see steps under aformentioned building section), if the error persists feel free to ask for help in the [Discord support channel](https://discord.gg/tTu72JEQUm)