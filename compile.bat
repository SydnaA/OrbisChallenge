@ECHO OFF

set JARPATH="%CD%\Java Library\JavaClient.jar";"%CD%\botplayer";"%CD%\Java Library\*"
set SRC_PATH="%CD%\botplayer\PlayerAI.java"
set SRC_FOLDER="%CD%\botplayer"

echo [Checking Path Variable]
javac.exe -version
if ERRORLEVEL 1 goto :SearchJava
ECHO [Java Found]
javac.exe -classpath %JARPATH% %SRC_PATH% -verbose 2>&1
if ERRORLEVEL 0  (
echo ==================================
echo COMPILATION WAS SUCCESSFUL
echo ==================================
EXIT /B
)


:SearchJava
ECHO [Java not found in Path, Searching for Java]
FOR /D %%G IN (C:/"Program Files (x86)"/Java/*) DO (
for /F "tokens=1,2 delims=: " %%a in ("%%G") do (
C:/"Program Files (x86)"/Java/"%%b"/bin/javac.exe -version
C:/"Program Files (x86)"/Java/"%%b"/bin/javac.exe -classpath %JARPATH% %SRC_PATH% -verbose 2>&1
if ERRORLEVEL 0 (

echo ==================================
echo COMPILATION WAS SUCCESSFUL
echo ==================================
EXIT /B
)
)
)

FOR /D %%G IN (C:/"Program Files"/Java/*) DO (
for /F "tokens=1,2 delims=: " %%a in ("%%G") do (
C:/"Program Files"/Java/"%%b"/bin/javac.exe -version
C:/"Program Files"/Java/"%%b"/bin/javac.exe -classpath %JARPATH% %SRC_PATH% -verbose 2>&1
if ERRORLEVEL 0  (
echo ==================================
echo COMPILATION WAS SUCCESSFUL
echo ==================================
EXIT /B
)

)
)

echo ==================================
echo COMPILATION FAILED
echo Check above to find the compilation errors
echo ==================================
runas /user:# "" >nul 2>&1