# ![Seppuku](res/seppuku_full.png)

![Website](https://img.shields.io/website?down_color=lightgrey&down_message=offline&up_color=darkgreen&up_message=online&url=https%3A%2F%2Fseppuku.pw%2F)
![Discord](https://img.shields.io/discord/579516739092480000?color=lightblue)
![License](https://img.shields.io/github/license/seppukudevelopment/seppuku)
![GitHub Version](https://img.shields.io/github/v/release/seppukudevelopment/seppuku)
![GitHub Lines](https://img.shields.io/tokei/lines/github/seppukudevelopment/seppuku)
![GitHub Contributors](https://img.shields.io/github/contributors/seppukudevelopment/seppuku?color=lightgrey)
![GitHub Language](https://img.shields.io/github/languages/top/seppukudevelopment/seppuku?color=9900ee)
![Downloads](https://img.shields.io/github/downloads/seppukudevelopment/seppuku/total?color=9900ee)

Seppuku is a free, lightweight, open-source [_Minecraft Forge_](https://files.minecraftforge.net/) mod for Minecraft 1.12.2, and soon to be for recent versions...

Originally oriented towards the 9B9T and 2B2T anarchy servers; it is a fully featured client-side mod with an external plugin API, unique exploits, and a [solid Discord community](https://discord.gg/UzWBZPe).

Check the [guide](https://seppuku.pw/guide.html) for help.

# Requirements
- **JDK 8** (https://adoptopenjdk.net/, https://aws.amazon.com/corretto/)
- __(optional)__ **Git**

# Building

### Linux / Mac
1. Clone the repository: `git clone git@github.com:seppukudevelopment/seppuku.git`
2. Run `./gradlew clean`
3. Run `./gradlew setupDecompWorkspace`
4. Edit `build.gradle` and change field `buildmode` to `RELEASE`. (e.g. `def
 buildmode = "RELEASE"`)
5. Run `./gradlew build`.

Your .jar file is in `build/libs/`.

#### Windows
> Using a git shell for Windows and using the linux guide above is highly recommended. (https://git-scm.com/downloads) 
1. **Clone** the repository.
2. **Import** the project through Gradle via `build.gradle`. *(simple tutorials online for 
[intellij](https://stackoverflow.com/questions/31256356/how-to-import-gradle-projects-in-intellij), 
[eclipse](https://stackoverflow.com/questions/10722773/import-existing-gradle-git-project-into-eclipse), etc...)*
3. Run the Gradle command `clean` via the IDE or the gradlew.bat file. *(via command prompt: `./gradlew.bat clean`)*
4. Run the Gradle command `setupDecompWorkspace` via the IDE or gradlew.bat file. *(via command prompt: `./gradlew.bat setupDecompWorkspace`)*
5. *(for IDE building only <IntelliJ / Eclipse / etc...> )*: **Refresh the project** *(reload ide or refresh gradle workspace)*
6. **Edit** `build.gradle` and change field `buildmode` to `RELEASE` ex: `def
buildmode = "RELEASE"`
7. **Run** the gradle command `build` via the IDE or gradlew.bat file. *(via
 command prompt: `gradlew.bat build`)*
 
Your .jar file is in `build/libs/`.

# Debugging
- Use VM arg `-Dfml.coreMods.load=me.rigamortis.seppuku.impl.fml.core.SeppukuLoadingPlugin`
- Ensure field `buildmode` in **build.gradle** is set to `IDE` ex: `def buildmode = "IDE"`
- Repeat the steps from `step #2` in the **Building** guide written above to fix errors.