# vim-scopes
Vim syntax plugin for the Scopes language

This program generates a vim plugin by querying the compiler about known symbols and then adding some keywords and rules manually. A pre generated version is included for simplicity, but after a language update introducing only symbols getting the correct highlighting is just a matter of rerunning the generator.

To rebuild the plugin:
in the repo directory run `scopes ./vim-scopes.sc > syntax/scopes.vim`.

If there's some mismatch between what is highlighted and legal code, feel free to file an issue. However, be aware that mid-line string blocks will *not* be added, as they're more trouble to match than they're worth.

Screenshots
-----------
indentation error highlighting
![](https://cdn.discordapp.com/attachments/251533063253786625/583904690815631376/unknown.png)

dark theme
![](https://media.discordapp.net/attachments/329404808643608586/583896996973248512/unknown.png?width=1248&height=676)

light theme
![](https://cdn.discordapp.com/attachments/329404808643608586/583897072051290112/unknown.png)
