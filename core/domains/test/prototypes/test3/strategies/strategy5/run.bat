echo Strategy >> %1/strategy5.out
echo file:%~dp0%~nx0 >> %1/strategy5.out
echo param1:%1 >> %1/strategy5.out
echo param2:%2 >> %1/strategy5.out
echo param3:%3 >> %1/strategy5.out
echo param4:%4 >> %1/strategy5.out
cd %~dp0
javac BadScoreButNoErrors.java
java BadScoreButNoErrors %1 %2 %3 %4