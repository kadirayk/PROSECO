@echo off
cd /d %~dp0
java -cp ".;libs/weka.jar;libs/util.jar;libs/Catalano.Image.jar;libs/Catalano.Math.jar;libs/Catalano.Statistics.jar" GenderPredictor -f validationInstances.serialized
