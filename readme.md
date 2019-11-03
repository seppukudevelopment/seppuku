# ![Seppuku](res/seppuku_full.png)

Seppuku is a free, lightweight, open-source Minecraft Forge client-side mod for Minecraft 1.12.2. Oriented towards 9B9T, this is a full-featured anarchy mod with an external plugin API, unique exploits, and a solid community.

# Requirements
- **JDK 8** (https://adoptopenjdk.net/, https://aws.amazon.com/it/corretto/)
- __(optional)__ **Git**

# Building

### Linux / Mac
1. Clone the repository `git clone git@github.com:seppukudevelopment/seppuku.git`
2. Run `gradlew setupDecompWorkspace`
3. Edit `src/main/java/me/rigamortis/seppuku/impl/fml/core/SeppukuClassTransformer.java` change `PatchManager.Environment.IDE` to `PatchManager.Environment.RELEASE`
4. Run `gradlew build`

### Windows
> Highly recommend using a git shell for Windows and using the linux guide above. (https://git-scm.com/downloads) 

#### IDE
1. Clone the repository
2. Import the project through Gradle via `build.gradle` (simple tutorials online for [intellij](https://stackoverflow.com/questions/31256356/how-to-import-gradle-projects-in-intellij), [eclipse](https://stackoverflow.com/questions/10722773/import-existing-gradle-git-project-into-eclipse), etc.)
3. Run the gradle command `setupDecompWorkspace` via the IDE or gradlew.bat file (via command prompt: `gradlew.bat setupDecompWorkspace`) 
4. Refresh the project (reload ide / refresh gradle workspace)
5. Edit `src/main/java/me/rigamortis/seppuku/impl/fml/core/SeppukuClassTransformer.java` change `PatchManager.Environment.IDE` to `PatchManager.Environment.RELEASE`
6. Run the gradle command `build` via the IDE or gradlew.bat file (via command prompt: `gradlew.bat build`) 

# Debugging
- Use VM arg `-Dfml.coreMods.load=me.rigamortis.seppuku.impl.fml.core.SeppukuLoadingPlugin`
- Ensure field `PATCH_MANAGER` in **SeppukuClassTransformer.java** is set to `PatchManager.Environment.IDE`
