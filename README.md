# FarLandsAgain
## About
A plugin to restore the Far Lands on Spigot servers. The starting coordinates can be configured, including making the entire world Far Lands.

## Usage
The official page, downloads and instructions can be found here: [FarLandsAgain](https://www.spigotmc.org/resources/farlandsagain.8228/).

## Notes
This can only add Far Lands to the vanilla generator and is not intended to support any custom generators. In order to get Far Lands in a custom generator the author of that generator must add the option.

Due to the necessity of modifying the NMS of the vanilla generator directly, any Spigot or Paper branches that alter the generation code may break the plugin. Support is only guaranteed in the base Spigot or Paper.

## Compiling
To compile the plugin yourself you will need:
 - Maven
 - The CraftBukkit jars for each NMS revision of Minecraft 1.12 and 1.14
 - The Spigot jars for each NMS revision of Minecraft 1.16 - 1.18
 - The Paper jars for each NMS revision of Minecraft 1.12 and 1.14 - 1.18
 
Any necessary dependencies are specified in the pom.xml for each module. Spigot/CraftBukkit APIs will be downloaded automatically when building the module but the CraftBukkit, Spigot and Paper jars will need to be compiled and added to your local Maven repo.
 
Once dependencies are in place the plugin can be built by doing `mvn install` on the parent project. The plugin will be found in the `FarLandsAgain/target/` directory.