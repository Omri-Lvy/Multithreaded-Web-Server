#!/bin/bash

# Set the compiled classes directory
classes_dir="./target/classes"

# Set the main class name (replace with your actual main class)
main_class="WebServer"

# Run the Java application
java -cp "$classes_dir" "$main_class" "$@"
