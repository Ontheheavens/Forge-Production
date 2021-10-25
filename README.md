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

23.10.2021

All remaining files of source mod were removed, work started from scratch.

* * *

Architecture draft:

 - Hullmod dummy (no code, as it does nothing) 
       	 [**Will probably only display info through tooltips**]
        
 - Ability (when toggled will do something)  
      	  [**Will display info through tooltip and act as a toggle for conversion**]
        
 - EveryFrameListener for deciding if ability is usable (check fleet status, and set ability property correctly, this property is used to determine if it is              activable)
       	 [**Done for now**]
        
 - Further listeners that do actual conversion (TBD later). 
      	  [**Will also create a unified production message**]
        
25.10.2021

    What do we need?

    - commodity conversion

    - way to toggle on/off for each conversion type

    - requirement for Heavy Machinery availability

    - way to control production scale

    - therefore, granularity for ship amount, ship size and combat readiness (forging capacities must be bestowed by ships)

    - restrictions for ship's forging capacities (can't do everything at once)

    - each ship engaged in production gets combat readiness malus daily

    - Salvage Gantry throughput bonuses

    - Nanoforges efficiency bonuses

    - tooltip information for each ship's forging capacity (in hullmod)

    - tooltip information for fleet's forging capacity (in ability)

    - single production report for daily forging activity (by ability OR listener)

    - SFX for daily production report

    OPTIONAL:

    - allow forging hullmods to be installable on destroyer

    - restrict forging hullmods to civilian-grade hulls

    - create a whitelist of specific hulls to be eligible for forge hullmods installation

    - add individual hullmod capacity/efficiency/throughput bonuses when installed on specific hulls

    - add built-in hullmod bonuses

    Architecture:

    1. Ability class

    - contains logic for conversion

    Multiple abilities: individual toggles for each type.

    - needs a listener to declare unified production report

    - introduces much bloat

    Single ability: toggle for all production, individual types are disabled through ship repairs. [GOING WITH THIS ONE]

    - can contain logic for unified production report

    2. Usability listener class

    - restricts ability usage based on hullmod presence in fleet.
    (need to have this information in ability tooltip)

    3. Hullmod classes

    Main hullmod for module installation restrictions and general ship stats modification.

    Multiple module hullmods for each conversion type. Every conversion needs some Heavy Machinery available.

    - Refinery module: refines Metal from Ore and Transplutonics from Transplutonic Ore. (Maybe split this one into two?)

    - Centrifuge Module : centrifuges Fuel from Volatiles. Adds a little bit of fuel capacity.

    - Manufacture Module: manufactures Supplies from Metal and Transplutonics.

    - Machineworking Module: manufactures Heavy Machinery from Metal and Transplutonics.

