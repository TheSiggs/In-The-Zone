# High Level Requirements

## Introduction

This file outlines high level requirements as interpreted by Tim Heuer.

### Game Play

We want a PVP game where player 1 and player 2 are sitting in front of separate computers. The computers need to be able to run Java and are assumed to be desktop
based with mouse and meyboard as control devices.
There are exactly 2 teams involved in the game controlled by exactly 2 distinct players. The 2 distinct players can each be a group of people acting as a team,
but essentially there are only 2 control device sets (keyboards and mice).
There are 4 characters per team who are controlled on a AxB grid board. A and B can vary, but should at least be 10x10 while the grid board can have allevated tiles that connect or are
separated.

Each character has a number of maximum health points as well as a current HP. When all the HP on one team reaches zero, the game ends with the other team being the victor.

Each character has items and abilities and those should balance between the two teams. There can be items like throwable bombs, cross bows etc. Players may choose items, weapons and abilities within certain parameters before a game.
Any ability or item has an area of effect in which it is active and a range (how far this ability can be cast or thrown). This is usually constrained into certain shapes specific to abilities and items.
The abilities and items can also affect health, positively or negatively. Instant effects often accompany abilities, allowing characters to do various things, such as reposition characters or cause status effects, buffs or debuffs (which affect character stats) after performing an ability . 

The game is turn based. So, each team gets a turn with X amount of actions they can perform, depending on there action and movement points. A character can move one square for the cost of movement point. Abilities have different Action Point costs that must be paid to perform. Movement Points and Action Points are reset at the beginning of every turn.
advances character X by one square horizontally or vertically when the board is
at right angles.
The squares can have textures, harm and traversability difficulty which makes it more difficult to travel through them.

For more information see here: https://docs.google.com/document/d/1TwKB23MKHXl1MSlc2pHCd8AWjAhhyrVbkOwW7hRR8SE/edit

TODO: complete/edit
