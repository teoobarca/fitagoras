#!/bin/bash
# LLM kontroly obsahu (kontroly/*.md): export textu všetkých lekcií LA1 a DML, voliteľne zábery krokov, potom pre každú
# kapitolu × skupinu kontrol jeden beh `codex exec` (Codex CLI prihlásené účtom ChatGPT, len čítanie) a jeden report.
# Návod: docs/SYSTEM.md, „LLM content checks“. Bez platby za tokeny: kľúče OPENAI_API_KEY a CODEX_API_KEY sa behu odoberú.
#
#   scripts/llm-kontrola.sh [--out DIR] [--kapitoly la1-k2,dml-tc] [--skupiny postup,znenie] [--zabery]
#                           [--paralelne 4] [--bez-exportu] [--len-report]
#
#   --out DIR       kam (predvolene ~/.fitagoras/llm-kontrola-<dátum>); export, behy, report.md
#   --kapitoly      čiarkou: la1-k2 la1-k3 la1-k4 dml-mi dml-mz dml-tc (predvolene všetky)
#   --skupiny       čiarkou: postup znenie vysvetlenia matematika sadzba (predvolene všetky; sadzba len so --zabery)
#   --zabery        zábery každého kroku lekcie (:app:snap, 320 dp, svetlá) a skupina sadzba nad nimi (beh na lekciu)
#   --paralelne N   súbežných behov codexu (predvolene 4)
#   --bez-exportu   použi export, ktorý už v DIR je
#   --len-report    len zostav report z hotových behov v DIR
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SELF="$ROOT/scripts/llm-kontrola.sh"
MODEL="gpt-6.1-sol"
EFFORT="medium"
JOB_TIMEOUT="${LLMK_TIMEOUT:-2700}" # s na jeden beh codexu

# ---- jeden beh (volá ho xargs) -------------------------------------------------------------------------------------
if [ "${1:-}" = "--job" ]; then
  job="$2" # <kapitola>:<skupina>[:<lekcia>]
  IFS=: read -r kap grp les <<< "$job"
  name="$kap-$grp${les:+-$les}"
  dir="$LLMK_OUT/behy/$name"
  mkdir -p "$dir"
  # a result counts only for the export it was made from (a new export reruns everything)
  [ -s "$dir/vysledok.json" ] && [ "$(cat "$dir/export-id" 2>/dev/null)" = "$(cat "$LLMK_EXPORT/id")" ] && { echo "hotové: $name"; exit 0; }
  rm -f "$dir/vysledok.json"
  imgs=()
  if [ -n "$les" ]; then
    while IFS= read -r f; do imgs+=(--image "$f"); done < <(ls "$LLMK_EXPORT/$kap/zabery/" | grep "^lesson_${les}_[0-9]*-" | sort -t_ -k3 -n | sed "s|^|$LLMK_EXPORT/$kap/zabery/|")
    [ ${#imgs[@]} -eq 0 ] && { echo "bez záberov: $name"; exit 0; }
  fi
  start=$(date +%s)
  # the prompt as an argument (stdin is /dev/null); keys for paid APIs removed so codex uses the ChatGPT login
  if env -u OPENAI_API_KEY -u CODEX_API_KEY -u OPENAI_BASE_URL perl -e 'alarm shift; exec @ARGV' "$JOB_TIMEOUT" \
      codex exec -m "$MODEL" -c model_reasoning_effort="$EFFORT" --skip-git-repo-check -s read-only --ephemeral \
      -C "$LLMK_EXPORT" --output-schema "$LLMK_OUT/schema.json" -o "$dir/vysledok.json" ${imgs[@]+"${imgs[@]}"} \
      -- "$(cat "$dir/prompt.md")" < /dev/null > "$dir/log.txt" 2>&1; then
    echo "$(( $(date +%s) - start ))" > "$dir/sekundy"
    cp "$LLMK_EXPORT/id" "$dir/export-id"
    echo "ok: $name ($(( $(date +%s) - start )) s)"
  else
    rc=$?
    echo "$(( $(date +%s) - start ))" > "$dir/sekundy"
    rm -f "$dir/vysledok.json"
    echo "ZLYHAL: $name (kód $rc, $dir/log.txt)"
  fi
  exit 0
fi

# ---- argumenty -----------------------------------------------------------------------------------------------------
OUT="$HOME/.fitagoras/llm-kontrola-$(date +%F)"
KAPITOLY=""; SKUPINY=""; ZABERY=0; PAR=4; EXPORT=1; LEN_REPORT=0
while [ $# -gt 0 ]; do
  case "$1" in
    --out) OUT="$2"; shift 2 ;;
    --kapitoly) KAPITOLY="${2//,/ }"; shift 2 ;;
    --skupiny) SKUPINY="${2//,/ }"; shift 2 ;;
    --zabery) ZABERY=1; shift ;;
    --paralelne) PAR="$2"; shift 2 ;;
    --bez-exportu) EXPORT=0; shift ;;
    --len-report) LEN_REPORT=1; EXPORT=0; shift ;;
    -h|--help) sed -n '2,17p' "$0"; exit 0 ;;
    *) echo "neznámy argument: $1" >&2; exit 2 ;;
  esac
