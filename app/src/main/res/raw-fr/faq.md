## Foire Aux Questions

#### UniPatcher, c'est quoi ?

UniPatcher est une application pour Android qui vous permet d'appliquer des patchs aux ROMs de nombreux jeux vidéo pour consoles.

#### Quels types de patchs sont pris en charge ?

L'appli prend en charge les patchs IPS, IPS32, UPS, BPS, APS (GBA), APS (N64), PPF, DPS, EBP et XDelta3.

#### Est-ce que je peux hacker ou cracker des jeux Android avec cette appli ?

Non. UniPatcher n'est pas conçu pour hacker des jeux Android.

#### Qu'est-ce qu'une ROM ?

Une ROM est un fichier contenant une copie d'une cartouche de jeu vidéo. À travers le processus de l'émulation, vous pouvez recopier ce fichier, le lancer dans un logiciel appelé "émulateur", et apprécier le jeu sur votre ordinateur ou votre smartphone.

#### Qu'est-ce que le "ROM Hacking ?"

Le "ROM Hacking" est une technique qui modifie les données contenues dans la ROM. Cela peut se traduire par des graphismes ou des niveaux différents, une modification du niveau de difficulté, ou encore une traduction dans une langue pour laquelle un jeu n'était pas disponible à l'origine.

#### Qu'est-ce qu'un patch ?

Un patch est un fichier contenant les changements entre la version originale d'une ROM et la version modifiée.

Ce patch est librement partagé, et les joueurs l'appliquent à une copie d'une ROM originale, ce qui produit ainsi une version jouable du hack.

#### Pourquoi les hackers de ROM ne partagent pas des jeux modifiés ?

Les modifications et les traductions sont généralement distribuées sous forme de patchs afin de réduire la taille du téléchargement et d'éviter tout problème lié aux droits d'auteur.

#### Comment appliquer un patch sur une ROM ?

You have to select a ROM file, a patch and choose which file to save, then click on the red round button. Files are selected through the standard Files application (or through one of the file managers you have installed). The application will show a message when the file is patched. Do not close the application until the file is patched.

#### L'appli affiche ce message après avoir sélectionné un fichier : "L'archive devrait d'abord être extraite à l'aide d'une autre appli."

Le fichier que vous avez sélectionné est une archive. Une archive contient vos dossiers et fichiers dans un format compressé.

Actuellement UniPatcher ne peut pas extraire les archives, vous devez alors extraire votre archive à l'aide d'une autre appli. Je recommande pour cela l'appli gratuite [ZArchiver] (https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### L'appli affiche ce message : "Cette ROM n'est pas compatible avec le patch".

L'appli affichera cette erreur si le checksum du patch ne correspond pas au checksum de la ROM. Cela veut dire que la ROM n'est pas compatible avec le patch. Vous devez alors choisir une autre ROM. En règle générale, il y a plusieurs ROMs de chaque jeu (par exemple des versions Europe, USA, Japon, etc.). 

Les hackers de ROMs publient souvent le checksum de la ROM qui va avec (sur une page Web ou sur un fichier README). Comparez-le avec celui que vous avez. Appuyez longuement sur le fichier dans le gestionnaire de fichiers et vous verrez 3 lignes : CRC32, SHA1 et MD5. Si une de ces lignes est la même, alors votre ROM correspond au patch. Sinon, vous avez besoin d'une ROM différente.

Dans le pire des cas si vous ne trouvez pas la ROM correcte, vous pouvez choisir l'option "Ignorer le checksum" dans les paramètres. Mais gardez à l'esprit que dans ce cas, le jeu pourra contenir des bugs ou être totalement injouable.

#### I can't patch "Super Mario World (U) [!].smc"

This ROM contains an SMC header, while most patches for this game require the ROM to not have this header. You can remove SMC header by selecting the appropriate item in the menu on the left and then apply the patch to the resulting ROM.

#### Je ne parviens pas à trouver la bonne ROM pour le jeu "Pokémon Emeraude".

La majorité des patchs de ce jeu fonctionne avec la ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba"

#### J'ai appliqué le patch IPS, mais le jeu ne fonctionne plus ou a beaucoup de bugs graphiques. Qu'est-ce que j'ai fait de mal ?

Les patchs au format IPS ne contiennent pas de checksum. De ce fait, le patch s'appliquera à n'importe quelle ROM, même si elle est incompatible. Dans ce cas, vous devez rechercher une autre ROM.

#### Que faire avec un fichier au format .ECM ?

Le format ECM est un format de compression de données conçu exclusivement pour les images disque. Vous pouvez décompresser le fichier en utilisant l'appli [ZArchiver] (https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver).

#### Est-ce qu'UniPatcher a des fonctionnalités supplémentaires ?

Bien sûr. UniPatcher peut :

- Créer des patchs XDelta3.
- Réparer les checksums des jeux pour Sega Mega Drive & Sega Genesis.
- Remove SMC header in Super Nintendo games.

#### Pourquoi je dois réparer le checksum des jeux Sega Mega Drive ?

Les checksums des jeux Sega Mega Drive (Genesis) sont directement insérés dans la ROM. Si vous changez une quelconque donnée du jeu, ils ne correspondront plus, et par conséquent feront crash le jeu. Cet utilitaire permet de recalculer correctement le checksum lié à la modification, et le réécrira sur le fichier ROM modifié.

**Attention :** Cette fonctionnalité ne fait pas de backups de la ROM.

#### Why is it sometimes necessary to remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing a header is sometimes used for the purpose of correctly applying a patch.

#### Comment traduire cette appli ?

Si vous souhaitez traduire cette appli dans une autre langue ou améliorer une traduction existante, n'hésitez pas à visiter le site [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/).

#### J'ai une question, une proposition d'amélioration ou un rapport de bug à soumettre.

Contactez-moi par mail <unipatcher@gmail.com>. Merci de m'écrire en Anglais ou en Russe. Si vous avez des problèmes pour patcher, attachez votre patch en pièce jointe et écrivez le nom de votre ROM, ce sera toujours un gain de temps.