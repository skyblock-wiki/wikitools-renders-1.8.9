# Manual Tests (Render Entity)

## Environment

- Minecraft 1.8.9
- Minecraft Forge 1.8.9 - 11.15.1.2318
- Mods: WikiTools Renders 2.6.7

For rendering features to work properly, please use a large enough window size (preferably full screen on a modern monitor) during the tests.

## Tests

Tester should set the following fields before or after performing tests. Tester should time their test session, which should include the time to launch Minecraft with the test environment.

- Tester: Not set
- Test date: Not set
- Time taken: Not set

Tester should perform each test and write the test result in the `Result` field. The test result should be "OK" if the `Action` is accurate and the output exactly matches `Expected Output`. Otherwise, write down the problem.

### Render Entity GUI

#### Render Entity GUI can be opened by pressing K

- Action: Go to a SkyBlock lobby. Press K.
- Expected Output: The Render Entity GUI opens.
- Result: Not set

### Copy Entity

#### Fake players can be copied by pressing M

- Action: Go to a SkyBlock lobby. Head to 2 70 -74. Press M to copy the Security Sloth NPC.
- Expected Output: Press K to open the GUI. The NPC is rendered within the display box on the left.
- Result: Not set

#### Mobs can be copied by pressing M, big mobs are scaled

- Action: Go to a creative mode world and spawn a big mob (e.g. ghast). Aim at the mob and press M. The "Copied entity" message should appear.
- Expected Output: Press K to open the GUI. The ghast is rendered within the display box on the left and does not overflow the box or block other parts of the screen.
- Result: Not set

### Entity Copy Options

#### Can copy self

- Action: Go to a creative mode world. Press K to open the GUI. Click the Copy Self button.
- Expected Output: Your player character is rendered within the display box on the left.
- Result: Not set

#### When current entity is a player with equipment, set to Steve inherits the equipment

- Action: Go to a creative mode world. Wear some armor and hold an item. Press K to open the GUI and click the Copy Self button. Then, click the Set to Steve icon.
- Expected Output: Steve is rendered within the display box on the left with your armor pieces and held item.
- Result: Not set

#### Can set to Steve when current entity is a mob

- Action: Go to a creative mode world. Press M on any mob to copy it. Press K to open the GUI. Click the Set to Steve icon.
- Expected Output: Steve is rendered within the display box on the left.
  - Steve may be rendered with your armor pieces and held item. This is expected behaviour.
- Result: Not set

### Add Item To Entity

#### An item can be added as entity's held item by pressing M

- Action: Go to a creative mode world and hold no item. Press K to open the GUI and click the Copy Self button. Go to the inventory. Hover on any item (e.g. snowball) and press M. The "Added item to entity" message should appear.
- Expected Output: Press K to open the GUI. The player is holding the item selected.
- Result: Not set

#### A block item can be added as entity's helmet by pressing Shift-M

- Action: Go to a creative mode world and wear no armor. Press K to open the GUI and click the Copy Self button. Go to the inventory. Hover on any block (e.g. snow block) and press Shift-M. The "Added item to entity" message should appear.
- Expected Output: Press K to open the GUI. The player is wearing the block selected as helmet.
- Result: Not set

#### Armor pieces can be added to entity's armor slots by pressing Shift-M

- Action: Go to a creative mode world and wear no armor. Press K to open the GUI and click the Copy Self button. Go to the inventory. Get armor of different variants (e.g. leather helmet, iron chestplate, gold leggings, and diamond boots) For each piece of armor, hover over it and press Shift-M. The "Added item to entity" message should appear.
- Expected Output: Press K to open the GUI. The player is wearing the same armor pieces you selected.
- Result: Not set

### Rendering Options

#### Can save entity image of non-player entity

- Action: Go to a creative mode world. Press M on any mob to copy it. Press K to open the GUI. Click the Save Entity Image icon.
- Expected Output: Exit the GUI. Press T to open chat messages. Click on the screenshot name.
  - The save location should be opened. 
  - The last image saved should be an isometric render of the mob. 
  - Check the image size. The longer side of the image should be 512px (±1px).
- Result: Not set

#### Can save entity image of player entity with equipment

