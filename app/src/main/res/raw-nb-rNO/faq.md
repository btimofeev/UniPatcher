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

You have to select a ROM file, a patch and choose which file to save, then click on the red round button. Files are selected through the standard Files application (or through one of the file managers you have installed). The application will show a message when the file is patched. Do not close the application until the file is patched.

#### Programmet viser en melding etter filvalg: "Akrivet bør utpakkes i et eksternt program".

Fila du har valgt er et arkiv. Akrivet inneholder mapper og filer i komprimert format.

UniPatcher støtter for tiden ikke utpakking av arkiver, så du må pakke ut arkivet ditt med et annet program. Jeg anbefaler gratisprogrammet [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Programmet viser feilen: "Denne ROM-fila er ikke kompatibel med programfiksen".

Programmet viser denne feilen hvis sjekksummen lagret i programfiksen ikke samsvaren med sjekksummen tilhørende ROM-fila. Dette betyr at ROM-fila ikke er kompatibel med programfiksen. Du må velge en annen ROM-fil. Vanligvis er det flere ROM-filer for hvert spill (som versjonen for Europa, USA, Japan, bra eller dårlige dumper, osv.).

ROM-hackere offentliggjør ofte sjekksummen av tilhørende ROM-fil (på en nettside eller README-fil). Du må sammenligne denne med den fra ROM-fila du har. Trykk lenge på filbehandleren og du vil disse tre linjene: CRC32, SHA1 og MD5. Du har rett ROM hvis én av disse linjene samsvarer med sjekksummen til programfiksen. Hvis sjekksummene ikke stemmer må du ha en annen ROM-fil.

I verste fall, hvis du ikke finner rett ROM-fil, kan du sette valget "Ignorer sjekksum" i innstillingene. Husk at i dette tilfellet vil spillet kanskje inneholde feil eller være helt ubrukelig.

#### I can't patch "Super Mario World (U) [!].smc"

This ROM contains an SMC header, while most patches for this game require the ROM to not have this header. You can remove SMC header by selecting the appropriate item in the menu on the left and then apply the patch to the resulting ROM.

#### Jeg kan ikke finne rett ROM-fil for spillet "Pokémon Emerald".

De fleste programfiksene til spillet fungerer med ROM-fila "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Jeg legger til IPS-programfiksen og så fungerer ikke spillet / inneholder grafiske artefakter. Hva gjør jeg galt?

Progamfikser i IPS-format inneholder ikke noen sjekksum. Derfor vil programfiksen gå sammen med enhver (selv gale) ROM-filer. I sådant fall, må du se deg om etter en annen ROM-fil.

#### Hva kan jeg gjøre med filer av typen .ECM?

ECM er et data-komprimeringsformat designet spesielt for diskavbildninger. Du kan pakke opp fila ved bruk av [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver)-programmet.

#### Har UniPatcher noen flere funksjoner?

Ja. UniPatcher kan:

- Opprett XDelta3-programfikser.
- Fiks sjekksum for Sega Mega Drive / Sega Genesis -spill
- Remove SMC header in Super Nintendo games.

#### Hvorfor må jeg fikse sjekksum for Sega Mega Drive-spill?

Sega Mega Drive (Genesis)-spill har sjekksummen sin skrevet inn i ROM-fila. Hvis du bare endrer en del av spillet, vil de ikke samsvare, noe som medfører at den ikke kan kjøres. Hva dette gjør er å regne ut korrekt sjekksum av endringen, og skriver det til den modifiserte ROM-fila."

**Advarsel:** Denne funksjonen lager ikke en sikkerhetskopi-ROM.

#### Why is it sometimes necessary to remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing a header is sometimes used for the purpose of correctly applying a patch.

#### Hvordan oversette programmet?

Hvis du vil oversette programmet til et annet språk eller forbedre en eksisterende oversettelse, kan du gjøre det på [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/)-siden.

#### Jeg har et spørsmål, en funksjonsforespørsel eller en feilrapport.

Kontakt meg på e-post <unipatcher@gmail.com>. Skriv på Engelsk eller Russisk. Hvis du har problemer med programfiksing, legg ved hele programfiksen og skriv navnet på ROM-fila di, det vil spare tid.