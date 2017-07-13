@echo off
cd /d %~dp0
java -cp ".;libs/weka.jar;libs/Catalano.Image.jar;libs/JAICore.jar;libs/commons-io-2.5.jar;libs/Catalano.Math.jar;libs/Catalano.Statistics.jar" GenderPredictor -i %1 %2
