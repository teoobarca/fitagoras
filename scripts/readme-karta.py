# A README picture: one app screen in a phone frame on a grid-paper card, the phone cut by the card's bottom edge
# (docs/VYVOJ.md, "Zábery v README"). Needs Pillow: uv run --no-project --with pillow python scripts/readme-karta.py
#   <capture.png> <light|dark> <visible screen height, px of the 1281-wide capture> <out.png> [<blank from>-<to>]
# The optional rows are painted with the screen's background: in the clip step, the faded line of an earlier caption
# under the player (rows 1412-1500), which the list's fade cuts through its letters.
import sys
from PIL import Image, ImageDraw
src, theme, vis, out = sys.argv[1], sys.argv[2], int(sys.argv[3]), sys.argv[4]
CUT = 92                                # rows kept above the app: room for the camera
S = 2                                   # the PNG is 2x its CSS size
CARD_W = 410 * S                        # two cards side by side in GitHub's 838 px README column
SCREEN_W = 360 * S                      # the app's 427 dp at 0.84: 14 sp body text ≈ 12 px
BEZEL, R_OUT, R_IN = 11 * S, 40 * S, 30 * S
TOP = 22 * S                            # card edge to the phone's top
STATUS = 156                            # status bar rows of the 1281 capture (52 dp), blanked: Robolectric has none
light = theme == "light"
im = Image.open(src).convert("RGB")
im = im.resize((1281, round(im.height * 1281 / im.width)), Image.LANCZOS)
bg = im.getpixel((8, STATUS + 10))
ImageDraw.Draw(im).rectangle((0, 0, im.width, STATUS), fill=bg)
if len(sys.argv) > 5:
    a, b = map(int, sys.argv[5].split('-'))
    ImageDraw.Draw(im).rectangle((0, a, im.width, b), fill=bg)
im = im.crop((0, CUT, 1281, vis)).resize((SCREEN_W, round(vis * SCREEN_W / 1281)), Image.LANCZOS)
phone_w = SCREEN_W + 2 * BEZEL
card_h = TOP + BEZEL + im.height
card_bg = (243, 239, 230) if light else (24, 27, 25)
grid = (226, 221, 210) if light else (38, 42, 40)
card = Image.new("RGB", (CARD_W, card_h), card_bg)
d = ImageDraw.Draw(card)
step = 22 * S
for x in range(0, CARD_W, step): d.line((x, 0, x, card_h), fill=grid, width=1)
for y in range(0, card_h, step): d.line((0, y, CARD_W, y), fill=grid, width=1)
x0 = (CARD_W - phone_w) // 2
frame = (22, 22, 22) if light else (50, 52, 51)
d.rounded_rectangle((x0, TOP, x0 + phone_w, card_h + R_OUT * 2), R_OUT, fill=frame)
mask = Image.new("L", im.size, 0)
ImageDraw.Draw(mask).rounded_rectangle((0, 0, im.width - 1, im.height + R_IN * 2), R_IN, fill=255)
card.paste(im, (x0 + BEZEL, TOP + BEZEL), mask)
cx, cy, cr = CARD_W // 2, TOP + BEZEL + 13 * S, 5 * S
d.ellipse((cx - cr, cy - cr, cx + cr, cy + cr), fill=(12, 12, 12) if light else (0, 0, 0))
# rounded card on a transparent background
outimg = Image.new("RGBA", card.size, (0, 0, 0, 0))
m = Image.new("L", card.size, 0)
ImageDraw.Draw(m).rounded_rectangle((0, 0, CARD_W - 1, card_h - 1), 16 * S, fill=255)
outimg.paste(card, (0, 0), m)
outimg.save(out, optimize=True)
print(out, outimg.size)
