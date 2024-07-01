@echo off

SET LIB=%cd%\..\..\..\..\..\..\lib

javac --module-path %LIB% --add-modules junit -d OUT *.java
java --module-path %LIB% --add-modules junit -cp OUT org.junit.runner.JUnitCore info.kgeorgiy.ja.firef0xil.bank.Tests

echo Status code: %errorlevel%

RMDIR OUT /S /Q