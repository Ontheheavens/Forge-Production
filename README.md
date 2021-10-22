# Forge Production

This is an attempt to learn basic principles and skills of Java programming by modding Starsector.
Source of the files is "Nijigen Extend", Starsector mod by Creature:

https://fractalsoftworks.com/forum/index.php?topic=17846.0

Files and features deemed unnecessary for the reworked idea were removed. Here is original content of source:

- Utility weapons - Items that are mounted on weapon slots but don't fire - instead giving unique bonues
- Forge Ships - Hullmods that allow the production of goods from the comfort of your own ship.
- Criminal Industries - Colony buildings that bring in money directly, but can negatively impact your colony in other ways.
- Agreus-Caparice Tech Cooperative - Adds a market in Agreus (Vanilla) and Caparice (YRXP), A&C Co-op, where you can buy the gadgets offered by this mod.

Removed was everything except Forge Ship Hullmods. Furthermore, hullmod classes were partially modified, an ability class and mod plugin file were added.

Currently project includes:

- "Forge Ship" base hullmod, that allows installation of specialized hullmods.
- 4 specialized hullmods, containing logic for commodity production.
- 2 "technical" hullmods, one completely unnecessary, left from scrapped parts of a mod, and other supposedly responsible for hullmod installation conflicts.
- new ability class, created by me with a goal of collecting notification messages and toggling forge production.
- mod plugin file, created for technical need of enabling campaign ability.

Current makeup of a mod produces certain shortcomings: for example, each hullmod installed on ship produces its own notification message, 
which can seriously clutter intel in late stages of the game.

Such is a state of the project at the time of the Git creation.

Development goals:

- Collect production notifications into one intel tooltip.
- Create a toggle method for all production in ability class.
- Develop Heavy Machinery breakdown mechanic with usage of Nanoforges.
- and other minor tweaks.

This is not intended to be a potentially big project with undefined development cycle. 
The main goal here is to create a streamlined, refined replacement of such mods as "Supply Forging", "Fuel Siphoning", and forges of "Nijigen Extend", 
with an implementation keeping to vanilla style, lore and feeling.
