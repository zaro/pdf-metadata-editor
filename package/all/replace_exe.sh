#!/bin/sh

SRC_FILE=WinRun4J.exe

for file in PdfMetadataEditor.exe PdfMetadataEditor64.exe pmedit-cli.exe pmedit-cli64.exe; do

    cp -v $SRC_FILE $file

done