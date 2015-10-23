@ECHO OFF


set JARPATH="%CD%\Java Library\Replay.jar"

echo [Checking PATH Variable]
java.exe -version >nul 2>&1
if ERRORLEVEL 1 goto :SearchJava
ECHO [Java found in path]
java.exe -jar %JARPATH% %*
if ERRORLEVEL 0 (
ECHO Client executed successfully
EXIT /B
)


:SearchJava
ECHO [Searching for Java]
FOR /D %%G IN (C:/"Program Files (x86)"/Java/*) DO (
for /F "tokens=1,2 delims=: " %%a in ("%%G") do (
C:/"Program Files (x86)"/Java/"%%b"/bin/java.exe -version
C:/"Program Files (x86)"/Java/"%%b"/bin/java.exe -jar %JARPATH% %*
if ERRORLEVEL 0 (
ECHO Program executed successfully
EXIT /B
)
)
)

FOR /D %%G IN (C:/"Program Files"/Java/*) DO (
for /F "tokens=1,2 delims=: " %%a in ("%%G") do (
C:/"Program Files"/Java/"%%b"/bin/java.exe -version
C:/"Program Files"/Java/"%%b"/bin/java.exe -jar %JARPATH% %*
if ERRORLEVEL 0 (
ECHO Program executed successfully
EXIT /B
)
)
)

:usage
echo java.exe was not found on your path!
echo This script requires:
echo 1) an installation of the Java Development Kit or an installation of the Java Runtime Environment, and
echo 2) either java.exe is on the path, or that the one of JAVA_HOME, JDK_HOME or JRE_HOME environment variables 
echo is set and points to the installation directory. If not, it will look under
echo C:\Program Files\Java\ and C:\Program Files (x86)\Java\ for any JDK or JRE of version 1.6 or greater.

runas /user:# "" >nul 2>&1