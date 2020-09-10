## 常见问答:

#### UniPatcher 是什么？

UniPatcher 是 Android 上一个为不同电子游戏主机的 ROM 映像应用补丁的工具。

#### 支持哪些补丁格式？

本应用支持 IPS、IPS32、UPS、BPS、APS (GBA)、APS (N64)、PPF、DPS、EBP 和 XDelta3 补丁。

#### 我可以通过这个应用修改或破解 Android 游戏吗？

不，UniPatcher 不是为修改 Android 游戏而设计的。

#### ROM 映像是什么？

ROM 映像是一个电脑文件，它包含了电子游戏卡带的拷贝。通过一个模拟进程来将文件拷贝出来，用“模拟器”软件运行，就可以在电脑或手机上游玩了。

#### ROM 修改是什么？

ROM 修改即修改 ROM 映像的数据，它可以是更改调整图像、关卡、难度，甚至将一个游戏翻译成其他语言。

#### 补丁是什么？

补丁是一个包含了原始 ROM 与修改过的 ROM 不同部分的文件。

补丁发布后，终端用户应用补丁到原始 ROM 的拷贝，就可以玩修改版了。

#### 为什么 ROM 修改者们不发布修改版游戏？

修改和翻译用补丁形式发布是为了减少下载数据量及避免版权问题。

#### 如何应用补丁到 ROM？

你必须选择 ROM 文件和补丁文件，然后触摸红色圆按钮。

在选择文件最后，你会得到一个集成了补丁的 ROM，它位于原始 ROM 的目录中。

#### 在选择文件后应用会显示一句消息：“压缩包应该由外部程序解压”。

你选择的文件是一个压缩包，它包含的目录和文件是压缩过的格式。

目前 UniPatcher 不能解开压缩档案，所以你需要用其他程序来解包。我推荐免费程序 [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver)

#### 应用会告知错误：“此 ROM 与补丁不兼容”。

如果补丁内的校验和与 ROM 内的校验和不符，应用会显示此错误，这意味着 ROM 文件与补丁不兼容。一般一个游戏有几个版本的 ROM (如欧、美、日版，好或坏的转储，等)。

ROM 修改者经常随 ROM 文件同时公开校验和 (在网页或 README 文件中)。将其与你的那一份比较，在文件管理器长按文件，你会看到以下三行：CRC32、SHA1和MD5。只要其中之一是相同的，你拥有的就是写入了这个补丁的 ROM，如果不相同，那你就需要换 ROM 了。

最坏的情况，如果你不能找到正确的 ROM 文件，那你可以在设置里开启选项“无视校验和”，但请记住在这种情况下游戏可能会含有 bug，甚至根本不能玩。

#### 我不能为游戏“口袋妖怪 翡翠”找到正确的 ROM。

Most of the patches of the game work with ROM "Pokemon - Emerald Version (U) \[f1\] (Save Type).gba".

#### 我应用了 IPS 补丁，然后游戏不运行了 / 包含图像错误，是我什么地方搞错了吗？

IPS 格式的补丁不含校验和，因此，它可以应用到任何 ROM 文件，哪怕是不匹配的。碰到这种情况，你需要其他的 ROM 文件。

#### 我该怎么处理 .ECM 文件类型？

ECM 是一种为光盘特制的数据压缩格式，你可以用 [ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver) 解压它。

#### 应用告知错误：“不能复制文件”。

该错误在某些安装了 Android 4.4 以上且有外部 SD 卡的设备上出现。对这些设备，Android 不允许应用将数据写入到 SD 卡中（对于此问题的详细说明 [在此](http://www.androidpolice.com/2014/02/17/external-blues-google-has-brought-big-changes-to-sd-cards-in-kitkat-and-even-samsung-may-be-implementing-them/)）

解决此问题的办法有：

- 不要对外部 SD 卡中的 ROM 文件应用补丁，将 ROM 文件移动到设备的内存中。
- 在设置中指定输出目录为设备内存的任一目录。
- 在设置中指定路径到外部 SD 卡目录 **Android/data/org.emunix.unipatcher/** 作为输出目录。
- 安装 [SDFix](https://play.google.com/store/apps/details?id=nextapp.sdfix) 应用（需要 ROOT 权限）。

#### 应用告知错误：“集成补丁后的文件校验和错误”。

可能是程序中的 bug，请联系我 [e-mail](mailto:unipatcher@gmail.com) 并在邮件里附上补丁。

#### UniPatcher 有什么附加功能吗？

是的，UniPatcher 还可以：

- 创建 XDelta3 补丁。
- 为世嘉 Mega Drive / Genesis 游戏修正校验和。
- Remove SMC header in Super Nintendo games.

#### 为什么我必须修正世嘉 Mega Drive 游戏的校验和？

世嘉 Mega Drive (Genesis) 游戏在 ROM 中有自己的校验和。如果你只更改游戏的任何部分, 它们将不匹配, 因此无法运行。它所做的是计算更改的正确校验和并将其写入修改后的 ROM 文件。

**警告:** 此功能不会创建备份 ROM。

#### Why is it sometimes necessary to remove SMC headers from Super Nintendo games?

An SMC header is 512 bytes found at the start of some SNES ROM images. These bytes have no purpose, but they change the location of the remaining data. Removing a header is sometimes used for the purpose of correctly applying a patch.

#### 怎么翻译此应用？

如果你想将本应用翻译为其他语言或想改进现有翻译，请在 [Transifex](https://www.transifex.com/unipatcher/unipatcher/dashboard/) 操作。

#### 我有问题，有功能请求或有 bug 汇报。

通过邮件 <unipatcher@gmail.com> 联系我，请用英语或俄语写信。如果你对打补丁有问题，把补丁用附件发送给我并注明你的 ROM 的名字，这样我和你都能省时省力。