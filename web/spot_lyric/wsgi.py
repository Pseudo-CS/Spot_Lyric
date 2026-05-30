"""
WSGI config for spot_lyric project.
"""

import os

from django.core.wsgi import get_wsgi_application

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "spot_lyric.settings")

application = get_wsgi_application()
