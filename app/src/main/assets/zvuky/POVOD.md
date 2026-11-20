# Zvuky appky: pôvod

Súbory sú pomenované podľa roly; appka ich hrá podľa mena (`shared/.../ui/Sound.kt`, register `Sfx`). Zvuk sa
vymení zámenou súboru: originál s menom roly do priečinka a `python3 scripts/zvuky.py <priečinok>` (orezanie ticha,
mono 44,1 kHz WAV, hlasitosť podľa roly). Teo vybral 18. 11. 2026 z porovnania zvukov iných appiek (mimo repozitára).

**Sú z cudzích aplikácií. Pred zverejnením repozitára ich treba nahradiť vlastnými alebo voľne licencovanými**
(docs/VYVOJ.md, „Pred zverejnením repozitára“). Tento súbor sa do APK nebalí (`ignoreAssetsPattern`).

Vlastná náhrada na porovnanie: `scripts/zvuky_vlastne.py` (všetkých 15 rol syntézou z oscilátorov, parametre z meraní týchto zvukov, jedna tónina F dur; zapisuje do `build/zvuky-vlastne`), výmena:
`python3 scripts/zvuky_vlastne.py --out app/src/main/assets/zvuky`.

| rola | kedy hrá | zdrojová appka | pôvodný súbor |
|---|---|---|---|
| tap | ťuknutie na možnosť, vektor, bunku, kláves | Elevate | `assets/games/onboardio/assets/sounds/click.wav` |
| button | hlavné tlačidlo (Pokračovať, Skontrolovať, keď nevyhodnocuje) | Elevate | `assets/games/onboardio/assets/sounds/button_press.wav` |
| correct | správne na prvý pokus, prvá v rade | Elevate | `assets/games/onboardio/assets/sounds/correct_answer.wav` |
| combo_1 … combo_5 | 2. až 6. a ďalšia správna za sebou na prvý pokus | Duolingo | `res/raw/match_correct_a_a_01.mp3` … `_05.mp3` |
| correct_alt | správne až na ďalší pokus („Teraz je to správne.“) | Drops | `res/raw/effect_interaction_checkpoint_success.mp3` |
| wrong | nesprávna odpoveď | Busuu | `res/raw/wrong.mp3` |
| wrong_alt | zlé číslo v bunke GEM-u (častá drobná chyba) | Duolingo | `res/raw/wrong_answer_quiet.wav` |
| step_done | príklad z viacerých častí hotový | Memrise | `res/raw/audio_reviewing.ogg` |
| lesson_done | koniec lekcie | Duolingo | `res/raw/node_complete.mp3` bez úvodu 0,45 s (úder hneď, nie o 0,54 s neskôr) |
| lesson_done_alt | výsledok nácviku testu (body dopadnú) | Quizlet | `res/raw/study_complete.ogg` |
| streak | predĺžený rad dní, raz za deň na konci lekcie | Quizlet | `res/raw/streak_activated.ogg` |

Po spracovaní (`scripts/zvuky.py`, 18. 11. 2026):

| rola | dĺžka | RMS 100 ms | špička |
|---|---|---|---|
| correct | 507 ms | -15.0 dB | -8.9 dB |
| combo_1 | 154 ms | -15.0 dB | -9.2 dB |
| combo_2 | 155 ms | -15.0 dB | -9.8 dB |
| combo_3 | 151 ms | -15.0 dB | -10.0 dB |
| combo_4 | 150 ms | -15.0 dB | -10.1 dB |
| combo_5 | 825 ms | -15.0 dB | -5.4 dB |
| lesson_done | 961 ms | -15.0 dB | -4.3 dB |
| streak | 733 ms | -16.0 dB | -7.6 dB |
| lesson_done_alt | 1613 ms | -17.0 dB | -8.9 dB |
| correct_alt | 300 ms | -19.0 dB | -15.0 dB |
| wrong | 289 ms | -18.0 dB | -12.4 dB |
| wrong_alt | 228 ms | -22.0 dB | -8.9 dB |
| step_done | 156 ms | -20.0 dB | -13.9 dB |
| button | 55 ms | -25.0 dB | -19.7 dB |
| tap | 21 ms | -29.2 dB | -22.9 dB |
