import os
import sys

# Ensure the root of the application is in the python path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from gui.app import SpotLyricApp

def main():
    try:
        app = SpotLyricApp()
        app.protocol("WM_DELETE_WINDOW", app.on_closing)
        app.mainloop()
    except Exception as e:
        print(f"Startup Error: {e}")
        print("\nPlease make sure tkinter/customtkinter dependencies are installed.")
        print("Under Debian/Ubuntu, run: sudo apt-get install python3-tk")

if __name__ == "__main__":
    main()
