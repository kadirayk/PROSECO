echo Strategy >> $1/strategy4.out
echo file:$0 >> $1/strategy4.out
echo param1:$1 >> $1/strategy4.out
echo param2:$2 >> $1/strategy4.out
echo param3:$3 >> $1/strategy4.out
echo param4:$4 >> $1/strategy4.out
cd $0
javac --release 8 NoOutPut.java
java NoOutPut $1 $2 $3 $4