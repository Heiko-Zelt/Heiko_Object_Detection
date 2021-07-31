# Heiko_Object_Detection
object detection batch job based on Deep Java Library

## Installations-Vorausetzungen
- Internet-Verbindung
- Ubuntu 20.04.1 oder Windows 10
- git 2.25.1 oder Git for Windows v2.31.1
- Apache Maven 3.6.3
- OpenJDK 14 oder 15
- Eclipse 2021-03 (4.19.0)

Andere Versionen funktionieren vermutlich auch. Ich habe aber nur mit diesen getestet.

## Installation

In der Bash oder Git-Bash:

    cd Installations-Verzeichnis
    git clone https://github.com/Heiko-Zelt/Heiko_Object_Detection

in Eclipse
1. Menüleiste > File > Open Projects from File System...
1. Verzeichnis "Heiko_Detection_Project" in "Heiko_Object_Detection" auswählen.
1. Warten bis Eclipse alle Dateien geparst hat. Im Project Explorer verschwinden rote (X)-Symbole.
1. Löschen aller Dateien im Verzeichnis "boxes" und "export".

## Starten

1. Im Project Explorer "src/main/java" > "de.heikozelt.objectdetection" > "BatchJob.java", Klick mit rechter Maustaste > "Run As" > "Java Application"
1. Auf der Console erscheint folgende Meldung: "SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder". Diese kann ignoriert werden.
1. Beim allerersten Starten läd die Deep Java Library ggf. Code aus dem Internet nach. Diese Downloads können je nach Engine umfangreich sein. Es dauert entsprechend lange. Bei schlechter Internet-Verbindung / abgebrochenem Download kann die Fehlermeldung "javax.net.ssl.SSLException: Tag mismatch!" ausgegeben werden. Einfach nochmal versuchen!

## Auswahl der Machine Learning Engine und des Modells

Zwecks Objekt-Erkennung wird die Deep Java Libraray verwendet. Diese bietet eine einheitliche Schnittstelle für viele Engines und Modelle.
Je nachdem welche Bibliotheken / .jar-Dateien sich im Classpath befinden, wird die Engine und das Modell ausgewählt.
Die Auswahl erfolgt z.B. mittels Maven und den Abhängigkeits-Deklarationen in der Datei pom.xml.
Zu jeder Engine gibt es eine Sammlung von trainierten Modellen, den sogenannten "Model-Zoo". Auch dieser Model-Zoo wird mittels pom.xml geladen.
Exemplarisch habe ich PyTorch mit resnet50/coco-dataset und Tensorflow mit mobilenet_v2/openimages_v4-dataset verwendet.

## Sonstiges

- Die Verarbeitung des ersten Bildes dauert länger, da erst die Engine und das Modell geladen werden. Alle weiteren Bilder werden schneller processed.
