@echo off
cd /d %~dp0
java -cp ".;libs/weka.jar;libs/util.jar;libs/JAICore.jar;libs/Catalano.Image.jar" ImageClassifier -t %1
