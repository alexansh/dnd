"""
Downloads free CC-BY 3.0 / CC0 fantasy game icons from game-icons.net
and converts them directly into Android Vector Drawable XML resources.
"""

import os
import re
import urllib.request

ICON_MAP = {
    'ic_d20': 'https://raw.githubusercontent.com/game-icons/icons/master/delapouite/dice-twenty-faces-twenty.svg',
    'ic_shield': 'https://raw.githubusercontent.com/game-icons/icons/master/sbed/shield.svg',
    'ic_sword': 'https://raw.githubusercontent.com/game-icons/icons/master/lorc/broadsword.svg',
    'ic_coins': 'https://raw.githubusercontent.com/game-icons/icons/master/delapouite/coins.svg',
    'ic_tombstone': 'https://raw.githubusercontent.com/game-icons/icons/master/sbed/tombstone.svg',
    'ic_potion': 'https://raw.githubusercontent.com/game-icons/icons/master/delapouite/health-potion.svg',
    'ic_key': 'https://raw.githubusercontent.com/game-icons/icons/master/lorc/key.svg',
    'ic_beer': 'https://raw.githubusercontent.com/game-icons/icons/master/delapouite/beer-stein.svg',
    'ic_dragon': 'https://raw.githubusercontent.com/game-icons/icons/master/lorc/dragon-head.svg',
    'ic_scroll': 'https://raw.githubusercontent.com/game-icons/icons/master/lorc/scroll-unfurled.svg',
    'ic_fire': 'https://raw.githubusercontent.com/game-icons/icons/master/lorc/fire-breath.svg',
    'ic_heart': 'https://raw.githubusercontent.com/game-icons/icons/master/lorc/heart-plus.svg'
}

DEST_DIR = os.path.join(
    os.path.dirname(__file__),
    '..', 'android', 'app', 'src', 'main', 'res', 'drawable'
)
DEST_DIR = os.path.abspath(DEST_DIR)
os.makedirs(DEST_DIR, exist_ok=True)

success_count = 0
for name, url in ICON_MAP.items():
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as resp:
            svg_text = resp.read().decode('utf-8')
            # Extract path data
            paths = re.findall(r'<path[^>]*d=["\']([^"\']+)["\']', svg_text)
            if paths:
                path_data = ' '.join(paths)
                vector_xml = f'''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="512"
    android:viewportHeight="512">
    <path
        android:fillColor="#FFD4AF37"
        android:pathData="{path_data}" />
</vector>
'''
                out_path = os.path.join(DEST_DIR, f'{name}.xml')
                with open(out_path, 'w', encoding='utf-8') as f:
                    f.write(vector_xml)
                print(f'[SUCCESS] Downloaded & Converted: {name}.xml')
                success_count += 1
            else:
                print(f'[WARN] No path data found for {name}')
    except Exception as e:
        print(f'[ERROR] Failed {name}: {e}')

print(f'\nFinished downloading {success_count}/{len(ICON_MAP)} fantasy vector assets.')
