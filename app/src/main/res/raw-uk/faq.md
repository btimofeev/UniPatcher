## Питання які задаються найчастіше:

#### Що таке UniPatcher?

Це додаток для Android який призначений для накладання патчів на ROM файли усіляких ігрових відеоконсолей.

#### Які формати патчів підтримуються?

Додаток підтримує патчі у форматах IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP та XDelta3.

#### Чи можливо із допомогою цього додатку зломати гру для Android?

Ні. Додаток не призначено для злому Android ігор.

#### Що таке ROM файл?

ROM файл це комп'ютерний файл який містить в собі копію даних із чіпа пам'яті ігрового картриджу. Цей файл може бути запущено з допомогою програми емулятора. Таким чином можна грати у консольні ігри на комп'ютері чи смартфоні.

#### Що таке ROM хакінг?

ROM хакінг це процес модифікації ROM файла з метою змінити графіку, рівні, геймплей або мову гри.

#### Що таке патч?

Патч це файл який містить різницю між оригінальною та зміненою версіями ROM'у.

Ромхакери розповсюджують патч, а користувачі застосовують цей патч до оригінальної версії ROM'у, тим самим отримуючи модифіковану версію.

#### Чому ромхакери не розповсюджують пропатчені ROM файли?

Хаки та переклади розповсюджуються у вигляді патчів для того, щоб зменшити розмір завантажуючих даних і не порушувати авторських прав на гру.

#### Як накласти патч на гру?

Необхідно вибрати ROM файл та Патч, а тоді натиснути на круглу червону кнопку

У результаті ви отримаєте пропатчений ROM, який буде знаходитись в одній папці із оригінальним ROM'ом.

#### Коли я вибираю файл додаток пише "Архів необхідно розпакувати у зовнішній програмі".

Файл який ви вибрали являється архівом. Архів містить у собі папки та файли у стисненому вигляді.

Currently UniPatcher can not extract archives, so you need to unpack your archive in a different program. I recommend a gratis program [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Додаток вибиває помилку: "Цей ROM не сувмісний із патчем".

The app will show this error if the checksum stored in the patch does not match the checksum of the ROM. This means that the ROM file is not compatible with the patch. You need to choose a different ROM file. Usually there are several ROMs for each game (such as the version for Europe, USA, Japan, good or bad dumps, etc.).

Ромхакери часто публікують контрольну суму необхідного ROM файла (на веб сторінці або у файлі Readme). Вам необхідно порівняти її із вашим ROM'ом. Для цього у вікні вибору файла зробіть довгий тап на ROM файлі і ви побачите 3 лінії: CRC32, SHA1 та MD5. Якщо одна із ліній співпадає із контрольною сумою яку вказав ромхакер, тоді ваш ROM підходить. Якщо ж вона не співпадає, то вам потрібно пошукати інший ROM.

In the worst case, if you can not find the correct ROM file, you can set the option "Ignore the checksum" in the settings. But bear in mind that in this case the game may contain bugs or be completely unplayable.

#### Не можу підібрати ROM для гри "Pokémon Emerald".

Більшість патчів цієї гри працюють із ROM'ом "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Я застосовую патч у форматі IPS до гри а після цього гра не працює/містить графічні артефакти. Що я роблю не правильно?

IPS format patches do not contain a checksum. Therefore, the patch will apply to any (even wrong) ROM file. In this case, you need to look for another ROM file.

#### Що можна зробити із файлом у форматі .ECM?

ECM це формат стискання даних створений спеціально для образів дисків. Програма [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver) уміє розпаковувати такий формат.

#### Додаток вибиває помилку "Не вдалося скопіювати файл".

The error occurs on some devices with Android 4.4+ having an external SD card. Android does not allow applications to write data to a SD card on these devices (a detailed description of the problem [here](http://www.androidpolice.com/2014/02/17/external-blues-google-has-brought-big-changes-to-sd-cards-in-kitkat-and-even-samsung-may-be-implementing-them/)).

There are several ways to solve this problem:

- Do not apply patches to the ROM file located on the external SD card. Just move the ROM file into the internal memory of the device.
- Specify the path to any directory in the internal memory of the device as the output directory (in the settings).
- Specify the path to **Android/data/org.emunix.unipatcher/** directory on the external SD card as the output directory (in the settings).
- Встановити програму [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix) (необхідний ROOT).

#### Додаток вибиває помилку "Контрольна сума пропатченого файлу неправильна".

Можливий баг у моїй програмі. Будь-ласка, напишіть мені на [e-mail](mailto:unipatcher@gmail.com) та прикріпіть патч до листа.

#### В UniPatcher'і є хоч якісь додаткові функції?

Так. Додаток може:

- створювати патчі у форматі XDelta3.
- Виправляти контрольну суму для ігор Sega Mega Drive.
- Додавати або видаляти SMC назви для ігор Super Nintendo.

#### Навіщо виправляти контрольну суму для ігор Sega Mega Drive?

Sega Mega Drive (Genesis) games have their checksum written into the ROM. If you only change any part of the game, they will not match, failing to run as a result. What this does is calculate the correct checksum of the change and write it to the modified ROM-file."

**Попередження:** ця функція не створює резервної копії ROM'у.

#### Навіщо додавати або видаляти SMC заголовки в іграх Super Nintendo?

SMC заголовок це 512 байт які знаходяться на початку деяких SNES ROM'ів. Ці байти не мають значення, але вони міняють розташування даних. Видаляння або додавання заголовку іноді використовується із ціллю правильного накладання патчів.

**Попередження:** ця функція не створює резервної копії ROM'у.

#### Як перекласти додаток на іншу мову?

Якщо ви хочете перекласти додаток на іншу мову або покращити існуючий переклад, ви можете зробити це на сайті [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/).

#### У мене є питання, ідея для нової функції або повідомлення про помилку.

Ви можете зв'язатися зі мною по електронній пошті, моя адреса <unipatcher@gmail.com>. Прошу писати англійською або російською. Якщо у вас проблема із накладанням патчу, то прикріпіть патч до листа та напишіть назву вашого ROM'у, це збереже наш час.