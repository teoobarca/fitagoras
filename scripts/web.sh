#!/bin/bash
# Webová appka FITagoras: produkčné zostavenie a priečinok na nahratie (docs/KMP.md, „The web“).
#
#   scripts/web.sh            zostaví build/web/ (Gradle cez $FITAGORAS_LOCK, ak je nastavený)
#   scripts/web.sh --bez-gradle   len pripraví build/web/ z posledného zostavenia
#
# Čo spraví: :shared:wasmJsBrowserDistribution (produkčné, Binaryen), skopíruje výsledok bez source mapy, zúži hlavné
# písmo na osi, ktoré appka používa (wdth 100, ROND 100: 3,9 MB → 1 MB, vzhľad rovnaký), dopíše verziu do index.html,
# napíše sw.js (offline: súbory tejto verzie v cache) a ku každému väčšiemu súboru pribalí .br a .gz (server ich
# posiela podľa Accept-Encoding). Na konci vypíše veľkosti a príkaz na nahratie.
# Potrebuje: JDK 21, brotli a gzip; uvx (fonttools) na zúženie písma, inak ostane celé.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
OUT="$ROOT/build/web"
DIST="$ROOT/shared/build/dist/wasmJs/productionExecutable"
LOCK="${FITAGORAS_LOCK:-}"  # optional: a wrapper that serializes Gradle runs on a shared machine, called as "$LOCK gradle <command>"

if [ "${1:-}" != "--bez-gradle" ]; then
  export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}"
  if [ -n "$LOCK" ] && [ -x "$LOCK" ]; then "$LOCK" gradle ./gradlew :shared:wasmJsBrowserDistribution; else ./gradlew :shared:wasmJsBrowserDistribution; fi
fi
[ -f "$DIST/index.html" ] || { echo "Chýba $DIST: zostavenie neprebehlo." >&2; exit 1; }

mkdir -p "$OUT"
# the folder mirrors this build exactly (a file of an older build would otherwise stay in the service worker's list)
# (packed copies of an older build go too: they are made again below)
rsync -a --delete --delete-excluded --exclude '*.map' --exclude 'zvuky/POVOD.md' "$DIST/" "$OUT/"

# the main font pinned to the axes the app sets (ui/Theme.kt, math/Engine.kt: wght, opsz, slnt vary; wdth and ROND don't)
FONT="$OUT/composeResources/dev.hamper.tabula.res/font/google_sans_flex.ttf"
if command -v uvx >/dev/null; then
  uvx --quiet --from 'fonttools==4.60.1' fonttools varLib.instancer "$FONT" wdth=100 ROND=100 -o "$FONT.tmp" 2>/dev/null && mv "$FONT.tmp" "$FONT" \
    || echo "Písmo ostalo celé (instancer zlyhal)." >&2
  # two characters the app puts after a formula (math/Compose.kt: "n-tica" keeps its hyphen, a comma its formula) that
  # the font lacks: a phone draws them from a system font, the browser has none and showed an empty box (and asked
  # Google's font server, which the page's CSP refuses). The non-breaking hyphen gets the hyphen's glyph, the word joiner
  # the zero-width space's; line breaking goes by the characters, so both still hold their line together.
  uvx --quiet --from 'fonttools==4.60.1' python - "$FONT" <<'PY' || echo "Písmu chýba U+2011/U+2060 (fonttools zlyhal)." >&2
import sys
from fontTools.ttLib import TTFont
f = TTFont(sys.argv[1])
for t in f["cmap"].tables:
    if t.isUnicode():
        t.cmap.setdefault(0x2011, t.cmap[0x2D])
        t.cmap.setdefault(0x2060, t.cmap[0x200B])
f.save(sys.argv[1])
PY
else
  echo "Písmo ostalo celé (chýba uvx)." >&2
fi

VERSION="$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' app/build.gradle.kts | head -1)"
BUILD="$VERSION-web.$(git rev-parse --short HEAD)$(git diff --quiet HEAD -- shared app/src/main/assets || echo '+')"
# the version the app shows in Settings (web/Main.kt reads it)
perl -0pi -e "s|<title>|<meta name=\"fitagoras-version\" content=\"$BUILD\">\n  <title>|" "$OUT/index.html"
# the web's own clip key (secrets.properties, never in git; the server holds only its hash): web/Main.kt reads it, the
# video sends it in the clip's address. Public by design, like the APK's; without it the web reads clips as captions.
CLIP_KEY=""
[ -f secrets.properties ] && CLIP_KEY="$(sed -n 's/^clipsWebToken=//p' secrets.properties | tail -1 | tr -d '[:space:]')"
if [ -n "$CLIP_KEY" ]; then
  case "$CLIP_KEY" in *[!A-Za-z0-9_-]*) echo "clipsWebToken v secrets.properties má nepovolené znaky." >&2; exit 1;; esac
  CLIP_KEY="$CLIP_KEY" perl -0pi -e 's|<title>|<meta name="fitagoras-clips" content="$ENV{CLIP_KEY}">\n  <title>|' "$OUT/index.html"
