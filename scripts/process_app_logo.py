import os
from PIL import Image, ImageDraw

source_path = r"C:\Users\saras\.gemini\antigravity\brain\42b3f588-d892-4e2f-97c1-6527eb471ae7\grimoire_app_logo_1792923765549.jpg"
res_dir = r"c:\Users\saras\OneDrive\Desktop\project\dnd\android\app\src\main\res"

img = Image.open(source_path).convert("RGBA")

# Ensure app_logo.png in res/drawable
drawable_path = os.path.join(res_dir, "drawable", "app_logo.png")
logo_512 = img.resize((512, 512), Image.Resampling.LANCZOS)
logo_512.save(drawable_path, format="PNG")
print(f"Saved {drawable_path}")

# Mipmap densities and sizes
densities = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

def make_round_icon(image, size):
    # Create circular mask
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size, size), fill=255)
    
    resized = image.resize((size, size), Image.Resampling.LANCZOS)
    round_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    round_img.paste(resized, (0, 0), mask)
    return round_img

for folder_name, size in densities.items():
    folder_path = os.path.join(res_dir, folder_name)
    os.makedirs(folder_path, exist_ok=True)
    
    # Square launcher icon
    square_img = img.resize((size, size), Image.Resampling.LANCZOS)
    square_path = os.path.join(folder_path, "ic_launcher.png")
    square_img.save(square_path, format="PNG")
    
    # Round launcher icon
    round_img = make_round_icon(img, size)
    round_path = os.path.join(folder_path, "ic_launcher_round.png")
    round_img.save(round_path, format="PNG")
    print(f"Generated {folder_name} ({size}x{size})")

# Also create anydpi-v26 adaptive icon
anydpi_path = os.path.join(res_dir, "mipmap-anydpi-v26")
os.makedirs(anydpi_path, exist_ok=True)

adaptive_ic_launcher = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/black" />
    <foreground android:drawable="@drawable/app_logo" />
</adaptive-icon>
"""

with open(os.path.join(anydpi_path, "ic_launcher.xml"), "w", encoding="utf-8") as f:
    f.write(adaptive_ic_launcher)

with open(os.path.join(anydpi_path, "ic_launcher_round.xml"), "w", encoding="utf-8") as f:
    f.write(adaptive_ic_launcher)

print("Generated adaptive icon xmls")
