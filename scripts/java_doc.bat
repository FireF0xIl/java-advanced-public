SET SOURCE=..\..\java-advanced-2022\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor

javadoc -cp ..\java-solutions -link https://docs.oracle.com/en/java/javase/17/docs/api/ ^
    -author -d ..\javadoc -private info.kgeorgiy.ja.firef0xil.implementor ^
    %SOURCE%\Impler.java %SOURCE%\JarImpler.java %SOURCE%\ImplerException.java
