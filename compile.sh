#!/bin/bash

mvn compile

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful."
else
    echo "Compilation failed."
fi
