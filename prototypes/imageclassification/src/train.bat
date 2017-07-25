@echo off
cd /d %~dp0
java -cp ".;libs/weka.jar;libs/util.jar;libs/Catalano.Image.jar" ImageClassifier -t contTrainingInstances.serialized
