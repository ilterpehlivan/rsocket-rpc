#!/usr/bin/env bash

echo "generating the Java executable script for linux to: $1"

mkdir -p $1/generated
#curl https://raw.githubusercontent.com/fabric8io-images/run-java-sh/master/fish-pepper/run-java-sh/fp-files/run-java.sh > run-java.sh
cat rsocket-rpc-gen $1/build/libs/rsocket-rpc-gen-*-jdk8.jar > $1/generated/rsocket-rpc-gen
chmod +x $1/generated/rsocket-rpc-gen

#Now let's create the platform files
cp $1/generated/rsocket-rpc-gen $1/generated/rsocket-rpc-gen-$2-osx-x86_64.exe
cp $1/generated/rsocket-rpc-gen $1/generated/rsocket-rpc-gen-$2-linux-x86_64.exe