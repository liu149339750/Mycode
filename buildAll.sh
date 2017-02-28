#!/bin/bash

if [ ! -d out ];then
mkdir out
fi
mode=debug
if [ ! -z $1 ];then
mode=$1
fi
ant -f githubproject/JazzyViewPager/lib/build.xml $mode -Dexport-dl=false
ant -f githubproject/NiftyNotification-master/app/src/main/build.xml $mode -Dexport-dl=false
