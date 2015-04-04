- Issue #39 j3o files generated on build.
Adding j3o files to dist.

- Issue #17 Deleted deprecated classes and package.

- Issue #38 Adding rotation in CSV.

- Merge branch 'master' of bitbucket.org:inthezone/in-the-zone

- Fix issues with end turn
Clicking the end turn button while a character was selected, or while
highlighting was in effect, put the user interface into an inconsistent
state.

- graphics: Health bars stay visible for a while after an attack.

- data_model: improve character death API
Add a new class to keep parameters together.

- data_model: kill characters simultaneously
When a single attack kills multiple characters, kill them all
simultaneously.

- Fix #31
This was quite tricky.  It seems JME puts all user animations into a
global cache, so the die animation from the last goblin was replacing
the die animation from the second one.  This problem will go away when
we integrate the real art.

- Fix #33

- Removed some comments.

- Suppressing settings on convert.

- Merge branch 'master' of https://bitbucket.org/geekdenz/in-the-zone
Conflicts:
	TerrainBattle/assets/Models/zan/zan_texturing.j3o
	TerrainBattle/scene.csv
	TerrainBattle/src/main/java/nz/dcoder/inthezone/Blend2J3o.java

- Changed assets.

- Batch converting of models in scene.csv.
You can now batch convert blend models
from .blend files into .j3o files by
running
./gradlew convert

- Merge branch 'master' of bitbucket.org:inthezone/in-the-zone

- Merge branch 'master' of https://bitbucket.org/inthezone/in-the-zone

