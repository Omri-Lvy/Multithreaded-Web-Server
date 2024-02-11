#!/bin/bash

# Set the source directory
SOURCE_DIR="src/main/java"

# Set the target directory for compiled classes
TARGET_DIR="target/classes"

# Compile all Java files, including those in subdirectories
javac -d "$TARGET_DIR" -cp "$TARGET_DIR" "$SOURCE_DIR"/config/*.java "$SOURCE_DIR"/server/*.java "$SOURCE_DIR"/*.java

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful."
else
    echo "Compilation failed."
fi
