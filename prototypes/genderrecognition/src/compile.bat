@echo off
cd /d %~dp0
javac -cp ".;libs/weka.jar;libs/util.jar;libs/Catalano.Image.jar" GenderPredictor.java
