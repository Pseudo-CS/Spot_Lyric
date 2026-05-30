"""
ASGI config for spot_lyric project.
"""

import os

from django.core.asgi import get_asgi_application

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "spot_lyric.settings")

application = get_asgi_application()