else
  echo "Bez clipsWebToken v secrets.properties: klipy sa na webe budú len čítať (titulky)." >&2
fi
# what the app downloads before its first screen (the wasm files, the fonts, the content files web/Main.kt preloads), as
# the browser hands it over unpacked: the splash's bar counts towards it (resources/web.js)
BYTES="$(cd "$OUT" && { ls ./*.wasm; find composeResources -path '*/font/*.ttf'; find composeResources -path '*/files/*' \( -name '*.json' -o -name '*.tsv' \) ! -path '*/coverage/*' ! -path '*/kontext/*'; } | xargs stat -f%z | awk '{s+=$1} END {print s}')"
perl -0pi -e "s|<title>|<meta name=\"fitagoras-bytes\" content=\"$BYTES\">\n  <title>|" "$OUT/index.html"

# the service worker. Online every file is asked for fresh (the server answers 304 when unchanged), so the page, the
# code and the content always come from one upload; the cache is only for offline. The two wasm files carry their
# hash in the name and come from the cache at once. The cache's name is a hash of this build's files, so any change
# in them, also in content the commit doesn't show, installs a new worker.
CONTENT="$(cd "$OUT" && find . -type f ! -name sw.js ! -name '*.br' ! -name '*.gz' | LC_ALL=C sort | xargs shasum -a 256 | shasum -a 256 | cut -c1-12)"
FILES="$(cd "$OUT" && find . -type f ! -name sw.js ! -name '.*' ! -name '*.br' ! -name '*.gz' | sed 's|^\./||' | sort | python3 -c 'import json,sys; print(json.dumps([l.strip() for l in sys.stdin if l.strip()]))')"
cat > "$OUT/sw.js" <<EOF
// FITagoras $BUILD ($CONTENT): written by scripts/web.sh. Online every file is fetched fresh (304 when unchanged); offline
// they come from this build's cache, filled whole at install; the hashed wasm files from any cache that has them. A new
// upload changes this file, so a new worker installs and replaces the cache only once it holds the whole new build.
const CACHE = "fitagoras-$CONTENT";
const FILES = $FILES;
self.addEventListener("install", (e) => e.waitUntil(caches.open(CACHE).then((c) => c.addAll(["./", ...FILES])).then(() => self.skipWaiting())));
self.addEventListener("activate", (e) => e.waitUntil(caches.keys()
  .then((keys) => Promise.all(keys.filter((k) => k.startsWith("fitagoras-") && k !== CACHE).map((k) => caches.delete(k))))
  .then(() => self.clients.claim())));
self.addEventListener("fetch", (e) => {
  const url = new URL(e.request.url);
  if (e.request.method !== "GET" || url.origin !== location.origin || !url.pathname.startsWith(new URL("./", self.registration.scope).pathname)) return;
  const key = e.request.mode === "navigate" ? "./" : e.request;
  if (/[0-9a-f]{16,}[.]wasm\$/.test(url.pathname)) {
    e.respondWith(caches.match(key).then((hit) => hit || fetch(e.request)));
    return;
  }
  // nothing fetched here goes into the cache: offline is always the last build the worker cached whole at install
  e.respondWith(fetch(e.request).catch(() => caches.match(key)));
});
EOF

# packed copies for the server to send as they are
find "$OUT" -type f \( -name '*.wasm' -o -name '*.js' -o -name '*.json' -o -name '*.ttf' -o -name '*.tsv' -o -name '*.md' -o -name '*.txt' -o -name '*.xml' -o -name '*.html' -o -name '*.wav' -o -name '*.webmanifest' \) -size +1k | while read -r f; do
  brotli -f -q 11 -o "$f.br" "$f"
  gzip -9 -n -c "$f" > "$f.gz"
done

raw=$(find "$OUT" -type f ! -name '*.br' ! -name '*.gz' -exec stat -f%z {} + | awk '{s+=$1} END {print s}')
first=0
for f in "$OUT"/*.wasm "$OUT/fitest.js" "$OUT/index.html" "$OUT/web.js" "$OUT"/composeResources/dev.hamper.tabula.res/font/*.ttf \
         $(find "$OUT/composeResources/dev.hamper.tabula.res/files" -maxdepth 2 \( -name '*.json' -o -name '*.tsv' \) ! -path '*/coverage/*'); do
  [ -f "$f.br" ] && first=$((first + $(stat -f%z "$f.br"))) || first=$((first + $(stat -f%z "$f")))
done
echo
echo "Hotovo: $OUT ($BUILD)"
echo "  spolu na disku bez .br/.gz: $((raw / 1024)) kB; prvé načítanie (brotli): $((first / 1024)) kB"
echo "  nahratie: obsah build/web/ na server, ktorý podáva /app/ (najprv wasm a zdroje, potom index.html, fitest.js a sw.js)"
