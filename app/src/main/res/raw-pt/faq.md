## Perguntas mais frequentes:

#### O que é o UniPatcher?

UniPatcher é uma ferramenta Android para aplicar patches em imagens ROM de vários consoles de vídeo-game.

#### Quais são os formatos de patch suportados?

O aplicativo suporta IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP e patches XDelta3.

#### Posso hackear ou crackear jogos Android usando este aplicativo?

Não. O UniPatcher não é desenvolvido para hackear jogos Android.

#### O que é imagem ROM?

Uma imagem ROM é um arquivo computacional que contém uma cópia de um jogo para console. Através do processo de emulação, você pode copiar este arquivo e rodá-lo usando uma aplicação chamada "emulador", para aproveitar o jogo pelo seu dispositivo, seja um computador, um tablet ou smartphone.

#### O que é hackeamento de ROM?

Hackeamento de ROM é a modificação de dados em uma imagem ROM. Isto pode tomar a forma de alterar gráficos, mudar níveis do jogo, manipular fatores de dificuldade, ou mesmo a tradução para um idioma para o qual um jogo não foi originalmente feito disponível.

#### O que é um patch?

Um patch é um arquivo que contém as diferenças entre a versão original de uma ROM e a versão hackeada.

O patch é distribuído, e os usuários finais aplicam o patch em uma cópia da ROM original, cuja produz uma versão jogável da hack.

#### Por quê ROM hackers não distribúem jogos modificados?

Hacks e traduções são geralmente distribuídos como patches para reduzir o tamanho de download e evitar problemas com direitos autorais.

#### Como aplicar um patch em uma ROM?

You have to select a ROM file, a patch and choose which file to save, then click on the red round button. Files are selected through the standard Files application (or through one of the file managers you have installed). The application will show a message when the file is patched. Do not close the application until the file is patched.

#### O aplicativo mostra uma mensagem após selecionar um arquivo: " O arquivo deve ser decomprimido em um aplicativo externo".

O arquivo que você selecionou é um ficheiro. O ficheiro contém os diretórios e arquivos em um formato comprimido (Ex. .zip, .rar).

Até este momento, o UniPatcher não pode extrair arquivos, então você precisará descompactar seu arquivo em uma aplicação diferente. Eu recomendo esta aqui, é gratuita: [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### O aplicativo mostra o erro: "Esta ROM não é compatível com o patch".

The app will show this error if the checksum stored in the patch does not match the checksum of the ROM. This means that the ROM file is not compatible with the patch. You need to choose a different ROM file. Usually there are several ROMs for each game (such as the version for Europe, USA, Japan, good or bad dumps, etc.).

Os ROM hacker quase sempre publicam o checksum do arquivo ROM (em uma página da web ou um arquivo README/LEIAME). Compare ele com o que você tem. Aperte e segure em um arquivo pelo gerenciador de arquivos e você verá estas 3 linhas: CRC32, SHA1 e MD5. Se qualquer um deste números "baterem", você tem a ROM cujo o patch foi direcionado. Se não, você precisará de outra ROM.

In the worst case, if you can not find the correct ROM file, you can set the option "Ignore the checksum" in the settings. But bear in mind that in this case the game may contain bugs or be completely unplayable.

#### I can't patch "Super Mario World (U) [!].smc"

This ROM contains an SMC header, while most patches for this game require the ROM to not have this header. You can remove SMC header by selecting the appropriate item in the menu on the left and then apply the patch to the resulting ROM.

#### Não consigo achar a ROM correta para o jogo "Pokémon Emerald".

A maioria dos patches do jogo funciona com a ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### Eu aplico o patch IPS e o jogo não está funcionando/ tem glitches gráficos. O quê estou fazendo de errado?

IPS format patches do not contain a checksum. Therefore, the patch will apply to any (even wrong) ROM file. In this case, you need to look for another ROM file.

#### O que eu posso fazer com arquivos do tipo ECM?

ECM é um formato de compressão de dados designado especificamente para imagens de disco. Você pode descomprimir o arquivo usando ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver) program.

#### O UniPatcher tem alguma função adicional?

Sim. UniPatcher pode:

- Criar patches do tipo XDelta3
- Corrigir checksum em jogos de Sega Mega Drive/ / Sega Genesis.
- Remove SMC header in Super Nintendo games.

#### Por que tenho que corrigir checksum para jogos de Sega Mega Drive?

Os jogos para Sega Mega Drive (Genesis) possuem o checksum escritas dentro da ROM. Se você modificar qualquer parte do jogo, eles não vão coincidir, ocasionando uma falha ao tentar executar. O que esta opção faz é calcular o checksum correto da mudança realizada e escrever o mesmo dentro da ROM modificada.

**AVISO:** Esta função não cria uma ROM de backup.

#### Why is it sometimes necessary to remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing a header is sometimes used for the purpose of correctly applying a patch.

#### Como traduzir o app?

Se você gostaria de traduzir o app para outro idioma ou melhorar uma tradução existente, você pode fazer isso no site [Transifex] https://www.transifex.com/unipatcher/unipatcher/dashboard/)

#### Tenho uma pergunta, um requerimento de função ou uma denúncia de bug.

Me contate pelo e-mail <unipatcher@gmail.com>. Por favor escreva em Inglês ou Russo se você tem problemas com patcheamento, anexe o patch no e-mail e escreva o nome da sua ROM, isso poupará nosso tempo.