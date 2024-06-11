# JavaTilemapEditor
This is an eclipse project for a java tilemap-editor.


# Features / Controls

Direct Interactions

Paint - Left click, paints currently selected tile. Inserts the tiles index into the map array.
Pick - Middle click, changes the selected tile to the index of the tile the mouse is currently over.
Open Pallete - Right click, allows you to select tiles from the "/tiles/" folder. There is functionality for subfolders being represented.
Close Pallete - D, closes the pallete if it is currently open.
Undo - Ctrl-Z, undoes previous painting interactions.
Redo - Ctrl-Y, redoes previously undone interactions.

Indirect Interactions

Save - Under the "file" button in the menu bar. Saves the current map data to the map
Save As - Under the "file" button in the menu bar. Saves the current map with the provided file name.
Open Map - Under the "file" button in the menu bar. Opens the selected map, and displays its data.
Create Map - Under the "file" button in the menu bar. Creates a new map with the provided dimensions.
Current Selected Tile - Purely Visual, located in the menu bar. Displays the current selected tile and its index.
