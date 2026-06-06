#!/bin/bash
# Javadoc Generation Script with proper classpath

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
SRC_DIR="$PROJECT_DIR/src"
DOC_DIR="$PROJECT_DIR/doc"
LIB_DIR="$PROJECT_DIR/lib"

# Build classpath with all required JAR files
CLASSPATH=""
for jar in "$LIB_DIR"/*.jar "$LIB_DIR"/javafx-sdk-23.0.2/lib/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Remove leading colon
CLASSPATH="${CLASSPATH#:}"

# Run javadoc
javadoc \
    -d "$DOC_DIR" \
    -sourcepath "$SRC_DIR" \
    -classpath "$CLASSPATH" \
    -private \
    -author \
    -version \
    -use \
    -windowtitle "GoNature API Documentation" \
    -doctitle "GoNature Project API" \
    -header "GoNature v1.0" \
    client common server

echo "Javadoc generation complete. Documentation available in: $DOC_DIR"
