## Często Zadawane Pytania

#### Co to jest UniPatcher

UniPatcher jest narzędziem do łatkowania ROM-ów różnych konsol

#### Jakie formaty łatek są obsługiwane?

Aplikacja Wspiera łatki: IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP i XDelta3.

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

#### Po zaaplikowaniu łatki IPS gra nie działa/zawiera glitche graficzne. Co robię źle?

Łatka typu IPS nie zawiera sumy kontrolnej. To znaczy że łatka zostanie zaaplikowana na jakikolwiek (nawet nieodpowiedni) ROM. W tym przypadku musisz poszukać innego ROM-a.

#### Co mogę zrobić z typem pliku .ECM?

ECM jest formatem skompresowanego pliku specyficznego dla image-ów z dysku. Możesz go zdekompresować używając [ZArchiver] (http://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Aplikacja wyświetla błąd: "Nie można skopiować pliku".

Ten problem występuje na niektórych urządzeniach z androidem 4.4+. Możliwe rozwiązania:

- Skopiuj ROM na wewnętrzną kartę pamięci **mnt/storage/emulated/0/**. Wtedy wybierz ROM z tej ścieżki.
- Zainstaluj [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix) aplikację [Wymaga dostępu do ROOT (Jeżeli nie wiesz co to ROOT, albo nie wiesz ja zrootować swoję urządzenie sprawdź https://www.xda-developers.com)].

#### Aplikacja wyświetla błąd: "Plik ma złą sumę kontrolną po złatkowaniu".

Może jest to bug w moim programie. Proszę skontaktuj się ze mną przez [e-mail] (mailto:mashin87@gmail.com) i załącz łatkę w liście.

#### Czy UniPatcher ma dodatkowe funkcje?

Tak. UniPatcher może:

- naprawiać sumę kontrolną w grach z konsol Sega Mega Drive/Sega Genesis
- Dodawać bądź usuwać nagłówek SMC dla gier z konsoli Super Nintendo Entertaiment System (SNES).

#### Po co mam naprawiać sumę kontrolną w grach z Sega Mega Drive/Genesis?

Sega Mega Drive/Genesis ma zabezpieczenia przed modyfikacją gry. ROM przechowywuje wartość sumy kontrolnej i jeżeli ona różni się od aktualnej sumy kontrolnej gra wyświetla czerwony ekran śmierci oraz przestaje działać. Ta funkcja poprawia sumę kontrolną w ROM-ie.

**Uwaga:** Ta funkcja nie tworzy kopii zapasowe ROM-u.

#### Dlaczego jest to czasami wymagane aby dodać albo usunąć nagłówek SMC z gier Super Nintendo?

Nagłówek SMC jest pierwszymi 512 bitami znajdowanymi na początku niektórych SNES-owych ROM image-ów. Te bity nie mają celu, ale one zmieniają lokalizację danych. Usuwanie bądź dodawanie nagłówków jest używane w celach poprawnego zaaplikowania łatki.

**Uwaga:** Ta funkcja nie tworzy kopii zapasowe ROM-u.

#### Jak przetłumaczyć aplikację?

Jeżeli chciałbyś/chciałabyś przetłumaczyć aplikację na inny język, albo poprawić istniejące tłumaczenie, możesz to zrobić na stronie [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/).

#### Mam pytanie, prośba o dodanie funkcji albo raport w sprawie błędu

Skontaktuj się ze mną przez e-mail <mashin87@gmail.com>. Proszę pisz po Angielsku albo Rosyjsku. Jeżeli masz jakieś problemy z łatkowaniem, załącz łatkę do mail-a i napisz nazwę ROM-u, to oszczędzi nasz czas.