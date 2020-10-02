## Często Zadawane Pytania

#### Co to jest UniPatcher

UniPatcher jest narzędziem do łatkowania ROM-ów różnych konsol

#### Jakie formaty łatek są obsługiwane?

Aplikacja Wspiera łatki: IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP i XDelta3.

#### Czy mogę hakować albo crackować gry Android za pomocą tej aplikacji?

Nie. UniPatcher nie został stworzony do hakowania gier Androida

#### Co to jest ROM image (czyt. Imidż)?

A ROM image is a computer file containing a copy of video game cartridge. Through the process of emulation, you copy that file out, run it in a piece of software called an "emulator", to enjoy the game on your computer or phone.

#### Co to jest ROM haking?

ROM haking jet modyfikowaniem danych w ROM image. To może być zmana kolorów, zmienianie poziomów gry czy nawet tłumaczenie na język w którym gra nie była oryginalnie dostępna.

#### Co to jest łatka?

Łatka jest plikiem który zawiera różnicę między oryginalną wersją ROM-u, a shakowaną wersją.

Łatka jest wypuszczona i finalni użytkownicy aplikują łatkę do oryginalego ROM-u, który produkuję grywalną wersję haku.

#### Dlaczego ROM hakerzy nie wypuszczają zmodyfikowanej gry?

Haki i tłumaczenia są generalnie wypuszczanie jako łatki aby zredukować wielkość pliku i aby ominąć problemy z prawami autorskimi

#### Jak zaaplikować łatkę do ROM-u?

You have to select a ROM file, a patch and choose which file to save, then click on the red round button. Files are selected through the standard Files application (or through one of the file managers you have installed). The application will show a message when the file is patched. Do not close the application until the file is patched.

#### Aplikacja wyświetla wiadomość do wybraniu pliku: "Archiwa powinny być wypakowane w zewnętrznym programie"

Plik który wybrałeś jest archiwum. Archiwa zawierają ścieżki i pliki w skompresowanym formacie

Currently UniPatcher can not extract archives, so you need to unpack your archive in a different program. I recommend a gratis program [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Aplikacja wyświetla błąd: "Ten ROM nie jest kompatybilny z łatką".

The app will show this error if the checksum stored in the patch does not match the checksum of the ROM. This means that the ROM file is not compatible with the patch. You need to choose a different ROM file. Usually there are several ROMs for each game (such as the version for Europe, USA, Japan, good or bad dumps, etc.).

ROM hackers often publish checksum of the accompanying ROM file (on a web page or in README file). Compare that to the one you have. Long tap the file in the file manager and you will see these 3 lines: CRC32, SHA1 and MD5. If one of those numbers are the same, you have the ROM the patch was written for. If not, you need a different ROM.

In the worst case, if you can not find the correct ROM file, you can set the option "Ignore the checksum" in the settings. But bear in mind that in this case the game may contain bugs or be completely unplayable.

#### I can't patch "Super Mario World (U) [!].smc"

This ROM contains an SMC header, while most patches for this game require the ROM to not have this header. You can remove SMC header by selecting the appropriate item in the menu on the left and then apply the patch to the resulting ROM.

#### I can not find the correct ROM for the game "Pokémon Emerald".

Większość łatek do gry "Pokémon - Emerald Version" działa z ROM-em "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Po zaaplikowaniu łatki IPS gra nie działa/zawiera glitche graficzne. Co robię źle?

IPS format patches do not contain a checksum. Therefore, the patch will apply to any (even wrong) ROM file. In this case, you need to look for another ROM file.

#### Co mogę zrobić z typem pliku .ECM?

ECM jest formatem skompresowanego pliku specyficznego dla image-ów z dysku. Możesz go zdekompresować używając [ZArchiver] (http://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Czy UniPatcher ma dodatkowe funkcje?

Tak. UniPatcher może:

- Create XDelta3 patches.
- Naprawiać sumę kontrolną w grach z konsol Sega Mega Drive/Sega Genesis
- Remove SMC header in Super Nintendo games.

#### Po co mam naprawiać sumę kontrolną w grach z Sega Mega Drive/Genesis?

Sega Mega Drive (Genesis) games have their checksum written into the ROM. If you only change any part of the game, they will not match, failing to run as a result. What this does is calculate the correct checksum of the change and write it to the modified ROM-file."

**Uwaga:** Ta funkcja nie tworzy kopii zapasowe ROM-u.

#### Why is it sometimes necessary to remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing a header is sometimes used for the purpose of correctly applying a patch.

#### Jak przetłumaczyć aplikację?

Jeżeli chciałbyś/chciałabyś przetłumaczyć aplikację na inny język, albo poprawić istniejące tłumaczenie, możesz to zrobić na stronie [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/).

#### Mam pytanie, prośba o dodanie funkcji albo raport w sprawie błędu

Skontaktuj się ze mną przez e-mail <unipatcher@gmail.com>. Proszę pisz po Angielsku albo Rosyjsku. Jeżeli masz jakieś problemy z łatkowaniem, załącz łatkę do mail-a i napisz nazwę ROM-u, to oszczędzi nasz czas.