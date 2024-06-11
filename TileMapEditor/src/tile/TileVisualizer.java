package tile;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class TileVisualizer extends JPanel {
    private String[] tilePaths;
    private BufferedImage[] tileImages;
    private int[][] map;
    private int numRows;
    private int numCols;
    private int tileSize = 50;
    private int selectedTileIndex = 0;
    private int spacing = 10;
    private Point viewPosition;
    private JLabel selectedTileIconLabel;
    private boolean isContextMenuVisible = false;
    private boolean isSubMenuVisible = false;
    private boolean mousePressed;
    private int lastX, lastY;
    private Stack<int[][]> undoStack = new Stack<>();
    private Stack<int[][]> redoStack = new Stack<>();
    private int[][] currentMapState;
    private JPopupMenu subMenu;
    private double zoomLevel = 1.0;
    private final Set<String> initialFolders = new TreeSet<>(Arrays.asList("caves", "inside", "misc", "overworld", "paradise", "waves", "water"));

    public TileVisualizer(String[] tilePaths, int[][] map, int viewX, int viewY) {
        this.tilePaths = tilePaths;
        this.map = map;
        this.viewPosition = new Point(viewX, viewY);
        this.currentMapState = copyMap(map);
        this.selectedTileIconLabel = new JLabel();
        this.selectedTileIconLabel.setPreferredSize(new Dimension(32, 32)); // Set preferred size for the icon

        calculateDimensions();
        loadTileImages();
        setFocusable(true);
        addMouseListener(createMouseListener());
        addMouseMotionListener(createMouseMotionListener());
        addMouseWheelListener(createMouseWheelListener());
    }

    private int[][] copyMap(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    private void updateTileMap(int[][] newMapState) {
        undoStack.push(copyMap(currentMapState));
        currentMapState = copyMap(newMapState);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            int[][] previousState = undoStack.pop();
            redoStack.push(copyMap(currentMapState));
            map = copyMap(previousState);
            repaint();
            currentMapState = copyMap(previousState);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            int[][] nextState = redoStack.pop();
            undoStack.push(copyMap(currentMapState));
            map = nextState;
            repaint();
            currentMapState = copyMap(nextState);
        }
    }

    private MouseAdapter createMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    handleLeftClick(e);
                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    handleMiddleClick(e);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e) && !isInMenu(e.getX(), e.getY())) {
                    mousePressed = false;
                }
                updateSelectedTileIcon();
            }

            private boolean isInMenu(int mouseX, int mouseY) {
                Container parent = SwingUtilities.getAncestorOfClass(JViewport.class, TileVisualizer.this);
                if (parent instanceof JViewport) {
                    JViewport viewport = (JViewport) parent;
                    Point convertedPoint = SwingUtilities.convertPoint(TileVisualizer.this, mouseX, mouseY, viewport);
                    mouseX = convertedPoint.x;
                    mouseY = convertedPoint.y;
                }

                int x = (int) ((mouseX + viewPosition.x) / zoomLevel);
                int y = (int) ((mouseY + viewPosition.y) / zoomLevel);
                int col = (x - 20) / (int) (tileSize + spacing);
                int row = (y - 50) / (int) (tileSize + spacing);

                return col < 0 || col >= numCols || row < 0 || row >= numRows;
            }

            private void handleLeftClick(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                int x = (int) ((mouseX + viewPosition.x) / zoomLevel);
                int y = (int) ((mouseY + viewPosition.y) / zoomLevel);
                int col = (x - 20) / (int) (tileSize + spacing);
                int row = (y - 50) / (int) (tileSize + spacing);

                if (col >= 0 && col < numCols && row >= 0 && row < numRows && !isInMenu(mouseX, mouseY)) {
                    map[row][col] = selectedTileIndex;
                    isContextMenuVisible = false;
                    updateSelectedTileIcon();
                    int[][] newMapState = copyMap(map);
                    newMapState[row][col] = selectedTileIndex;
                    updateTileMap(newMapState);
                    repaint();
                }
            }

            private void handleMiddleClick(MouseEvent e) {
                mousePressed = true;
                lastX = e.getX();
                lastY = e.getY();

                int mouseX = e.getX();
                int mouseY = e.getY();
                int x = (int) ((mouseX + viewPosition.x) / zoomLevel);
                int y = (int) ((mouseY + viewPosition.y) / zoomLevel);
                int col = (x - 20) / (int) (tileSize + spacing);
                int row = (y - 50) / (int) (tileSize + spacing);

                if (col >= 0 && col < numCols && row >= 0 && row < numRows && !isInMenu(mouseX, mouseY)) {
                    selectedTileIndex = map[row][col];
                    isContextMenuVisible = false;
                    updateSelectedTileIcon();
                    repaint();
                }
            }
        };
    }

    private MouseMotionAdapter createMouseMotionListener() {
        return new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mousePressed && !isContextMenuVisible) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    lastX = e.getX();
                    lastY = e.getY();
                    viewPosition.translate(-dx, -dy);
                    repaint();
                }
            }
        };
    }

    private MouseAdapter createMouseWheelListener() {
        return new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!isContextMenuVisible) {
                    if (e.getPreciseWheelRotation() < 0) {
                        zoomLevel += 0.1;
                    } else {
                        zoomLevel = Math.max(0.1, zoomLevel - 0.1);
                    }
                    repaint();
                }
            }
        };
    }

    private void calculateDimensions() {
        numRows = map.length;
        numCols = map[0].length;
    }

    private void loadTileImages() {
        tileImages = new BufferedImage[tilePaths.length];
        for (int i = 0; i < tilePaths.length; i++) {
            try {
                tileImages[i] = ImageIO.read(getClass().getResourceAsStream(tilePaths[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showContextMenu(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        int col = (mouseX - 20) / (int) (tileSize * zoomLevel + spacing);
        int row = (mouseY - 50) / (int) (tileSize * zoomLevel + spacing);

        if (!isContextMenuVisible && col >= 0 && col < numCols && row >= 0 && row < numRows) {
            JPopupMenu contextMenu = new JPopupMenu();

            Map<String, List<Integer>> folderMap = createFolderMap(tilePaths);
            for (String folder : initialFolders) {
                JMenuItem folderMenuItem = new JMenuItem(folder);
                folderMenuItem.addActionListener(ev -> {});
                folderMenuItem.addMouseListener(new FolderMouseListener(folder));
                contextMenu.add(folderMenuItem);
            }

            contextMenu.addPopupMenuListener(new ContextMenuListener());
            contextMenu.show(this, mouseX, mouseY);
        }
    }

    private class FolderMouseListener extends MouseAdapter {
        private final String folder;

        public FolderMouseListener(String folder) {
            this.folder = folder;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (!isSubMenuVisible) {
                if (subMenu == null) {
                    subMenu = createSubMenu();
                }
                updateSubMenu(folder);
                Point location = e.getComponent().getLocationOnScreen();
                subMenu.setLocation(location.x + e.getComponent().getWidth(), location.y);
                subMenu.setVisible(true);
                isSubMenuVisible = true;
            }
        }

        private JPopupMenu createSubMenu() {
            JPopupMenu subMenu = new JPopupMenu();
            subMenu.setLayout(new GridLayout(0, 5));
            subMenu.setPreferredSize(new Dimension(400, 300));
            subMenu.addPopupMenuListener(new SubMenuListener());
            return subMenu;
        }

        private void updateSubMenu(String folder) {
            subMenu.removeAll();
            Map<String, List<Integer>> folderMap = createFolderMap(tilePaths);
            List<Integer> tileIndexes = folderMap.get(folder);
            for (Integer tileIndex : tileIndexes) {
                JButton tileButton = new JButton(new ImageIcon(tileImages[tileIndex]));
                tileButton.setPreferredSize(new Dimension(50, 50));
                tileButton.addActionListener(e -> {
                    selectedTileIndex = tileIndex;
                    subMenu.setVisible(false);
                    isSubMenuVisible = false;
                    isContextMenuVisible = false;
                    updateSelectedTileIcon();
                });
                subMenu.add(tileButton);
            }
        }
    }

    private class ContextMenuListener implements PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            isContextMenuVisible = true;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            isContextMenuVisible = false;
            if (!isSubMenuVisible) {
                subMenu.setVisible(false);
            }
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            isContextMenuVisible = false;
        }
    }

    private class SubMenuListener implements PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            isSubMenuVisible = true;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            isSubMenuVisible = false;
            isContextMenuVisible = false;
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            isSubMenuVisible = false;
        }
    }

    private void updateSelectedTileIcon() {
        selectedTileIconLabel.setIcon(new ImageIcon(tileImages[selectedTileIndex]));
    }

    private Map<String, List<Integer>> createFolderMap(String[] tilePaths) {
        Map<String, List<Integer>> folderMap = new HashMap<>();
        for (int i = 0; i < tilePaths.length; i++) {
            String path = tilePaths[i];
            String folder = path.split("/")[1];
            folderMap.computeIfAbsent(folder, k -> new ArrayList<>()).add(i);
        }
        return folderMap;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(zoomLevel, zoomLevel);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int tileIndex = map[row][col];
                BufferedImage tileImage = tileImages[tileIndex];
                int x = col * (tileSize + spacing) + 20;
                int y = row * (tileSize + spacing) + 50;
                g2d.drawImage(tileImage, x - viewPosition.x, y - viewPosition.y, tileSize, tileSize, null);
            }
        }
    }
    
    public static void main(String[] args) {
    	
        String[] tilePaths = generateTilePaths(initialFolders, 3123);
        
        public static String[] generateTilePaths(Set<String> initialFolders, int numberOfTiles) {
            ArrayList<String> paths = new ArrayList<>();

            for (String folder : initialFolders) {
                for (int i = 0; i < numberOfTiles; i++) {
                    String filePath = "/tiles/" + folder + "/" + "Tile" + i + ".png"; // Adjust the file naming convention as needed
                    paths.add(filePath);
                }
            }

            return paths.toArray(new String[0]);
        }
        
        // Load map data from file
        int[][] map = TileVisualizer.loadMapFromFile("/maps/map0.txt");

        JFrame frame = new JFrame("TheCasualWendigo's Tile Editor");

        TileVisualizer visualizer = new TileVisualizer(tilePaths, map, 0, 0);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizer.saveMapToFile("res/maps/map0.txt");
            }
        });
        fileMenu.add(saveMenuItem);
        menuBar.add(fileMenu);
        
        visualizer.updateSelectedTileIcon(); 

        JMenuItem selectedTileMenuItem = new JMenuItem("Selected Tile: " + visualizer.selectedTileIndex + " ") {
            @Override
            public void paint(Graphics g) {
                super.paint(g); // Call the default paint method first

                BufferedImage iconImage = visualizer.getSelectedTileIcon();
                if (iconImage != null) {
                    FontMetrics fontMetrics = g.getFontMetrics();
                    int textWidth = fontMetrics.stringWidth(getText());
                    int iconWidth = iconImage.getWidth() / 2;
                    int iconHeight = iconImage.getHeight() / 2;
                    int totalWidth = textWidth + iconWidth + 5; // Adjust 5 according to your preference
                    int textX = getWidth() - totalWidth;
                    int textY = (getHeight() + fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;
                    int iconX = textX + textWidth;
                    int iconY = (getHeight() - iconHeight) / 2;

                    // Draw the text
                    g.drawString(getText(), textX, textY);

                    // Draw the icon
                    g.drawImage(iconImage, iconX, iconY, iconWidth, iconHeight, null);
                }
            }
        };


     // Create an action for decrementing the selected tile index
     Action decrementAction = new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
             visualizer.selectedTileIndex = (visualizer.selectedTileIndex - 1 + tilePaths.length) % tilePaths.length;
             visualizer.updateSelectedTileIcon();
             selectedTileMenuItem.setText("Selected Tile: " + visualizer.selectedTileIndex + " ");
         }
     };

     // Create an action for incrementing the selected tile index
     Action incrementAction = new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
             visualizer.selectedTileIndex = (visualizer.selectedTileIndex + 1) % tilePaths.length;
             visualizer.updateSelectedTileIcon();
             selectedTileMenuItem.setText("Selected Tile: " + visualizer.selectedTileIndex + " ");
         }
     };
     
     Action closeAction = new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
        	 visualizer.subMenu.setVisible(false);
         }
     };
     
    	
        Action undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
           	 visualizer.undo();
            }
        };
        
        Action redoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
           	 visualizer.redo();
            }
        };

    	// Other key bindings for decrement, increment, and close actions
    	visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "decrement");
    	visualizer.getActionMap().put("decrement", decrementAction);
    	visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "increment");
    	visualizer.getActionMap().put("increment", incrementAction);
    	visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "close");
    	visualizer.getActionMap().put("close", closeAction);
     
    	visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
    	visualizer.getActionMap().put("undo", undoAction);
    	
    	visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
    	visualizer.getActionMap().put("redo", redoAction);


        // Bind the refresh action to a left mouse click
        visualizer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    visualizer.updateSelectedTileIcon();
                    selectedTileMenuItem.setText("Selected Tile: " + visualizer.selectedTileIndex + " ");
                }
            }
        });
    	
    	
     // Add the selectedTileMenuItem to the menu bar
     menuBar.add(selectedTileMenuItem);

     // Set the menu bar to the frame
     frame.setJMenuBar(menuBar); // Add the menu bar to the frame

     visualizer.setBackground(new Color(40, 43, 48)); 
     
        // Add the visualizer panel to the frame
     frame.add(visualizer);
     frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set frame to fullscreen
     frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
     frame.setVisible(true);

    }
}