done
mkdir -p "$OUT"
OUT="$(cd "$OUT" && pwd)"
EXP="$OUT/export"
export LLMK_OUT="$OUT" LLMK_EXPORT="$EXP"
[ $LEN_REPORT = 1 ] || command -v codex >/dev/null || { echo "chýba codex CLI" >&2; exit 1; }

LOCK="${FITAGORAS_LOCK:-}"  # optional Gradle wrapper, as in scripts/web.sh
gradle() { # one Gradle at a time on this machine (docs/SYSTEM.md)
  if [ -n "$LOCK" ] && [ -x "$LOCK" ]; then "$LOCK" gradle ./gradlew "$@"; else ./gradlew "$@"; fi
}
if [ -z "${JAVA_HOME:-}" ] && ! /usr/libexec/java_home >/dev/null 2>&1; then
  for j in /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home /opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home; do
    [ -x "$j/bin/java" ] && { export JAVA_HOME="$j"; break; }
  done
fi

T0=$(date +%s)
cd "$ROOT"

# ---- 1. export ------------------------------------------------------------------------------------------------------
if [ $EXPORT = 1 ]; then
  echo "== export obsahu do $EXP"
  rm -rf "$EXP"; mkdir -p "$EXP"
  gradle :shared:exportContent -Pexport.out="$EXP" -q
  # the context next to the export, so a run reads everything from one folder
  mkdir -p "$EXP/docs"; cp docs/ZASADY.md "$EXP/docs/"; cp -R kurz "$EXP/kurz"; cp -R kontroly "$EXP/kontroly"
  git -C "$ROOT" rev-parse --short HEAD > "$EXP/commit"
  sed -n 's/.*versionName = "\(.*\)".*/\1/p' app/build.gradle.kts | head -1 > "$EXP/verzia"
  echo "$(date +%s)-$(cat "$EXP/commit")" > "$EXP/id"
fi
if [ $LEN_REPORT = 1 ]; then
  python3 -I "$ROOT/scripts/llm-kontrola-report.py" "$OUT"
  echo "== report: $OUT/report.md"; exit 0
fi
[ -f "$EXP/index.md" ] || { echo "export chýba: $EXP (pusti bez --bez-exportu)" >&2; exit 1; }
[ -s "$EXP/id" ] || echo "$(date +%s)-$(cat "$EXP/commit" 2>/dev/null)" > "$EXP/id" # an export from before the id
ALL_KAP="$(ls -d "$EXP"/*/ | xargs -n1 basename | grep -E '^(la1|dml)-')"
[ -n "$KAPITOLY" ] || KAPITOLY="$ALL_KAP"

# ---- 2. zábery ------------------------------------------------------------------------------------------------------
if [ $ZABERY = 1 ] && [ $LEN_REPORT = 0 ]; then
  for k in $KAPITOLY; do
    # complete only when every lesson step has its picture (an interrupted run is redone)
    [ "$(ls "$EXP/$k/zabery" 2>/dev/null | grep -c '^lesson_.*\.png$')" -ge "$(grep -c '^lesson:' "$EXP/$k/obrazovky.tsv")" ] && continue
    echo "== zábery $k"
    routes="$(grep '^lesson:' "$EXP/$k/obrazovky.tsv" | cut -f1 | paste -sd, -)"
    course="${k%%-*}"
    gradle :app:snap -Psnap.route="$routes" -Psnap.course="$course" -Psnap.width=320 -Psnap.theme=light \
      -Psnap.height=1600 -Psnap.out="$EXP/$k/zabery" -q > "$OUT/zabery-$k.log" 2>&1 || { echo "zábery $k zlyhali: $OUT/zabery-$k.log" >&2; exit 1; }
  done
fi

# ---- 3. prompty -----------------------------------------------------------------------------------------------------
hdr() { sed -n "s/^$2: *//p" "$1" | head -1; }
ALL_GRP="$(for f in kontroly/*.md; do [ "$(basename "$f")" = README.md ] || hdr "$f" Skupina; done | sort -u)"
[ -n "$SKUPINY" ] || SKUPINY="$ALL_GRP"

cat > "$OUT/schema.json" <<'EOF'
{"type":"object","additionalProperties":false,"required":["prezrete","nalezy"],"properties":{
 "prezrete":{"type":"string","description":"čo si prešiel a prepočítal, jednou-dvomi vetami"},
 "nalezy":{"type":"array","items":{"type":"object","additionalProperties":false,
  "required":["kontrola","miesto","subor","citat","preco","zavaznost","istota"],
  "properties":{"kontrola":{"type":"string"},"miesto":{"type":"string"},"subor":{"type":"string"},"citat":{"type":"string"},
   "preco":{"type":"string"},"zavaznost":{"type":"string","enum":["P1","P2","P3"]},"istota":{"type":"string","enum":["vysoka","stredna","nizka"]}}}}}}
