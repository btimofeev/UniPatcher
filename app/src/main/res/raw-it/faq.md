## Domande Frequenti:

#### Cosa è UniPatcher?

UniPatcher è uno strumento per Android per applicare delle patch alle ROM di varie console per videogiochi.

#### Quali formati di patch sono supportati?

L'app supporta le patch IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP e XDelta3.

#### Posso hackerare o crackare i giochi di Android con questa app?

No. UniPatcher non è progettato per hackerare i giochi di Android.

#### Cosa è una immagine ROM?

Una immagine ROM è un file del computer che contiene una copia dei dati da una memoria di sola lettura della cartuccia di un videogioco. Il termine è utilizzato nel contesto dell'emulazione, dove i giochi vengono copiati in un file ROM e possono essere eseguiti in un computer o un telefono, usando un programma chiamato emulatore.

#### Cosa è l'hackeraggio di una ROM?

L'hackeraggio di una ROM è la modifica dei dati in una immagine ROM. Questo può esprimersi come l'alterazione grafica, il cambiamento dei livelli, il ritocco del fattore di difficoltà, o anche la traduzione in un linguaggio per cui il gioco non è stato reso disponibile in origine.

#### Cosa è una patch?

Una patch è un file che contiene le differenze tra la versione originale della ROM e la versione hackerata.

La patch è distribuita, e gli utenti applicazione la patch ad una copia della ROM originale, che produce una versione avviabile dell'hack.

#### Perchè gli hacker di ROM non distribuiscono i giochi modificati?

Gli hack e le traduzioni sono generalmente distribuiti come patch per ridurre la dimensione del download ed evitare problemi di copyright.

#### Come si applica una patch ad una ROM?

É molto semplice: devi scegliere il file ROM e la patch, quindi premi sul pulsante rotondo rosso.

Come risultato, hai una ROM patchata, che verrà posizionata nella stessa cartella con la ROM originale.

#### L'app mostra un messaggio dopo la selezione del file: "L'archivio dovrebbe essere spacchettato in un programma esterno"

Il file che hai selezionato è un archivio. L'archivio contiene le cartelle e i file in un file compresso.

Al momento attuale UniPatcher non può estrarre archivi, quindi c'è bisogno di spacchettare il tuo archivio in un programma differente. Raccomandiamo un programma gratuito [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### L'app mostra l'errore: "Questa ROM non è compatibile con la patch".

L'app mostrerà questo errore se il checksum memorizzato nella patch non coincide con il checksum della ROM. Questo significa che la ROM non è compatibile con la patch. Devi scegliere una ROM differente. Di solito ci sono diverse ROM per ogni gioco (ad esempio la versione per Europa, USA, Giappone, buoni o brutti dettagli, etc).

Gli hacker delle ROM speso pubblicano il checksum del file ROM (su una pagina web o su un file Readme). Devi compararlo con la tua ROM. Tieni premuto sul file nel gestore di file e vedrai 3 linee: CRC21, SHA1 e MD5. Se una di queste linee coincide con il checksum che è stato scritto dall'hacker della ROM, allora hai la ROM corretta.

#### Non trovo la ROM corretta del gioco "Pokemon Emerald".

La maggior parte del gioco funziona con la ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Io applico la patch IPS e poi il gioco non funziona / contiene difetti grafici. Cosa sto sbagliando?

Le patch con il formato IPS non contengono un checksum. Perciò, la patch verrà applicata ad ogni ROM (anche sbagliate). In questo caso, devi cercare un'altra ROM.

#### Cosa posso fare con un file .ECM?

ECM è un formato di compressione progettato specificamente per i dischi immagine. Puoi decomprimere il file usando il programma [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### L'app mostra l'errore: "Non puoi copiare il file".

L'errore appare su alcuni dispositivi con Android 4.4. Soluzioni possibili:

- Copia il file ROM nella memory card nella cartella **Android/data/org.emunix.unipatcher/**. Quindi devi selezionare la ROM da questa cartella.
- Installa l'applicazione [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix) (richiede permessi ROOT)

#### L'app mostra l'errore: "Il file ha un checksum sbagliato dopo che è stato patchato".

Forse c'è un bug nel mio programma. Ti prego di contattarmi alla [e-mail](mailto:mashin87@gmail.com) e allega la patch alla mail.

#### Ha UniPatcher qualche funzione aggiuntiva?

Si. UniPatcher può:

- create XDelta3 patches.
- Risolvere checksum per un gioco Sega Mega Drive / Sega Genesis.
- aggiungere o rimuovere l'intestazione SMC per un gioco Super Nintendo.

#### Perchè devo risolvere il checksum per i giochi del Sega Mega Drive?

Il Sega Mega Drive (Genesis) ha delle protezioni dalle modifiche del gioco. La ROM contiene il valore checksum, e se è diverso dal checksum effettivo il gioco mostra uno schermo rosso e smette di funzionare. Questa funzione scrive il checksum corretto nella ROM.

**Attenzione:** Questa funzione non crea una ROM di backup.

#### Perchè è necessario a volte aggiungere o rimuovere l'intestazione SMC dai giochi del Super Nintendo?

Un'intestazione SMC sono 512 byte che si trova all'inizio di alcune immagini ROM del SNES. Questi byte non hanno uno scopo, ma cambiano la locazione dei dati rimanenti. La rimozione o l'aggiunta dell'intestazione è usata a volte con lo scopo di applicare correttamente la patch.

**Attenzione:** Questa funzione non crea una ROM di backup.

#### Come tradurre l'app?

Se ti piacerebbe tradurre l'app in una lingua diversa o migliorare una traduzione esistente, puoi farlo sul [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/) sito.

#### Ho una domanda, la richiesta di una funzionalità o la segnalazione di un bug.

Contattami alla e-mail <mashin87@gmail.com>. Per favore, scrivi in inglese o in russo. Se hai qualche problema con il patch, allega la patch alla mail e scrivi il nome della tua ROM, ti farà risparmiare tempo.
