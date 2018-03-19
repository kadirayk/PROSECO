@echo off
title grounding routine
cd /d %~dp0
java -Dserver.port=8090 -jar src/autoML.jar