# Čo appka posiela

Úplný zoznam toho, čo FITagoras posiela po sieti a komu. Krátko je to v [README](../README.md).

Postup sa ukladá len v telefóne (vo webovej verzii v prehliadači). Webová verzia neposiela štatistiky ani hlásenia
chýb, zo servera si sťahuje len samotnú stránku a klipy z prednášok, ktoré pustíš. Appka pre Android posiela po sieti toto:

- **Štatistiky o učení** na server autora (fitagoras.hamper.dev). Sú zapnuté predvolene, appka to povie na úvodnej
  obrazovke a vypneš ich v Nastaveniach (Súkromie, Posielať štatistiky). Posiela sa:
  - otvorenie appky s predmetom a paralelkou; začiatok, kroky, preskočenia, odchod a koniec lekcie a ako dlho trvali;
  - odpovede: ktorá úloha (pri vygenerovanej aj jej čísla), či správne, či s pomocou, koľký pokus a pri nesprávnej
    aj to, čo si zadal;
  - nápoveda, Ako na to?, Prečo? a riešenie; koľko z klipu si pozrel; body z cvičnej písomky; ponuka a inštalácia
    aktualizácie; pád appky (druh chyby, jej správa a miesto v kóde);
  - pri každej udalosti dátum a čas, verzia appky a o telefóne: výrobca, model, verzia Androidu, veľkosť obrazovky,
    mierka písma, tmavý režim, jazyk, časové pásmo, typ siete a či ide o emulátor;
  - náhodné číslo inštalácie a odtlačok telefónu, aby história prežila preinštalovanie (SHA-256 z identifikátora,
    ktorý Android dáva tejto appke v tvojom telefóne, so soľou appky).

  Neposiela sa meno, e-mail ani účet. Server podľa autora IP adresu neukladá, čas drží na minútu a po roku všetko
  maže. Keď štatistiky vypneš, ďalšie sa neposielajú, neodoslané sa zahodia a server zabudne odtlačok telefónu.
  Čo už odišlo, tam ostane do ročného zmazania, ale s telefónom sa už nespojí. Keď ich znova zapneš, začne sa nová
  história pod novým číslom a odtlačok sa už neposiela.
- **Nahlásiť chybu:** tvoj text, úloha alebo klip, ku ktorému patrí (pri klipe aj miesto v zázname), lekcia, čas
  a verzia appky. Ide aj pri vypnutých štatistikách: pri zapnutých pod číslom inštalácie, takže sa dá spojiť s tvojou
  históriou, pri vypnutých pod jednorazovým číslom.
- **Kontrola aktualizácie** (fitagoras.hamper.dev): len verzia, ktorú máš, pri spustení a pri každom návrate do
  appky, najviac raz za minútu. Vypneš ju v Nastaveniach (Aktualizácie). Novú verziu appka stiahne, až keď ťukneš na Aktualizovať.
- **Klipy z prednášok** sa stiahnu zo servera, keď ich pustíš. Appka k nim nič o tebe nepridáva; server vidí len to,
  čo pri sťahovaní vidí každý server: IP adresu a model telefónu s verziou Androidu, ktoré prehrávač posiela sám.

**Pytagoras**, AI pomocník (v appke aj vo webovej verzii), posiela dáta do OpenAI, nie autorovi, a len keď sa
prihlásiš svojím účtom ChatGPT a použiješ Pytagora (Opýtať sa, Odfoť riešenie alebo postup). Vtedy ide pod tvojím
účtom tvoja otázka, fotky papiera (bez polohy), text úlohy so vzorovým riešením z appky a pri príklade aj to, čo si
zadal. Míňa to limit tvojho ChatGPT plánu. Appka žiada, aby OpenAI rozhovor neukladal; čo si aj tak ponechá, určujú
jeho podmienky. Prihlásenie ostáva v telefóne zašifrované (vo webovej verzii v úložisku prehliadača), heslo appka
nevidí a Odpojiť (v Nastaveniach) ho zruší aj v OpenAI.