EOF

bundle_of() { sed -n "s/^Balík kapitoly: \`\([^\`]*\)\`.*/\1/p" "$EXP/$1/lekcie.md" | head -1; }

prompt() { # kapitola skupina [lekcia] > prompt
  local k="$1" g="$2" l="${3:-}" b course inputs specs f
  b="$(bundle_of "$k")"; course="${k%%-*}"
  specs=""; inputs=""
  for f in kontroly/*.md; do
    [ "$(basename "$f")" = README.md ] && continue
    [ "$(hdr "$f" Skupina)" = "$g" ] || continue
    specs="$specs $f"; inputs="$inputs, $(hdr "$f" Vstupy)"
  done
  echo "Si kontrolór obsahu výučbovej appky FITagoras (BI-LA1 a BI-DML na FIT ČVUT). Prejdi kapitolu \`$k\` podľa kontrol nižšie a vráť nálezy."
  echo
  echo "Pravidlá behu: len čítaj súbory v tomto priečinku (a ak treba overiť riadok, repo v $ROOT). Nič nemeň, nič nevytváraj, nič neinštaluj, nespúšťaj build ani testy, nepoužívaj sieť. Ak je toto otázka, odpovedz na ňu, nič nemeň."
  echo "Nehľadaj chyby mimo týchto kontrol. Každý nález musí mať doslovný citát z exportu (pri záberoch opis záberu). Radšej menej istých nálezov ako veľa domnienok; neistý nález označ \`istota: nizka\`."
  echo
  if [ -n "$l" ]; then
    echo "Vstup: zábery krokov lekcie \`$l\` (priložené obrázky v poradí krokov, meno súboru = route) a jej text v \`$k/lekcie.md\` (oddiel „Lekcia … · \`lesson:$l\`“)."
  else
    echo "Vstupy (čítaj celé, po častiach): $(echo "$inputs" | tr ',' '\n' | sed 's/^ *//' | grep -v '^$' | grep -v zabery | sort -u | sed "s|^|\`$k/|; s|$|.md\`|" | paste -sd, - | sed 's/,/, /g')."
  fi
  echo "Kontext kurzu: zásady \`docs/ZASADY.md\`, predmet \`kurz/$course/README.md\` (značenie, čo kto chce a zakazuje), balík kapitoly \`$b\` (celý je nižšie)."
  echo
  echo "Pole \`kontrola\` = meno súboru kontroly bez .md: $(for f in $specs; do basename "$f" .md; done | paste -sd, - | sed 's/,/, /g')."
  echo
  echo "=================== FORMÁT A ZÁVAŽNOSŤ (kontroly/README.md) ==================="
  sed -n '/^## Formát nálezu/,$p' kontroly/README.md
  for f in $specs; do
    echo; echo "=================== KONTROLA $(basename "$f" .md) ($f) ==================="
    cat "$f"
  done
  echo; echo "=================== BALÍK KAPITOLY $b ==================="
  cat "$EXP/$b"
}

JOBS=()
for k in $KAPITOLY; do
  for g in $SKUPINY; do
    if [ "$g" = sadzba ]; then
      [ -d "$EXP/$k/zabery" ] || continue
      for l in $(grep '^lesson:' "$EXP/$k/obrazovky.tsv" | cut -f2 | uniq); do JOBS+=("$k:$g:$l"); done
    else
      JOBS+=("$k:$g")
    fi
  done
done
[ ${#JOBS[@]} -gt 0 ] || { echo "žiadne behy (sadzba potrebuje --zabery)" >&2; exit 2; }
if [ $LEN_REPORT = 0 ]; then
  for j in "${JOBS[@]}"; do
    IFS=: read -r k g l <<< "$j"
    d="$OUT/behy/$k-$g${l:+-$l}"; mkdir -p "$d"
    prompt "$k" "$g" "$l" > "$d/prompt.md"
  done

  # ---- 4. behy ----------------------------------------------------------------------------------------------------
  echo "== ${#JOBS[@]} behov codexu ($MODEL, $EFFORT), $PAR súbežne"
  printf '%s\n' "${JOBS[@]}" | xargs -P "$PAR" -I{} "$SELF" --job {}
fi

# ---- 5. report ------------------------------------------------------------------------------------------------------
[ $LEN_REPORT = 1 ] || echo "$(( $(date +%s) - T0 ))" >> "$OUT/trvanie"
python3 -I "$ROOT/scripts/llm-kontrola-report.py" "$OUT"
echo "== report: $OUT/report.md"
# a failed or timed-out Codex run makes the whole run fail (the report lists it under „Zlyhané behy“)
failed=0
for j in "${JOBS[@]}"; do IFS=: read -r k g l <<< "$j"; [ -s "$OUT/behy/$k-$g${l:+-$l}/vysledok.json" ] || failed=$((failed + 1)); done
[ $failed = 0 ] || { echo "zlyhaných behov: $failed" >&2; exit 1; }
