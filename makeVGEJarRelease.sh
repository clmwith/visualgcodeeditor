#! /bin/bash

echo building jar release

export SRC=~/NetBeansProjects/VisualGCodeEditor/ext
export VGE=~/NetBeansProjects/VisualGCodeEditor/dist/VisualGCodeEditor.jar

TMP=/tmp/vge$$.tmp
mkdir -p $TMP
cd $TMP

cp "$SRC"/*.dll /usr/lib/jni/librxtxSerial.so .
unzip -o "$SRC/*.jar"
rm -R "$TMP/META-INF"

DEST=$TMP/VisualGCodeEditor.jar
cp "$VGE" "$DEST"
zip -d "$DEST" META-INF

zip -r "$DEST" *

echo release is at : $TMP/VisualGCodeEditor.jar
echo run with cmd :
echo
echo java -jar $TMP/VisualGCodeEditor.jar

