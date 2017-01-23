## Często Zadawane Pytania

#### Co to jest UniPatcher

UniPatcher jest narzędziem do łatkowania ROM-ów różnych konsol

#### Jakie formaty łatek są obsługiwane?

The app supports IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP and XDelta3 patches.

#### Czy mogę hakować albo crackować gry Android za pomocą tej aplikacji?

Nie. UniPatcher nie został stworzony do hakowania gier Androida

#### Co to jest ROM image (czyt. Imidż)?

ROM (Ang.Read-Only-Memory) image jest plikiem komputerowym który zawiera kopię danych z tylko-do-odczytu chipu pamięci kartridża gry komputerowej. Określenie jest używane w kontekście emulacji, gdzie starsze gry są skopiowane do pliku ROM i za pomocą emulatorów (oprogramowania do emulacji) mogą być puszczone na komputerze, urządzenie przenośnym albo na Android TV.

#### Co to jest ROM haking?

ROM haking jet modyfikowaniem danych w ROM image. To może być zmana kolorów, zmienianie poziomów gry czy nawet tłumaczenie na język w którym gra nie była oryginalnie dostępna.

#### Co to jest łatka?

Łatka jest plikiem który zawiera różnicę między oryginalną wersją ROM-u, a shakowaną wersją.

Łatka jest wypuszczona i finalni użytkownicy aplikują łatkę do oryginalego ROM-u, który produkuję grywalną wersję haku.

#### Dlaczego romhakerzy nie wypuszczają zmodyfikowanej gry?

Haki i tłumaczenia są generalnie wypuszczanie jako łatki aby zredukować wielkość pliku i aby ominąć problemy z prawami autorskimi

#### Jak zaaplikować łatkę do ROM-u?

To bardzo proste: musisz wybrać plik ROM, łatkę i nacisnąć okrągły czerwony przycisk.

Jako wynik, dostajesz złatkowany ROM, który będzie zlokalizowany w tej samej ścieżce co oryginalny ROM

#### Aplikacja wyświetla wiadomość do wybraniu pliku: "Archiwa powinny być wypakowane w zewnętrznym programie"

Plik który wybrałeś jest archiwum. Archiwa zawierają ścieżki i pliki w skompresowanym formacie

Narazie UniPatcher nie może wypakowywać archiw, więc musisz wypakować je w zewnętrznym programie. Rekomenduje darmowy program [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Aplikacja wyświetla błąd: "Ten ROM nie jest kompatybilny z łatką".

Aplikacja będzie wyświetlać ten błąd jeżeli suma kontrolna przechowywana w łatce nie jest taka sama jak suma kontrolna ROM-u. To znaczy że ROM nie jest kompatybilny z łatką. Potrzebujesz wybrać inny ROM . Często jest kilka wersji ROM-u dla danej gry (Np. mamy wersję Europejską, Amerykańską, Japońską, lepszy dump lub gorszy itp.).

Romhakerzy często publikują sumę kontrolną pliku ROM (na stronie internetowej albo w pliku Readme.txt razem z łatką). Potrzebujesz ją porównać z twoim ROM-em. Dłużej przytrzymaj na pliku w menedżerze plików i wtedy zobaczysz trzy linijki: CRC2, SHA1 i MD5. Masz poprawny ROM jeżeli jedna z tych linijek równa się z sumą kontrolną podaną przez Romhakera. Jeżeli sumy kontrolne się nie równają poszukaj innego ROM-u.

#### I can not find the correct ROM for the game "Pokemon Emerald".

Większość łatek do gry "Pokémon - Emerald Version" działa z ROM-em "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### I apply the IPS patch and then the game is not working / contains graphic glitches. What am I doing wrong?

IPS format patches do not contain a checksum. Therefore, the patch will apply to any (even wrong) ROM. In this case, you need to look for another ROM.

#### Co mogę zrobić z typem pliku .ECM?

ECM is a data compression format designed specifically for disc images. You can decompress the file using [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver) program.

#### The app shows the error: "Could not copy file".

The error occurs on some devices with Android 4.4. Possible solutions:

- Copy the ROM file on the memory card in the folder **Android/data/org.emunix.unipatcher/**. Then you need to select the ROM from this directory.
- Install [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix) application (requires ROOT access).

#### The app shows the error: "File have the wrong checksum after it was patched".

Maybe it's a bug in my program. Please contact me at [e-mail](mailto:mashin87@gmail.com) and attach the patch to the letter.

#### Czy UniPatcher ma dodatkowe funkcje?

Tak. UniPatcher może:

- fix checksum for a Sega Mega Drive / Sega Genesis games.
- add or remove SMC header for a Super Nintendo games.

#### Why do I have to fix checksum for the Sega Mega Drive games?

Sega Mega Drive (Genesis) has protection from game modification. ROM stores the checksum value, and if it differs from the actual checksum the game displays a red screen and stops running. This function writes the correct checksum in the ROM.

**Warning:** This function does not create a backup ROM.

#### Why it's needed sometimes to add or remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing or adding a header is sometimes used for the purpose of correctly applying a patch.

**Warning:** This function does not create a backup ROM.

#### How to translate the app?

If you would like to translate the app into another language or improve an existing translation, you can do it on [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/) site.

#### Mam pytanie, prośba o dodanie funkcji albo raport w sprawie błędu

Contact me at e-mail <mashin87@gmail.com>. Please write in English or Russian. If you have problems with patching, attach the patch to the letter and write the name of your ROM, it will save our time.
