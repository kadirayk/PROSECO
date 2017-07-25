@echo off
cd /d %~dp0
title HTN Strategy
echo Starting execution of HTN Strategy...
java -jar strategy.jar ../../params/ output/ ../../benchmarks/benchmarkTaskOffer.bat