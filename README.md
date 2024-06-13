# JavaTilemapEditor
This is an eclipse project for a java tilemap-editor.<br>
**Please supply _your own_ tiles. This _does not_ come with pre-packaged tiles.**<br>
Tiles should be placed within the /res/tiles/ folder. Folders of tiles can be placed in that directory.<br>

# Features / Controls

## Direct Interactions

1. <ins>Mouse Interactions</ins>
    - Paint - Left click, paints currently selected tile. Inserts the tiles index into the map array.
    - Pick - Middle click, changes the selected tile to the index of the tile the mouse is currently over.
    - Open Pallete - Right click, allows you to select tiles from the "/tiles/" folder. There is functionality for subfolders being represented.

2. <ins>Keyboard Interactions</ins>
    - Close Pallete - D, closes the pallete if it is currently open.
    - Undo - Ctrl + Z, undoes previous painting interactions.
    - Redo - Ctrl + Y, redoes previously undone interactions.

3. <ins>Mouse and Keyboard Interactions</ins>
    - Zoom - Ctrl + Scroll. Zooms in and out of the map.
    - Pan - Ctrl + Middle Click. Pans across the currently opened map.

## Menus / Indirect Interactions

1. <ins>File Menu</ins>
    - Save - Saves the current map data to the currently opened map file.
    - Save As - Saves the current map with the provided file name in the provided location.
    - Open Map - Opens the selected map, and displays its data. Resets the zoom and map position.
    - Create Map - Creates a new map with the provided dimensions. Resets the zoom and map position.

2. <ins>Brush Menu</ins>
    - Toggle Preview - Toggles whether or not a preview will be displayed.
    - Preview Transparency - Changes the transparency of the preview being displayed.

3. <ins>Miscellaneous</ins>
    - Current Selected Tile - Purely Visual, located in the menu bar. Displays the current selected tile and its index.
