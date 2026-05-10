Sport Manager — release layout
================================

Contents (after build):
  SportManager.bat   … Double-click or run from cmd to start the app
  sport-manager.jar … Your application classes only
  lib\               … OpenJFX + transitive jars from Maven (ship these for offline use)

If lib\ is missing or incomplete (first launch without zip libs):
  SportManager.bat runs a classpath-only step that downloads OpenJFX into lib\ from
  Maven Central (internet required once). Then the game restarts with module-path as usual.

Requirements (per course policy):
  • JDK or JRE 17 or newer installed on the machine
  • java.exe on PATH (no JRE shipped with this zip — only JavaFX + other libs)

Build the zip from source (team machine):
  mvn clean package -DskipTests

  (Tests need a working JavaFX display/natives; run `mvn test` on a machine with GPU/UI.)

If `clean` fails with "Failed to delete ...\target\...":
  Close Sport Manager and any running tests; exit debug sessions that load target\classes.
  Then run `mvn clean package -DskipTests` again. Or use package-release.bat — it falls back to
  `mvn package -DskipTests` without clean when delete is blocked.

Artifact:
  target\sport-manager-release.zip

Submit / install:
  • Extract the zip anywhere (ASCII path recommended on Windows).
  • Run SportManager.bat

Note for markers:
  • Libraries are declared in pom.xml; `maven-dependency-plugin` copies them into lib\ at package time
    (this avoids noisy “Failed to build parent project for org.openjfx…” assembly warnings).
  • JavaFX native libraries are extracted automatically on first run (cache under
    %PUBLIC%\SportManagerJavaFXCache on Windows, ~/.sportmanager/javafx-cache elsewhere).
