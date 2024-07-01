@echo off

SET LIB=%cd%\..\..\..\..\..\..\lib

javac --module-path %LIB% --add-modules junit  -d OUT *.java
