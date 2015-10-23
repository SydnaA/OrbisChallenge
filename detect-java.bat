@ECHO OFF

ECHO ==================================
ECHO [Checking PATH variable for Java]
ECHO ==================================
java.exe -version
if ERRORLEVEL 1 goto :SearchJava86
if ERRORLEVEL 0 (
ECHO ========================================================
ECHO Java was successfully found on this machine on the PATH
ECHO ========================================================
PAUSE
EXIT /B
)

:SearchJava86
ECHO =================================
ECHO Java not found in PATH
ECHO Searching for Java in C: drive]
ECHO =================================
FOR /D %%G IN (C:/"Program Files (x86)"/Java/*) DO (
for /F "tokens=1,2 delims=: " %%a in ("%%G") do (
C:/"Program Files (x86)"/Java/"%%b"/bin/java.exe -version
if ERRORLEVEL 1 goto :SearchJava
if ERRORLEVEL 0 (
ECHO =============================================
ECHO Java was successfully found on this machine. 
ECHO =============================================
PAUSE
EXIT /B
)
)
)

:SearchJava
FOR /D %%G IN (C:/"Program Files (x86)"/Java/*) DO (
for /F "tokens=1,2 delims=: " %%a in ("%%G") do (
C:/"Program Files (x86)"/Java/"%%b"/bin/java.exe -version
if ERRORLEVEL 1 goto :JavaNotFound
if ERRORLEVEL 0 (
ECHO ============================================
ECHO Java was successfully found on this machine. 
ECHO ============================================
PAUSE
EXIT /B
)
)
)

:JavaNotFound
ECHO ============================================================================================================
echo java.exe was not found on your path!
echo The game requires:
echo 1) an installation of the Java Development Kit or an installation of the Java Runtime Environment, and
echo 2) either java.exe is on the path, or that the one of JAVA_HOME, JDK_HOME or JRE_HOME environment variables 
echo is set and points to the installation directory. If not, it will look under
echo C:\Program Files\Java\ and C:\Program Files (x86)\Java\ for any JDK or JRE of version 1.6 or greater.
PAUSE
ECHO ============================================================================================================