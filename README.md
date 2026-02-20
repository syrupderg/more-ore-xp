<a href="https://modrinth.com/mod/more-ore-xp"><img alt="modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg"></a> 
<img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg"> 

# What is this mod?

More Ore XP lets you configure how much experience is dropped when you mine a any ore block.

By default, the only new changes are made to:
- Copper Ore -> min: 3 max: 7
- Deepslate Copper Ore -> min: 3 max: 7
- Iron Ore -> min: 3 max: 7
- Deepslate Iron Ore -> min: 3 max: 7
- Gold Ore -> min: 3 max: 7
- Deepslate Gold Ore -> min: 3 max: 7

![](https://cdn.modrinth.com/data/BjtheZTY/images/2889e7daf0ada6f50c908a7588a74cd456d069cf.png)

# Ore experience values:

| Minecraft Ore | Changed Min | Changed Max | Vanilla Min | Vanilla Max |
| --- | :---: | :---: | :---: | :---: |
| Ancient Debris | 7 | 17 | 0 | 0 |
| Coal  | 0 | 2 | 0 | 2 |
| Deepslate Coal | 0 | 2 | 0 | 2 |
| Copper | 3 | 7 | 0 | 0 |
| Deepslate Copper | 3 | 7 | 0 | 0 |
| Diamond | 3 | 7 | 3 | 7 |
| Deepslate Diamond | 3 | 7 | 3 | 7 |
| Emerald | 3 | 7 | 3 | 7 |
| Deepslate Emerald | 3 | 7 | 3 | 7 |
| Gold | 3 | 7 | 0 | 0 |
| Deepslate Gold | 3 | 7 | 0 | 0 |
| Iron | 3 | 7 | 0 | 0 |
| Deepslate Iron | 3 | 7 | 0 | 0 |
| Lapis | 2 | 5 | 2 | 5 |
| Deepslate Lapis | 2 | 5 | 2 | 5 |
| Nether Gold | 0 | 1 | 0 | 1 |
| Nether Quartz | 2 | 5 | 2 | 5 |
| Redstone | 1 | 5 | 1 | 5 |
| Deepslate Redstone | 1 | 5 | 1 | 5 |

# Client side:
Drag and drop in your `mods` folder. <br>
Only required for singleplayer. <br>
You can join servers that doesn't have this mod installed.

To manually change the values, edit `moreorexp.json` in `config` folder.

## Client side requirements:
- Fabric API (must be installed)
- Cloth Config API
  > Optional, if you want to configure the mod with mod menu.
- Mod Menu
  > Optional, if you want to only see details about this mod. <br>
  > Without Cloth Config API you can't edit this mod from Mod Menu.

# Server side:
Drag and drop in your `mods` folder. <br>
Players don't need to install this mod in order to join the server.

To change the values, edit `moreorexp.json` in `config` folder.

## Server side requirements:
- Fabric API (must be installed)

# Removing the mod:
Just remove the `more-ore-xp-<version>.jar` file from `mods` folder and optionally `moreorexp.json` from `config` folder. <br>
No extra steps are needed.

# Known issues:
Currently no major issues are found. If you find an error or if you want a feature to be added please ask it in the [Issues](https://github.com/syrupderg/more-ore-xp/issues) page.
