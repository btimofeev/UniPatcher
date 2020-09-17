## Domande Frequenti:

#### Cosa è UniPatcher?

UniPatcher è uno strumento per Android per applicare delle patch alle ROM di varie console per videogiochi.

#### Quali formati di patch sono supportati?

L'app supporta le patch IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP e XDelta3.

#### Posso hackerare o crackare i giochi di Android con questa app?

No. UniPatcher non è progettato per hackerare i giochi di Android.

#### Cosa è una immagine ROM?

A ROM image is a computer file containing a copy of video game cartridge. Through the process of emulation, you copy that file out, run it in a piece of software called an "emulator", to enjoy the game on your computer or phone.

#### Cosa è l'hackeraggio di una ROM?

L'hackeraggio di una ROM è la modifica dei dati in una immagine ROM. Questo può esprimersi come l'alterazione grafica, il cambiamento dei livelli, il ritocco del fattore di difficoltà, o anche la traduzione in un linguaggio per cui il gioco non è stato reso disponibile in origine.

#### Cosa è una patch?

Una patch è un file che contiene le differenze tra la versione originale della ROM e la versione hackerata.

La patch è distribuita, e gli utenti applicazione la patch ad una copia della ROM originale, che produce una versione avviabile dell'hack.

#### Perchè gli hacker di ROM non distribuiscono i giochi modificati?

Gli hack e le traduzioni sono generalmente distribuiti come patch per ridurre la dimensione del download ed evitare problemi di copyright.

#### Come si applica una patch ad una ROM?

Devi scegliere il file ROM e la patch, quindi premi sul pulsante rotondo rosso.

Come risultato, hai una ROM patchata, che verrà posizionata nella stessa cartella con la ROM originale.

#### L'app mostra un messaggio dopo la selezione del file: "L'archivio dovrebbe essere spacchettato in un programma esterno"

Il file che hai selezionato è un archivio. L'archivio contiene le cartelle e i file in un file compresso.

Currently UniPatcher can not extract archives, so you need to unpack your archive in a different program. I recommend a gratis program [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### L'app mostra l'errore: "Questa ROM non è compatibile con la patch".

The app will show this error if the checksum stored in the patch does not match the checksum of the ROM. This means that the ROM file is not compatible with the patch. You need to choose a different ROM file. Usually there are several ROMs for each game (such as the version for Europe, USA, Japan, good or bad dumps, etc.).

ROM hackers often publish checksum of the accompanying ROM file (on a web page or in README file). Compare that to the one you have. Long tap the file in the file manager and you will see these 3 lines: CRC32, SHA1 and MD5. If one of those numbers are the same, you have the ROM the patch was written for. If not, you need a different ROM.

In the worst case, if you can not find the correct ROM file, you can set the option "Ignore the checksum" in the settings. But bear in mind that in this case the game may contain bugs or be completely unplayable.

#### I can't patch "Super Mario World (U) [!].smc"

This ROM contains an SMC header, while most patches for this game require the ROM to not have this header. You can remove SMC header by selecting the appropriate item in the menu on the left and then apply the patch to the resulting ROM.

#### Non trovo la ROM corretta del gioco "Pokémon Emerald".

La maggior parte del gioco funziona con la ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Io applico la patch IPS e poi il gioco non funziona / contiene difetti grafici. Cosa sto sbagliando?

IPS format patches do not contain a checksum. Therefore, the patch will apply to any (even wrong) ROM file. In this case, you need to look for another ROM file.

#### Cosa posso fare con un file .ECM?

ECM è un formato di compressione progettato specificamente per i dischi immagine. Puoi decomprimere il file usando il programma [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Ha UniPatcher qualche funzione aggiuntiva?

Si. UniPatcher può:

- Create XDelta3 patches.
- Risolvere checksum per un gioco Sega Mega Drive / Sega Genesis.
- Remove SMC header in Super Nintendo games.

#### Perchè devo risolvere il checksum per i giochi del Sega Mega Drive?

Sega Mega Drive (Genesis) games have their checksum written into the ROM. If you only change any part of the game, they will not match, failing to run as a result. What this does is calculate the correct checksum of the change and write it to the modified ROM-file."

**Attenzione:** Questa funzione non crea una ROM di backup.

#### Why is it sometimes necessary to remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing a header is sometimes used for the purpose of correctly applying a patch.

#### Come tradurre l'app?

Se ti piacerebbe tradurre l'app in una lingua diversa o migliorare una traduzione esistente, puoi farlo sul [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/) sito.

#### Ho una domanda, la richiesta di una funzionalità o la segnalazione di un bug.

Contattami alla e-mail <unipatcher@gmail.com>. Per favore, scrivi in inglese o in russo. Se hai qualche problema con il patch, allega la patch alla mail e scrivi il nome della tua ROM, ti farà risparmiare tempo.