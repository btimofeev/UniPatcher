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

Este error ocurre si la checksum del patch no coincide con la checksum del ROM, lo cual significa que el ROM es incompatible con el patch. Por favor elige un ROM diferente. Generalmente hay varios ROM de cada juego (por ejemplo la versión europea, americana, japonesa, buen o mal dump del ROM, etc.)

Los hackers de ROM suelen publicar el patch con la checksum del ROM adjunta (en la página web o el archivo LÉEME). Compara la del patch con la del ROM que tienes. Largo toca el archivo en el gestor de archivos y verás 3 líneas: CRC32, SHA1, y MD5. Si uno de los números coincide, tienes el patch hecho para el ROM.  Si no, tienes el ROM equivocado.

Si desafortunadamente no puedes encontrar el ROM correcto, puedes activar la opción “Ignorar checksums” en la configuración. Pero ten en cuenta que el juego patcheado puede contener fallos o ser completamente injugable.

#### No puedo encontrar la ROM correcta para el juego "Pokémon Esmeralda".

La mayoría de los parches del juego funcionan con la ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Aplico el parche IPS y luego el juego no funciona/contiene fallos gráficos. ¿Qué estoy haciendo mal?

Los patches IPS format no tienen una checksum. Por lo tanto cualquier ROM (incluso si es el equivocado) será patcheado. En este caso, tendrás que buscar otro archivo ROM.

#### ¿Qué puedo hacer con el tipo de archivo .ECM?

ECM es un formato de compresión de datos diseñado específicamente para imágenes de disco. Puede descomprimir el archivo con el programa [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver)

#### La aplicación muestra el error: "No se pudo copiar archivo".

El error ocurre en algunos dispositivos de Android 4.4+ con una tarjeta SD externa. Android no permite que los aplicaciones escriban datos con una tarjeta SD en estos dispositivos (se puede encontrar [aquí](http://www.androidpolice.com/2014/02/17/external-blues-google-has-brought-big-changes-to-sd-cards-in-kitkat-and-even-samsung-may-be-implementing-them/) una descripción detallada en inglés).

Hay varias maneras de resolver este problema:

- No patchees los archivos ROM ubicadas en la tarjeta SD externa. Mueve el ROM a la memoria interna del dispositivo.
- Espicifica la ruta a cualquier directorio en la memoria interna de la carpeta de salida.
- Espicifica la ruta de la carpeta de salida a **Android/data/org.emunix.unipatcher/** en la tarjeta SD externa (en la configuración).
- Instalar la aplicación [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix) (Requiere acceso a ROOT).

#### La aplicación muestra el error: "El archivo tiene checksum incorrecto después de haber sido parcheado".

Tal vez sea un error en mi programa. Por favor, póngase en contacto conmigo en [e-mail](mailto:unipatcher@gmail.com) y adjunte el parche en el mensaje.

#### ¿Tiene UniPatcher algunas características adicionales?

Sí. UniPatcher puede:

- Crear parches XDelta3.
- Fijar checksum para juegos de Sega Mega Drive / Sega Genesis
- Remove SMC header in Super Nintendo games.

#### ¿Por qué tengo que fijar el checksum para los juegos de Sega Mega Drive?

La checksum de los juegos Sega Megadrive (Genesis) están escritos en sus ROM. Si editas cualquier parte del juego, ya no coinciden, y no se iniciará. Lo que esto hace es calcular la checksum correcta del cambio y escribirlo al archivo ROM modificado.”

**Advertencia:** Esta función no crea una ROM de respaldo.

#### Why is it sometimes necessary to remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing a header is sometimes used for the purpose of correctly applying a patch.

#### ¿Cómo traducir la aplicación?

Si desea traducir la aplicación en otro idioma o mejorar una traducción existente, puede hacerlo en el sitio [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/).

#### Tengo una pregunta, una petición de característica o un informe de error.

Contactame en e-mail <unipatcher@gmail.com>. Por favor escriba en Inglés o Ruso. Si tienes problemas con parches, adjunte el parche al mensaje y escriba el nombre de su ROM, ahorrará nuestro tiempo.