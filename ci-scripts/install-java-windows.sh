#!/bin/bash

GIT_BASH_BIN=$(dirname `which bash.exe`)
INSTALL_DIR=$HOME/tools

# Make sure Git Binary path is first
echo  'export PATH='"$GIT_BASH_BIN:$INSTALL_DIR/usr/bin:$INSTALL_DIR/wix"':$PATH' >> $HOME/.bashrc
. $HOME/.bashrc

# Make sure unzip is present
if [ -z "$(which unzip)" ]; then
    echo Handling unzip
    if which 7z.exe; then
        ln -s $(which 7z.exe) "$GIT_BASH_BIN"/unzip.exe
    else
        (
            mkdir -p $INSTALL_DIR
            cd $INSTALL_DIR
            curl -Lo- https://mirror.msys2.org/msys/x86_64/unzip-6.0-2-x86_64.pkg.tar.xz | tar xJf -
        )
    fi
fi

if [ -z "$(which zip)" ]; then
    echo Handling zip
    if which 7z.exe; then
        ln -s $(which 7z.exe) "$GIT_BASH_BIN"/zip.exe
    else
        T=$(mktemp -d)
        (
            mkdir -p $INSTALL_DIR
            cd $INSTALL_DIR;
            curl -Lo- https://mirror.msys2.org/msys/x86_64/zip-3.0-3-x86_64.pkg.tar.xz | tar xJf -
        )
    fi
fi

if which light && which candle; then
    echo -- wix already present
else
    echo -- install wix
    mkdir -p $INSTALL_DIR/wix
    cd $INSTALL_DIR/
    curl -LO https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip 
    cd wix/
    unzip ../wix311-binaries.zip
    which light.exe
    which candle.exe
fi

echo --- Install sdkman
export SDKMAN_DIR="$HOME/sdkman" && curl -s "https://get.sdkman.io" | bash
echo --- cat $HOME/.bashrc
cat $HOME/.bashrc
echo ---
. $HOME/.bashrc
sdk version
sdk install java 17.0.8.1-tem
sdk install maven
