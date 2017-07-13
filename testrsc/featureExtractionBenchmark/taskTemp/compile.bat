@echo off
cd /d %~dp0
javac -cp ".;libs/weka.jar;libs/Catalano.Image.jar;libs/JAICore.jar;libs/commons-io-2.5.jar" GenderPredictor.java
