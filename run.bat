@echo off
rem Compilar los archivos .java
echo Compilando el proyecto...
javac -d out src/main/*.java src/analizadores/*.java src/clasesAux/*.java src/tablaS/*.java

if %ERRORLEVEL% neq 0 (
    echo Error durante la compilación. Revisa el código.
    pause
    exit /b
)

rem Ejecutar la clase principal
echo Ejecutando el programa...
java -cp out main.Main

if %ERRORLEVEL% neq 0 (
    echo Error durante la ejecución del programa.
    pause
    exit /b
)

echo Proceso finalizado exitosamente.
pause