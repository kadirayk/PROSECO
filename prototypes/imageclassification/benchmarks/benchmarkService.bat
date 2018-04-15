@echo off
cd /d %~dp0
call java -jar ./Benchmark.jar testbed/validationInstances.serialized ../../src/