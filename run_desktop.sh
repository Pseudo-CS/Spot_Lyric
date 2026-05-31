#!/bin/bash

# Determine project root directory
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WEB_DIR="$PROJECT_DIR/web"
VENV_DIR="$WEB_DIR/.venv"

echo "=========================================================="
echo "         SpotLyric Desktop Application Launcher           "
echo "=========================================================="

# Check for system-level Tkinter dependency
python3 -c "import tkinter" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Warning: Python 'tkinter' module is missing on your system."
    echo "Tkinter is required to run the desktop GUI."
    echo "----------------------------------------------------------"
    echo "How to install:"
    echo "  - Debian/Ubuntu: sudo apt-get update && sudo apt-get install python3-tk"
    echo "  - Fedora/CentOS/RHEL: sudo dnf install python3-tkinter"
    echo "  - Arch Linux: sudo pacman -S tk"
    echo "  - macOS: brew install python-tk"
    echo "----------------------------------------------------------"
    read -p "Press Enter to continue anyway, or Ctrl+C to abort..."
fi

# Ensure virtual environment is ready
if [ ! -d "$VENV_DIR" ] || [ ! -f "$VENV_DIR/bin/python" ]; then
    echo "Virtual environment not found. Setting it up at $VENV_DIR..."
    python3 -m venv "$VENV_DIR"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to create virtual environment."
        exit 1
    fi
    echo "Installing required packages..."
    "$VENV_DIR/bin/pip" install -r "$WEB_DIR/requirements.txt"
else
    # Quick check if requirements are satisfied
    echo "Verifying Python dependencies..."
    # Suppress output unless error
    "$VENV_DIR/bin/pip" install -r "$WEB_DIR/requirements.txt" --quiet
fi

if [ $? -ne 0 ]; then
    echo "Error: Failed to install Python dependencies. Please check requirements.txt."
    exit 1
fi

echo "Launching SpotLyric Desktop GUI..."
"$VENV_DIR/bin/python" "$WEB_DIR/main.py"