- Action: Go to a creative mode world. Wear some armor and hold an item. Press K to open the GUI and click the Copy Self button. Click the Save Entity Image icon.
- Expected Output: Exit the GUI. Press T to open chat messages. Click on the screenshot name.
  - The save location should be opened.
  - The last image saved should be an isometric render of the player with their armor and held item.
  - Check the image size. The longer side of the image should be 512px (±1px).
- Result: Not set

#### Can download skin of player entity

- Action: Go to a SkyBlock lobby. Head to 2 70 -74. Press M to copy the Security Sloth NPC. Click the Download Skin icon.
- Expected Output: Exit the GUI. Press T to open chat messages. Click on the screenshot name.
  - The save location should be opened.
  - The last image saved should be a skin file of the NPC.
  - The image size should be 64x64px.
- Result: Not set

#### Can download head of player entity

- Action: Go to a SkyBlock lobby. Head to 2 70 -74. Press M to copy the Security Sloth NPC. Click the Download Head icon.
- Expected Output: Exit the GUI. Press T to open chat messages. Click on the screenshot name.
  - The save location should be opened.
  - The last image saved should be a head sprite of the player. The hat overlay, in this case the sunglasses of the NPC, should appear on top of the head layer.
  - The image size should be 72x72px.
- Result: Not set

### Entity Modifiers

#### Can toggle small arms of player entity

- Action: Go to a creative mode world. Press K to open the GUI and click the Copy Self button. Try to use the Toggle Small Arms button.
- Expected Output: The player entity toggles between small arm and large arm models.
- Result: Not set

#### Can toggle invisible of non-player entity

- Action: Go to a creative mode world. Press M on any mob to copy it. Press K to open the GUI. Try to use the Toggle Invisible button.
- Expected Output: The entity toggles between visible and invisible.
- Result: Not set

#### Can toggle invisible of player entity with equipment remaining visible

- Action: Go to a creative mode world. Wear some armor pieces and hold an item. Press K to open the GUI and click the Copy Self button. Try to use the Toggle Invisible button.
- Expected Output: The player entity toggles between visible and invisible. On the invisible state, the player's armor pieces and held item remain visible.
- Result: Not set

#### Can remove enchants of player entity

- Action: Go to a creative mode world. Wear some enchanted armor pieces and hold an enchanted item^. Press K to open the GUI and click the Copy Self button. Click the remove enchants button.
  - ^ One may enchant the held item using the command `/enchant <player> <enchantment> <level>`, where enchantment is protection for armor and sharpness for sword, and level is 1.
- Expected Output: The armor pieces and held item of the player entity are no longer enchanted.
- Result: Not set

#### Can remove armor of player entity

- Action: Go to a creative mode world. Wear some armor pieces. Press K to open the GUI and click the Copy Self button. Click the remove armor button.
- Expected Output: The armor pieces of the player entity are removed.
- Result: Not set

#### Can remove item of player entity

- Action: Go to a creative mode world. Hold any item. Press K to open the GUI and click the Copy Self button. Click the remove armor button.
- Expected Output: The held item of the player entity is removed.
- Result: Not set

#### Can set head pitch of player entity

- Action: Go to a creative mode world. Press K to open the GUI and click the Copy Self button. Move back and forth the head pitch slider.
- Expected Output: The player entity's head goes rotates in the axis one nods with their head.
- Result: Not set

#### Can set head yaw of player entity

- Action: Go to a creative mode world. Press K to open the GUI and click the Copy Self button. Move back and forth the head yaw slider.
- Expected Output: The player entity's head goes rotates in the axis one shakes their head.
- Result: Not set

#### Head pitch and yaw resets when copying new entity

- Action: Go to a creative mode world. Press K to open the GUI and click the Copy Self button. Change the head pitch and yaw to non-zero values. Click the Copy Self button again.
- Expected Output: The head pitch and yaw slider is set to zero. In the display box, the player entity's head resets to the default position.
- Result: Not set

### Any Other Tests (Optional)

Testers can test the mod with whatever conditions they want and write any problem they found below this line.

### Comments and Suggestions (Optional)

Testers can provide their comments and suggestions about the mod and/or the manual tests below this line. Testers can report any ambiguity in the test document or suggest for new tests here.
