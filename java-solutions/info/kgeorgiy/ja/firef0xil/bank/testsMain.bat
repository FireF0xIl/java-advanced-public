@echo off

SET LIB=%cd%\..\..\..\..\..\..\lib

javac --module-path %LIB% --add-modules junit -d OUT *.java
java --module-path %LIB% --add-modules junit -cp OUT info.kgeorgiy.ja.firef0xil.bank.BankTests

echo Status code: %errorlevel%

RMDIR OUT /S /Q