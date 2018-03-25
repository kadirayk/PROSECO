@echo off
title grounding routine
cd /d %~dp0
java -Dserver.port=0 -jar src/autoML.jar