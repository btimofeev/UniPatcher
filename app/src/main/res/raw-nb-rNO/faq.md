## Ofte Stilte Spørsmål:

#### Hva er UniPatcher?

UniPatcher er et Android-verktøy for å legge programfikser til ROM-avbildninger tilhørende forskjellige spillkonsoller.

#### Hvilke programfiks-formater støttes?

Programmet støtter IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP og XDelta3 -programfikstyper.

#### Kan jeg hacke eller cracke Android-spill med dette programmet?

Nei. UniPatcher er ikke designet for å hacke Android-spill.

#### Hva er en ROM-avbildning?

En ROM-avbildning er ei datafil som inneholder en kopi av dataen fra en videospillkasett. Uttrykket brukes i sammenheng med emulering, prosessen der gamle spill blir kopiert til ROM-filer, og kan, ved bruk av en type programvare som heter "emulator", kjøres på en datamaskin eller telefon.

#### Hva er ROM-hacking?

ROM-hacking er å modifisere dataene i ROM-avbildningen. Dette kan være å forandre grafikken, endre nivåene, endre vanskelighetsgraden, eller selv oversettelse til et annet språk der spillet aldri kom ut.

#### Hva er en programfiks?

En programfiks er ei fil som inneholder forskjellene mellom orginalversjonen av ei ROM-fil og den hackede versjonen

Programfiksen blir distribuert, og sluttbrukere legger den til en kopi av orginal-ROM-fila, som produserer en spillversjon av hack-en.

### Hvorfor ROM-hackere ikke distribuerer modifiserte spill?

Hacks og oversettelser bli i hovedsak distribuert som programfikser for å redusere nedlastingsstørrelse og for å unngå kopiretts-problematikk.

#### Hvordan legge en programfiks til ei ROM-fil?

Du må først velge ROM-fila og deretter programfiksen, trykk så på den runde røde knappen.

Som resultat får du ei programfikset ROM-fil, som er å finne i samme mappe, sammen med den opprinnelige ROM-fila.

#### Programmet viser en melding etter filvalg: "Akrivet bør utpakkes i et eksternt program".

Fila du har valgt er et arkiv. Akrivet inneholder mapper og filer i komprimert format.

UniPatcher støtter for tiden ikke utpakking av arkiver, så du må pakke ut arkivet ditt med et annet program. Jeg anbefaler gratisprogrammet [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Programmet viser feilen: "Denne ROM-fila er ikke kompatibel med programfiksen".

Programmet viser denne feilen hvis sjekksummen lagret i programfiksen ikke samsvaren med sjekksummen tilhørende ROM-fila. Dette betyr at ROM-fila ikke er kompatibel med programfiksen. Du må velge en annen ROM-fil. Vanligvis er det flere ROM-filer for hvert spill (som versjonen for Europa, USA, Japan, bra eller dårlige dumper, osv.).

ROM-hackere offentliggjør ofte sjekksummen av tilhørende ROM-fil (på en nettside eller README-fil). Du må sammenligne denne med den fra ROM-fila du har. Trykk lenge på filbehandleren og du vil disse tre linjene: CRC32, SHA1 og MD5. Du har rett ROM hvis én av disse linjene samsvarer med sjekksummen til programfiksen. Hvis sjekksummene ikke stemmer må du ha en annen ROM-fil.

I verste fall, hvis du ikke finner rett ROM-fil, kan du sette valget "Ignorer sjekksum" i innstillingene. Husk at i dette tilfellet vil spillet kanskje inneholde feil eller være helt ubrukelig.

#### Jeg kan ikke finne rett ROM-fil for spillet "Pokémon Emerald".

De fleste programfiksene til spillet fungerer med ROM-fila "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Jeg legger til IPS-programfiksen og så fungerer ikke spillet / inneholder grafiske artefakter. Hva gjør jeg galt?

Progamfikser i IPS-format inneholder ikke noen sjekksum. Derfor vil programfiksen gå sammen med enhver (selv gale) ROM-filer. I sådant fall, må du se deg om etter en annen ROM-fil.

#### Hva kan jeg gjøre med filer av typen .ECM?

ECM er et data-komprimeringsformat designet spesielt for diskavbildninger. Du kan pakke opp fila ved bruk av [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver)-programmet.

### Programmet viser feilen: "Kunne ikke kopiere fil".

Feilen opptrer på noen enheter med Android 4.4+ som har eksternt SD-kort. Android tillater ikke programmer å skrive data til SD-kort på disse enhetene (en detaljert beskrivelse av problemet finnes [her] (http://www.androidpolice.com/2014/02/17/external-blues-google-has-brought-big-changes-to-sd-cards-in-kitkat-and-even-samsung-may-be-implementing-them/)).

Det er flere måter å løse dette problemet på:

- Ikke utfør programfikser for ROM-filer plassert på eksternt SD-kort. Bare flytt ROM-fila til det interne minnet på enheten.
- Oppgi stien til ei mappe på internminne på enheten som utdatamappe (i innstillingene).
- Oppgi stien til **Android/data/org.emunix.unipatcher/** mappen på eksternt SD-kort som utdatamappe (i innstillingene).
- Installer [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix)-programmet (krever ROOT-tilgang).

#### Programmet viser feilmeldingen: "Fila har feil sjekksum etter at den ble programfikset".

Kanskje det er en feil i programmet mitt. Kontakt meg på [e-post](mailto:unipatcher@gmail.com) og legg ved hele programfiksen.

#### Har UniPatcher noen flere funksjoner?

Ja. UniPatcher kan:

- Opprett XDelta3-programfikser.
- Fiks sjekksum for Sega Mega Drive / Sega Genesis -spill
- Legg til eller fjern SMC-hode for Super Nintendo-spill.

#### Hvorfor må jeg fikse sjekksum for Sega Mega Drive-spill?

Sega Mega Drive (Genesis)-spill har sjekksummen sin skrevet inn i ROM-fila. Hvis du bare endrer en del av spillet, vil de ikke samsvare, noe som medfører at den ikke kan kjøres. Hva dette gjør er å regne ut korrekt sjekksum av endringen, og skriver det til den modifiserte ROM-fila."

**Advarsel:** Denne funksjonen lager ikke en sikkerhetskopi-ROM.

#### Hvorfor må man noen ganger legge til eller fjerne SMC-hode fra Super Nintendo-spill?

Et SMC-hode er 512 Byte i starten på noen SNES-ROM-avbildninger. Disse Bytene har ingen funksjon, men de endrer plasseringen for etterfølgende data. Fjerning eller tillegg av hode brukes noen ganger for å legge til en programfiks på rett vis.

**Advarsel:** Denne funksjonen lager ikke en sikkerhetskopi-ROM.

#### Hvordan oversette programmet?

Hvis du vil oversette programmet til et annet språk eller forbedre en eksisterende oversettelse, kan du gjøre det på [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/)-siden.

#### Jeg har et spørsmål, en funksjonsforespørsel eller en feilrapport.

Kontakt meg på e-post <unipatcher@gmail.com>. Skriv på Engelsk eller Russisk. Hvis du har problemer med programfiksing, legg ved hele programfiksen og skriv navnet på ROM-fila di, det vil spare tid.