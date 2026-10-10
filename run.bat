@echo off
setlocal

if not defined JAVAFX_HOME set "JAVAFX_HOME=C:\javafx-sdk-19.0.2"

if not exist "%JAVAFX_HOME%\lib\javafx.controls.jar" (
    echo JavaFX introuvable dans "%JAVAFX_HOME%".
    echo Definissez JAVAFX_HOME vers votre dossier JavaFX SDK 19.0.2.
    exit /b 1
)

if exist out rmdir /s /q out
mkdir out

javac --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.fxml -encoding UTF-8 -d out src\application\Main.java src\modele\Drone.java src\modele\Environnement.java src\modele\Intrus.java
if errorlevel 1 exit /b 1

java --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.fxml -cp out application.Main
