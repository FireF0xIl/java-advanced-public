@echo off
SET SOURCE=..\java-solutions
SET JAR_PACKAGE=info.kgeorgiy.ja.firef0xil.implementor
SET PACKAGE=info\kgeorgiy\ja\firef0xil\implementor
SET BASE_SOURCE=..\..\java-advanced-2022

XCOPY %SOURCE%\%PACKAGE% %JAR_PACKAGE%\%PACKAGE% /I /Y >NUL
COPY %SOURCE%\module-info.java %JAR_PACKAGE% >NUL

javac -m %JAR_PACKAGE% --module-source-path . -p %BASE_SOURCE%\lib;%BASE_SOURCE%\artifacts -d OUT

jar -c -f JarImplementor.jar -m Manifest.MF -C OUT\%JAR_PACKAGE% .

RD %JAR_PACKAGE% /S /Q
RD OUT /S /Q