- Merged in jedcalkin/in-the-zone (pull request #3)
issue #34

Adds invisible obstacles surrounding the battle zone.  It's a hack, but it'll do for now.

- Merge branch 'master' of https://bitbucket.org/inthezone/in-the-zone

- fixed issue #34

- Issue #29.  Fix the real problem and add comments.

- Merged in jedcalkin/in-the-zone (pull request #2)
fixed issue #29 push will not crash the game

This patch doesn't quite get to the heart of the problem, so I'll make a small correction and push it upstream.
- fixed issue #29 push will not crash the game

- Added JavaExec task to run Blend2J3o.
Doesn't yet work!

- Changes from laptop.
Not sure if it's stable.

- Issue #30 - Simple converter complete.
Still need to build it into the build
system.

- Merge branch 'master' of bitbucket.org:inthezone/in-the-zone

- Issue #30 - Basic conversion working.
This works! But more work needs to be
done. We need to refactor and add it
to the build system so that .blend
files can be added automatically.

- Issue #30 - Converting blend2j3o.
Simplest possible convert done.

- Merged in geekdenz/in-the-zone (pull request #1)
Moved models in scene.csv.
- Moved models in scene.csv.

- gui: center the message box

- control: send messages to GUI, fixes #27

- control: implement mana zone messaging, fixes #28

- I have added enterLeaveManaZone method to be called from the control package.

- Added AP to Use Items menu

- Removed Messages, Added showMessage function.

- Merge branch 'working-gui'
Conflicts:
	TerrainBattle/src/main/java/nz/dcoder/inthezone/jfx/MainHUDController.java

- data_model: improvement to area of affect code
Use a set to represent the area of affect as it is being assembled.
This prevents bugs where the same square appears twice in the area of
affect, leading to the same character getting targeted twice.

- data_model: bug in piercing attacks
Piercing attacks do not target the character performing the attack.

- control: enable mana zones
The way I've done it is a bit of a hack.  It would be better to add
another notification to the GUI but I'll hold off on that until I merge
back into master.

- graphics: make mana zones visible
Hack the old board.map so that 2 represents a mana zone.

- data_model: add support for mana zones

- graphics: add fade out to health bars

- graphics: improve health bars
Make them look nicer.  Make them appear on mouse over.

- control: health bars appear when characters are targeted
health bar rendering still needs some tuning

- control: show health bar on targeted characters
Also fix up the appearance and operation of the health bars.

- graphics: add code for health bars

- graphics: add indicator for selected character
Needs texturing, lighting, or some such thing, but works OK for now.

- highlighting: add range highlighting
Also alpha blend the highlighting so it's easier to see the scale of the
board.

- Added some text prompt. Have not finished yet.

- data_model: players get separate sets of items
The AI player cannot use the player's items and vice versa.

- refactor: merge the input package into control
This puts all the control layer classes in the same package.

- items: implement items
Also fix numerous bugs in the data layer

- items: create mechanism to report items in the gui

- refactor: major refactoring
Create new control package.  Now we have a clean separation between
model (data_model), view (graphics and jfx), and controller (control and
input)

- graphics: refactor ControllerChain
This makes the ControllerChain mechanism easier to use, harder to
misuse, and documents its shortcomings.

- Working on text prompt.

- graphics: basic highlighting

- abilities: test teleport and heal
Fix a minor bug in teleport

- gui: permanent component to menus

- graphics: make setAnimation compatible with controllerchain

- presentation: fix various UI glitches

- input: improve targeting
You can click a character to target, rather than a square to target.

- data_model: pushing costs MP, not AP
Should it also cost some AP?  This would require expanding the data
model for abilities

- pushing: debug corpse pushing
The animation is a little "glitchy".  That's a consequence of the goblin
model lacking a "die" animation, which is something we will require from
the actual character models.

- graphics: refactor CharacterWalkController
Turn it into the more general PathController which works for waking,
running, and sliding objects.

- data_model: Enable pushing

- gui: fix death animation

- graphics: simple death animation

- Merge branch 'master' into working-gui

- Mac DMG creation; task distDmg
Note that this works only on a Mac.

- data_model: fix bugs surrounding attacks and death

- gui: bug in menu management

- gui: Tooltip to describe attacks in more detail

- gui: Notice for empty menus

- gui: spaces to tabs
Also fix attack and magic menus to match the selected character.

- gui: implement main menu
Also disable right mouse button since we don't need it anymore.  Also
fix bug to make AI player human playable.

- gui: track AP, MP and HP

- gui: Tie GUI together with the rest of the game

- Merge branch 'master' of bitbucket.org:inthezone/in-the-zone into working-gui

- Distribution for Mac.

- gui: Adjust menus and fix css

- presentation: mechanism for abilities
Implemented a very general mechanism that allows for repeating
abilities, and complex abilities such as "transport" that involve
multiple stages of input from the player.

- presentation: give the goblins weapons

- graphics: queue up all animations properly
Animations must be queued up on the ContollerChain to ensure that they
play back in sequence rather than all at the same time.

- data_model: bug which prevented full use of MP
Discovered a bug which prevented the player for using his last movement
point.

- graphics: create chainable controllers
This allows use to queue up a sequence of animations so each starts
after the previous one finishes.

- Merge branch 'master' of bitbucket.org:inthezone/in-the-zone

- Issue #18 - Windows package.
Windows package now works. It
results in zans-story.zip in
build/distributions.

- ai: create stub for AI player

- presentation: make AI turn human controllable

- data_model: notify presentation layer on AI turn

- ui: integrate new CharacterInfo class

- data_model: Create new CharacterInfo class
Also move AbilityInfo to the pure package where it belongs

- data: create separate entries for each goblin

- Issue #18 - Windows and Mac config (packaging).
However, cannot test yet. Need to try in Windows
and compile the game there to see if this works.

- Working on issue #18.
Pretty much working for Linux. Just need to zip up
the files now. Budled JRE for Linux!

- Resolves issue #10 and #15. Started issue #18.
Blender models can be added by writing their relative
paths to a CSV file which has to be given on the command
line. By default it opens scene.csv.
Gradle tasks have been added to package the game.
TODO: Windows and MacOS.

- input: Add mechanism for an 'end-turn' button in the GUI

- Main: refactor to avoid NullPointerExceptions
The previous design was prone to NullPointerExceptions because the
dependencies between the various components were implicit.  I have made
them explicit which forces them to be created in the correct order to
avoid exceptions.

- ui: create stubs for overloaded mouse buttons
This will soon allow us to implement the move and action buttons in the
GUI.

- ui: stubs for calling into the GUI

- Loadable CSV file loading models dynamically.
When loading a scene.csv file with filename,
x, y, z coordinates one can define where a
model is located.

- user_interface: streamline JavaFX launching
There was a race condition in the code that launched JavaFX.

- presentation: create stubs for all callbacks
Also implement most of the death logic.

- graphics: fix walking animations again
Walking animations fixed and now characters jump out of the way properly

- graphics: use a custom Control for movement
Movement is once again a bit broken, I'll fix it shortly

- Adding Zan and house model to blender task.
Early prototype stage with sample house
and Zan loaded from .blend files to show
artists how their models render.

- data_model: characters can walk through characters
But only characters on the same team

- graphics: fix movement animations
I was using the wrong animation, and some of the headings were wrong

- data_model: clean up pathfinding
Also fixes a few bugs

- graphics: reimplement movement
Note that it now uses the right mouse button

- input: implement select character with mouse

- data_model: integrate improved a*
OldMain is now broken beyond repair, since it depended on the old
game-ai package.  No matter, the new Main will soon be fully
operational.

- main: remove default AppState
This gets rid of the frame rate counter

- input: rotate view

- Cleanup.

- graphics: set facing

- graphics: rotate characters upright

- graphics: set up animations

- presentation: Port basic initialisation over from OldMain

- Made old version of RunnableWithController deprecated

- data_files: add goblin character

- Yet another gradle file.

- Merge branch 'master' of bitbucket.org:inthezone/in-the-zone
Conflicts:
	TerrainBattle/.gitignore
	TerrainBattle/.gradle/2.1/taskArtifacts/cache.properties.lock
	TerrainBattle/gradle/.gradle/2.1/taskArtifacts/fileHashes.bin
	TerrainBattle/gradle/.gradle/2.1/taskArtifacts/fileSnapshots.bin
	TerrainBattle/src/main/java/nz/dcoder/inthezone/BlenderRender.java

- Rendering clock model.
From this it is apparent that jme doesn't
support some 3D features of blender.

- Another gradle commit.

- Redundant imports.

- Needed these files to work.

- more git ignores

- Cleanup of gradle daemon artifacts.
Blender rendering working again.

- data_model: add hpAdjust effect
For abilities that do a fixed amount of damage

- Merge branch 'master' of bitbucket.org:inthezone/in-the-zone

- merge resolution

- Still working on the menu. Ran into a display bug when trying to switch between Magic Menu and Attack Menu.

- data_model: close data files properly
Using the all new try-with-resources statement !!!

- data_model: minor bugs with equipment
Fixed computation of effective base stats to properly incorporate weapon
buffs.  Also added toString method to equipment for debugging.

- data_model: integrate formulas

- data_model: test formulas

- data_model: formalise ability classes
We now have two ability classes, physical and magical

- fixed null pointer exception :)
I wanted to run the code to make sure I hadn't broken the build with my
formula code, then I discovered that little NullPointerException and
couldn't resist fixing it.

- data_model: implement formulas system
Also refactor UnicodeInputReader into its own package

- merge of work done during hangout on 2014.2.16 21:00

- spaces to tabs and a blue box behind the model

- Blender rendering woman model.

- Gradle Wrapper.

- Merge branch 'master' of geekdenz.bitbucket.org:inthezone/in-the-zone
Conflicts:
	TerrainBattle/src/main/java/nz/dcoder/inthezone/OldMain.java
	TerrainBattle/src/main/java/nz/dcoder/inthezone/deprecated/Armour.java
	TerrainBattle/src/main/java/nz/dcoder/inthezone/deprecated/Attack.java
	TerrainBattle/src/main/java/nz/dcoder/inthezone/deprecated/CharacterState.java
	TerrainBattle/src/main/java/nz/dcoder/inthezone/deprecated/Skill.java
	TerrainBattle/src/main/java/nz/dcoder/inthezone/deprecated/Weapon.java
	TerrainBattle/src/main/java/nz/dcoder/inthezone/deprecated/attack_example.java
	TerrainBattle/src/main/java/nz/dcoder/inthezone/graphics/CharacterGraphics.java

- Refactoring to separate concerns.
@callum_lowcay helped with design
decisions.

- Merge branch 'master' of https://bitbucket.org/inthezone/in-the-zone

- atempted to fill callback methods by reference

- data_model: implemented simulated dual wielding

- data_model: change ability amount parameter to s

- data_model: restrict piercing to cardinal directions
Also changed the name of the canPassObstacles parameter to the less
confusing "requriesLOS".

- data_model: split weapon damage into physical and magical

- removed objects packege, it is now unused

- Added stub classes for overall mechanics.

- New gradle wrapper.

- Refactored Character -> CharacterGraphics.

- Merge branch 'master' of geekdenz.bitbucket.org:inthezone/in-the-zone
Conflicts:
	TerrainBattle/.gradle/2.1/taskArtifacts/cache.properties.lock
	TerrainBattle/.gradle/2.1/taskArtifacts/fileHashes.bin
	TerrainBattle/.gradle/2.1/taskArtifacts/fileSnapshots.bin
	TerrainBattle/.gradle/2.1/taskArtifacts/taskArtifacts.bin
	TerrainBattle/src/main/java/nz/dcoder/inthezone/jfx/MainHUDController.java

- Issue #9 - Started refactoring concerns.
Keep old class as reference. Delete once
all functionality is restored.

- Started on GUI mockup. Changed status bar color, shape and text, added placement 'player selected' screen and character menus(WIP)

- Fixed issue #8 - Removed redundant AI code.

- Added blender test class.
So that artists can eventually view their models
inside jme to see how they would look in game.
However, this is not complete, I couldn't see
anything yet, even though there were no errors.

- data_model: character death and end conditions

- data_model: finish the BattleController class

- data_model: object triggering
Implement the method that detects when an object such as a mine should
be triggered.

- data_model: multi-targeting
Add support for properly informing the presentation layer when an
ability has multiple targets

- data_model: turn number checking and object triggering

- data_model: implement push effect

- data_model: implement character 'death'

- data_model: rewrite line-of-sight
The original LOS algorithm required each segment to be the same length
which doesn't work very well in general, so I replaced it with
Bresenham's line algorithm, modified to return a manhatten path.

- data_model: update all abilities with new targeting

- data_model: fully implement targeting
Also improves the damage formula a bit

- data_model: Add 'hasAOEShading' parameter
resolves issue #6

- data_model: add a method to compute the manhatten circle
A manhatten circle is of course a diamond

- data_model:  implement line of sight algorithm
Turns out it's a bit tricky.

- data_model: clean up Ability subclasses

- data_model: tidy up LevelController

- data_model: implement most of Character.java
Added a new method to CanDoAbility to get the position of the agent
doing the ability

- data_model: Some methods to manage items

- data_model: Items do not do abilities, characters do abilities

- data_model: add 'repeats' property for abilities
resolves issue #5

- data_model: finish TurnCharacter

- data_model: fix failing test case

- data_model: implement levelController.addExp()

- Merge branch 'master' of https://bitbucket.org/inthezone/in-the-zone
Merge of work by jedc and Callum

- data_model: implement teleport/heal/damageAbility

- data_model: implement battle system
Implemented basic battle mechanics and the data layer half of the motion
system.

- data_model: implement equals and hashCode for Position

- data_model: fix BattleObject as per meeting on 8/2/15
The "isObstacle" property was agreed to be too restrictive, because some
objects are valid as intermediary locations, but not as the destination
for a character. e.g., corpses.  A character cannot occupy the same
square as a corpse, but may pass over that square to reach a safe
destination.

- testing:  Test cases for factories
I also set up gradle to build and run the test cases

- factories: implement CharacterFactory
Database reading code is now complete, but it will need testing.
Perhaps we can use JUnit tests?

- factories: implement LevelControllerFactory
also created a new exception for malformed databases

- Merge branch 'master' of bitbucket.org:inthezone/in-the-zone

- removed extra dependency

- Merge branch 'master' of https://bitbucket.org/inthezone/in-the-zone
Conflicts:
	TerrainBattle/src/main/java/nz/dcoder/inthezone/objects/CharacterState.java

- Fixing Errors, additional error have came up.

- data_model: oversight in LevelController
Forgot to add the array that maps exp to levels

- factories: implement ItemFactory
Also made a change to the DatabaseName class so it automatically trims
off leading and trailing whitespace

- factories: implement EquipmentFactory
Minor addition made to EquipmentClass

- factories: implement BattleObjectFactory
This required some minor changes to the data model layer again.

- update gradle wrapper

- factories: implement AbilityFactory
I have added the apache commons CSV parser to our dependencies.

- data_model: breaking changes to Ability subclasses
Changed the way the constructors work for the Ability subclasses to
better suit AbilityFactory.  Sorry.

- data_model: added factory classes
Had to change the Ability class to public.

- data_model: create skeleton
I have created the classes we discussed on Sunday 1st February.  I
suggest that a good place to start filling in the classes would be the
DamageAbility class, where the damage formula lives, and
LevelController, where the levelling formula goes.

- Oops, did gradle wrapper in wrong directory.

- Added gradle wrapper.
This is useful so all developers
are forced to use the exact same
version of gradle.
So instead of writing
gradle build
for example, you would write
./gradlew build
on *nix and BSD (also Mac), or

gradle.bat build
or
gradle build
on Windows.

- update to inthezone/objects to comply with java convetion

- Reverted to using jar dependencies.
That way we don't depend on the jME
repository to be up.

- More exceptions fixed.
Characters removed from teams on death.

- Fixed minor NullPointerException.

- Attacking with right mouse button shows health.
Simple attacking now works.
Spatials (characters) are removed from the scene
when they "die".

- Added notifications

- Added Health Bar (WIP). Removed "loading_screen.fxml". Moved Tims button.

- JavaFX Controller can call SimpleApplication methods.

- Basic JavaFX progress bar as health bar.

- Transparent window with JavaFX XML GUI.
The GUI can now be transparent and show
the JME App beneath.

- JavaFX working with JME!
Basic JavaFX seems to be working now.

- Gradle Wrapper added to source control.

- Building with JDK 8 for better JavaFX.
Please contact me if you have trouble
getting it to work. I tested with
jMonkeyEngine by changing the JDK
to Java 8.

- Fixed errors.

- Merge branch 'CharacterHP' into multiple-players-enemies

- Partial cleanup of objects classes.

- 2 teams of 5 characters each, facing each other.

- Characters selectable and movable.

- Goblin animation.

- PointLight instead of DirectionalLight.

- New Character class.

- these classes create character objects, the attack method can be chucked in with the other game logic, when we have some other game logic.

- Corrected game-ai problem with swapped x/y.

- Fixed facing and disappear bug.
Now the model doesn't disappear when
it goes over half the board.
3D model imports should work now.

- 3D model working with correct facing.
However, it disappears when the board is turned
or when it goes over the middle of the board,
it seems. Not sure why this happens.

- Run gradle in daemon mode by default.
This makes running gradle faster.

- Added random blender model and displayed it.
Successfully imported blender model and displayed
it. However, the model doesn't appear 100% right.
This may be an issue later but should be fine for
now.

- Running scene task with assets.

- Changed speed on animation of character.

- Animating path with shortest path.

- Better version of game-ai.

- Preventing index out of bounds exception.

- Better AI for AStarSearch module.

- Without AI for master to have a running game.

- game-ai as library starting AStarSearch within game.

- Trying to get assets working with Java properties.

- Tim shape on board.

- BoardState read from txt file and initialised board.

- Loading board state from file.

- some resources added which cannot be used yet

- Deleted n-puzzle because unused.

- Merge branch 'ai' of bitbucket.org:geekdenz/in-the-zone into ai

- Finally got A* working for a small example.
Total re-implementation of A* according to
gaming book.

- Merge branch 'ai' of bitbucket.org:geekdenz/in-the-zone into ai
Conflicts:
	TerrainBattle/src/main/java/nz/dcoder/inthezone/HighlightedRoute.java

- home ai stuff, still unstable

- still not working astar but seems close

- full blown library for some other AI algorithms

- new puzzle lib
unused imports deleted

- some AI experiments with A*
But not working yet. :(

- build works in Linux

- removed some files
which make the build not work in Linux

- distribution working
Now that the distribution process is
working, deployment becomes a lot easier!

- started on distribution build
but it still doesn't run when unzipping
the contents to a folder

- gradle support

- fixed project to build in macos

- movement animation working with picking and arrows

- basic rotation of map

- View 45 degrees from top.

- initial import
