## Preguntas frecuentes:

#### ¿Qué es UniPatcher?

UniPatcher es una herramienta Android para aplicar parches a imágenes ROM de varias consolas de videojuegos.

#### ¿Qué formatos de parche son compatibles?

La aplicación soporta parches IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP y XDelta3.

#### ¿Puedo hackear o crackear juegos Android usando esta aplicación?

No. UniPatcher no está diseñado para hackear los juegos de Android.

#### ¿Qué es una imagen de ROM?

Una imagen ROM es un archivo de computadora que contiene una copia del cartucho de videojuegos. A través del proceso de emulación, copias ese archivo, lo ejecutas en una pieza de software que se llama un "emulador", para disfrutar del juego en su ordenador o teléfono.

#### ¿Qué es ROM hacking?

ROM hacking es modificar los datos en una imagen ROM. Esto puede tomar la forma de alterar gráficos, cambiar niveles del juego, ajustar el factor de dificultad, o incluso la traducción del juego a un idioma que no estaba disponible originalmente.

#### ¿Qué es un parche?

Un parche es un archivo que contiene las diferencias entre la versión original de un ROM y la versión hackeada.

El parche se distribuye, y los usuarios finales aplican el parche a una copia de la ROM original, que produce una versión jugable del hack.

#### ¿Por qué los ROM hackers no distribuyen juegos modificados?

Los hacks y las traducciones se distribuyen generalmente como parches para reducir el tamaño de la descarga y evitar los problemas de copyright.

#### ¿Cómo aplicar un parche a la ROM?

Debe elegir el archivo de ROM y el parche, entonces pulse el botón redondo rojo.

Como resultado, obtiene una ROM parcheada, que se ubicará en el mismo directorio con la ROM original.

#### La aplicación muestra un mensaje después de la selección del archivo: "El archivo debe descomprimirse en un programa externo".

El archivo que ha seleccionado es un archivo. El archivo contiene los directorios y archivos en un formato comprimido.

Actualmente UniPatcher no puede extraer archivos, por lo que necesita descomprimir su archivo en un programa diferente. Te recomiendo un programa gratis [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### La aplicación muestra el error: "Esta ROM no es compatible con el parche".

The app will show this error if the checksum stored in the patch does not match the checksum of the ROM. This means that the ROM file is not compatible with the patch. You need to choose a different ROM file. Usually there are several ROMs for each game (such as the version for Europe, USA, Japan, good or bad dumps, etc.).

ROM hackers often publish checksum of the accompanying ROM file (on a web page or in README file). Compare that to the one you have. Long tap the file in the file manager and you will see these 3 lines: CRC32, SHA1 and MD5. If one of those numbers are the same, you have the ROM the patch was written for. If not, you need a different ROM.

In the worst case, if you can not find the correct ROM file, you can set the option "Ignore the checksum" in the settings. But bear in mind that in this case the game may contain bugs or be completely unplayable.

#### No puedo encontrar la ROM correcta para el juego "Pokémon Esmeralda".

La mayoría de los parches del juego funcionan con la ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Aplico el parche IPS y luego el juego no funciona/contiene fallos gráficos. ¿Qué estoy haciendo mal?

IPS format patches do not contain a checksum. Therefore, the patch will apply to any (even wrong) ROM file. In this case, you need to look for another ROM file.

#### ¿Qué puedo hacer con el tipo de archivo .ECM?

ECM es un formato de compresión de datos diseñado específicamente para imágenes de disco. Puede descomprimir el archivo con el programa [ZArchiver]. (https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver)

#### La aplicación muestra el error: "No se pudo copiar archivo".

The error occurs on some devices with Android 4.4+ having an external SD card. Android does not allow applications to write data to a SD card on these devices (a detailed description of the problem [here](http://www.androidpolice.com/2014/02/17/external-blues-google-has-brought-big-changes-to-sd-cards-in-kitkat-and-even-samsung-may-be-implementing-them/)).

There are several ways to solve this problem:

- Do not apply patches to the ROM file located on the external SD card. Just move the ROM file into the internal memory of the device.
- Specify the path to any directory in the internal memory of the device as the output directory (in the settings).
- Specify the path to **Android/data/org.emunix.unipatcher/** directory on the external SD card as the output directory (in the settings).
- Instalar la aplicación [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix) (Requiere acceso a ROOT).

#### La aplicación muestra el error: "El archivo tiene checksum incorrecto después de haber sido parcheado".

Tal vez sea un error en mi programa. Por favor, póngase en contacto conmigo en [e-mail](mailto:unipatcher@gmail.com) y adjunte el parche en el mensaje.

#### ¿Tiene UniPatcher algunas características adicionales?

Sí. UniPatcher puede:

- Crear parches XDelta3.
- Fijar checksum para juegos de Sega Mega Drive / Sega Genesis
- Añadir o quitar cabezera SMC para juegos de Super Nintendo.

#### ¿Por qué tengo que fijar el checksum para los juegos de Sega Mega Drive?

Sega Mega Drive (Genesis) games have their checksum written into the ROM. If you only change any part of the game, they will not match, failing to run as a result. What this does is calculate the correct checksum of the change and write it to the modified ROM-file."

**Advertencia:** Esta función no crea una ROM de respaldo.

#### ¿Por qué es necesario a veces para añadir o quitar cabeceras SMC de juegos de Super Nintendo?

Una cabecera SMC es 512 bytes encontrados al inicio de algunas imágenes ROM de SNES. Estos bytes no tienen ningún propósito, pero cambian la ubicación de los datos restantes. Quitando o agregando una cebecera se usa a veces con el fin de aplicar correctamente un parche.

**Advertencia:** Esta función no crea una ROM de respaldo.

#### ¿Cómo traducir la aplicación?

Si desea traducir la aplicación en otro idioma o mejorar una traducción existente, puede hacerlo en el sitio [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/).

#### Tengo una pregunta, una petición de característica o un informe de error.

Contactame en e-mail <unipatcher@gmail.com>. Por favor escriba en Inglés o Ruso. Si tienes problemas con parches, adjunte el parche al mensaje y escriba el nombre de su ROM, ahorrará nuestro tiempo.