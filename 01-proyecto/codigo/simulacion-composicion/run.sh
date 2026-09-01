#!/bin/bash
export JAVA_HOME=/tmp/jdk/jdk-21.0.12.1+1
export PATH=$JAVA_HOME/bin:$PATH

MP=""
for jar in $(find /home/svfabio/.m2/repository/org/openjfx -name "*-linux.jar" -not -name "*sources*" -not -name "*javadoc*" 2>/dev/null); do
    [ -n "$MP" ] && MP="$MP:"
    MP="$MP$jar"
done
for jar in $(find /home/svfabio/.m2/repository/org/jfree -name "*.jar" -not -name "*sources*" -not -name "*javadoc*" 2>/dev/null); do
    [ -n "$MP" ] && MP="$MP:"
    MP="$MP$jar"
done

java --module-path "$MP:target/classes" \
     --add-modules javafx.controls,javafx.swing \
     -m com.simulacion/com.simulacion.App
