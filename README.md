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
    - Open Map - Opens the selected map, and displays its data. Resets the zoom and map position.
    - Create Map - Creates a new map with the provided dimensions. Resets the zoom and map position.
    - Save - Saves the current map data to the currently opened map file.
    - Save As - Saves the current map with the provided file name in the provided location.
    - Export Image - Compiles the drawn tiles into a .PNG image, automatically detecting tile and image size.

2. <ins>Brush Menu</ins>
    - Toggle Preview - Toggles whether or not a preview will be displayed.
    - Toggle Collision - Toggles whether or not the painted tile will have collision*.
    - Toggle Stamp - Toggles whether or not the current selected stamp will be painted.
    - Stamp Creation Menu - Enables the user to create paintable stamps.
      - Width - Sets the width of the stamp.
      - Height - Sets the height of the stamp.
      - Create Grid - Creates the paintable stamp grid.
      - Save to File - Saves the stamp with the indicated name and file type (".dat" files are expected).
      - Load from File - Loads the selected file as a stamp, into the stamp pattern array.
    - Preview Transparency - Changes the transparency of the preview being displayed.
        
3. <ins>View Menu</ins>
     - Reset View - Resets the view position and zoom.
     - Reset Position - Just resets the view position.
     - Reset Zoom - Just resets the zoom.
     - Zoom + - Zooms in slightly.
     - Zoom - - Zooms out slightly.

4. <ins>Miscellaneous</ins>
    - Current Selected Tile - Purely Visual, located in the menu bar. Displays the current selected tile, if it currently has collision, and its index.

*Collision is achieved by adding the base number of tiles to the current index, essentially doubling the total numbers of tiles. You should account for this when making your game. For example, if you have ten tiles then number eleven is the zero index of the original tiles, just with collision.
