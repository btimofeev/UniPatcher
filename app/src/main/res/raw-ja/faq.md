## よくある質問と答え

#### UniPatcher とは?

UniPatcher は、さまざまなビデオゲームコンソールの ROM イメージにパッチを適用するための Android ツールです。

#### サポートされているパッチ形式は?

アプリは IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP および XDelta3 パッチ形式をサポートしています。

#### このアプリを使用して Android のゲームをハックまたはクラックすることはできますか?

いいえ、UniPatcher は Android のゲームをハックするようには設計されていません。

#### ROM イメージとは?

ROM イメージは、ビデオゲームカートリッジのコピーを含むコンピューターファイルです。 エミュレーションのプロセスを通して、そのファイルをコピーし、 "エミュレーター" と呼ばれるソフトウェアで実行して、コンピューターや電話でゲームを楽しむことができます。

#### What is ROM hacking?

ROM hacking is modifying the data in a ROM image. This may take the form of altering graphics, changing game levels, tweaking difficulty factor, or even translation into a language for which a game was not originally made available.

#### What is a patch?

A patch is a file that contains the differences between the original version of a ROM and the hacked version.

The patch is distributed, and end users apply the patch to a copy of the original ROM, which produces a playable version of the hack.

#### Why ROM hackers do not distribute modified games?

Hacks and translations are generally distributed as patches to reduce download size and avoid copyright issues.

#### How to apply a patch to ROM?

You must choose the ROM file and the patch, then tap on the red round button.

As a result, you get a patched ROM, which will be located in the same directory with the original ROM.

#### App shows a message after file selection: "Archive should be unpacked in an external program".

The file you have selected is an archive. The archive contains the directories and files in a compressed format.

Currently UniPatcher can not extract archives, so you need to unpack your archive in a different program. I recommend a free program [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### The app shows the error: "This ROM is not compatible with the patch".

The app will show this error if the checksum stored in the patch does not match the checksum of the ROM. This means that the ROM is not compatible with the patch. You need to choose a different ROM. Usually there are several ROMs for each game (such as the version for Europe, USA, Japan, good or bad dumps, etc.).

ROM hackers often publish checksum of the accompanying ROM file (on a web page or in README file). Compare that to the one you have. Long tap the file in the file manager and you will see these 3 lines: CRC32, SHA1 and MD5. If one of those numbers are the same, you have the ROM the patch was written for. If not, you need a different ROM.

#### I can not find the correct ROM for the game "Pokémon Emerald".

Most of the patches of the game work with ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### I apply the IPS patch and then the game is not working / contains graphic glitches. What am I doing wrong?

IPS format patches do not contain a checksum. Therefore, the patch will apply to any (even wrong) ROM. In this case, you need to look for another ROM.

#### What can I do with .ECM file type?

ECM is a data compression format designed specifically for disc images. You can decompress the file using [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver) program.

#### The app shows the error: "Could not copy file".

The error occurs on some devices with Android 4.4. Possible solutions:

- Copy the ROM file on the memory card in the folder **Android/data/org.emunix.unipatcher/**. Then you need to select the ROM from this directory.
- Install [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix) application (requires ROOT access).

#### The app shows the error: "File have the wrong checksum after it was patched".

Maybe it's a bug in my program. Please contact me at [e-mail](mailto:mashin87@gmail.com) and attach the patch to the letter.

#### Has UniPatcher some additional features?

Yes. UniPatcher can:

- Create XDelta3 patches.
- Fix checksum for a Sega Mega Drive / Sega Genesis games.
- Add or remove SMC header for Super Nintendo games.

#### Why do I have to fix checksum for the Sega Mega Drive games?

Sega Mega Drive (Genesis) games are protected from modification. If the checksum of the game differs from the one the ROM amounts to, the game displays a red screen and stops running. What this does is calculate the correct checksum and write it to the ROM.

**Warning:** This function does not create a backup ROM.

#### Why it's needed sometimes to add or remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing or adding a header is sometimes used for the purpose of correctly applying a patch.

**Warning:** This function does not create a backup ROM.

#### How to translate the app?

If you would like to translate the app into another language or improve an existing translation, you can do it on [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/) site.

#### I have a question, a feature request or a bug report.

Contact me at e-mail <mashin87@gmail.com>. Please write in English or Russian. If you have problems with patching, attach the patch to the letter and write the name of your ROM, it will save our time.