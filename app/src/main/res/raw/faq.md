## Frequently Asked Questions:

#### What is UniPatcher?

UniPatcher is an Android tool for applying patches to ROM images of various video game consoles.

#### What patch formats are supported?

The app supports IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP and XDelta3 patches.

#### Can I hack or crack Android game using this app?

No. UniPatcher is not designed to hack the Android games.

#### What is ROM image?

A ROM image is a computer file containing a copy of video game cartridge. Through the process of emulation, you copy that file out, run it in a piece of software called an "emulator", to enjoy the game on your computer or phone.

#### What is ROM hacking?

ROM hacking is modifying the data in a ROM image. This may take the form of altering graphics, changing game levels, tweaking difficulty factor, or even translation into a language for which a game was not originally made available.

#### What is a patch?

A patch is a file that contains the differences between the original version of a ROM and the hacked version.

The patch is distributed, and end users apply the patch to a copy of the original ROM, which produces a playable version of the hack.

#### Why ROM hackers do not distribute modified games?

Hacks and translations are generally distributed as patches to reduce download size and avoid copyright issues.

#### How to apply a patch to ROM?

You have to select a ROM file, a patch and choose which file to save, then click on the red round button. Files are selected through the standard Files application (or through one of the file managers you have installed). The application will show a message when the file is patched. Do not close the application until the file is patched.

#### App shows a message after file selection: "Archive should be unpacked in an external program".

The file you have selected is an archive. The archive contains the directories and files in a compressed format.

Currently UniPatcher can not extract archives, so you need to unpack your archive in a different program. I recommend a gratis program [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### The app shows the error: "This ROM is not compatible with the patch".

The app will show this error if the checksum stored in the patch does not match the checksum of the ROM. This means that the ROM file is not compatible with the patch. You need to choose a different ROM file. Usually there are several ROMs for each game (such as the version for Europe, USA, Japan, good or bad dumps, etc.).

ROM hackers often publish checksum of the accompanying ROM file (on a web page or in README file). Compare that to the one you have. Long tap the file in the file manager and you will see these 3 lines: CRC32, SHA1 and MD5. If one of those numbers are the same, you have the ROM the patch was written for. If not, you need a different ROM.

In the worst case, if you can not find the correct ROM file, you can set the option "Ignore the checksum" in the settings. But bear in mind that in this case the game may contain bugs or be completely unplayable.

#### I can't patch "Super Mario World (U) [!].smc"

This ROM contains an SMC header, while most patches for this game require the ROM to not have this header. You can remove SMC header by selecting the appropriate item in the menu on the left and then apply the patch to the resulting ROM.

#### I can not find the correct ROM for the game "Pokémon Emerald".

Most of the patches of the game work with ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### I apply the IPS patch and then the game is not working / contains graphic glitches. What am I doing wrong?

IPS format patches do not contain a checksum. Therefore, the patch will apply to any (even wrong) ROM file. In this case, you need to look for another ROM file.

#### What can I do with .ECM file type?

ECM is a data compression format designed specifically for disc images. You can decompress the file using [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver) program.

#### Has UniPatcher some additional features?

Yes. UniPatcher can:

- Create XDelta3 patches.
- Fix checksum for a Sega Mega Drive / Sega Genesis games.
- Remove SMC header in Super Nintendo games.

#### Why do I have to fix checksum for the Sega Mega Drive games?

Sega Mega Drive (Genesis) games have their checksum written into the ROM. If you only change any part of the game, they will not match, failing to run as a result. What this does is calculate the correct checksum of the change and write it to the modified ROM-file."

**Warning:** This function does not create a backup ROM.

#### Why is it sometimes necessary to remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing a header is sometimes used for the purpose of correctly applying a patch.

#### How to translate the app?

If you would like to translate the app into another language or improve an existing translation, you can do it on [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/) site.

#### I have a question, a feature request or a bug report.

Contact me at e-mail <unipatcher@gmail.com>. Please write in English or Russian. If you have problems with patching, attach the patch to the letter and write the name of your ROM, it will save our time.
