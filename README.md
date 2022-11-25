# shell

This repository is for shell interoperability

It contains main class: ShellExecutor, which simplifies execution of commands on different shells:

* Linux bash
* Macos bash
* Cygwin bash
* MSys bash
* Windows cmd

Information about currently used shell is carried by OsType.

Repository contains also OsPath, which is universal implementation of path on different shells.
OsPath can also do simple conversions between different types of OsPath e.g. it can convert from Cygwin path to Windows
path.
