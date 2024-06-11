package main;

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
    private static Set<String> initialFolders = null;

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
        // Get the mouse coordinates directly from the MouseEvent
        int mouseX = e.getX();
        int mouseY = e.getY();

        // Calculate the column and row based on the mouse position and view settings
        int col = (mouseX - 20) / (int) (tileSize * zoomLevel + spacing);
        int row = (mouseY - 50) / (int) (tileSize * zoomLevel + spacing);

        // Check if the context menu should be shown
        if (!isContextMenuVisible && col >= 0 && col < numCols && row >= 0 && row < numRows) {
            JPopupMenu contextMenu = new JPopupMenu();

            // Create the folder map and populate the context menu
            Map<String, List<Integer>> folderMap = createFolderMap(tilePaths);
            for (String folder : initialFolders) {
                JMenuItem folderMenuItem = new JMenuItem(folder);
                folderMenuItem.addActionListener(ev -> {
                    // Do nothing on click
                });
                folderMenuItem.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!isSubMenuVisible) {
                            if (subMenu == null) {
                                subMenu = createSubMenu(); // Create submenu if not already created
                            }
                            updateSubMenu(folder); // Update submenu content
                            Point p = folderMenuItem.getLocationOnScreen();
                            subMenu.setLocation(p.x + folderMenuItem.getWidth(), p.y);
                            subMenu.setVisible(true);
                            isSubMenuVisible = true;
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (subMenu != null) {
                            subMenu.setVisible(false); // Hide submenu on mouse exit
                            isSubMenuVisible = false;
                        }
                    }
                });
                contextMenu.add(folderMenuItem);
            }

            // Add popup menu listener to manage context menu visibility
            contextMenu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    isContextMenuVisible = true;
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    isContextMenuVisible = false;
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    isContextMenuVisible = false;
                }
            });

            // Show the context menu at the true mouse position
            contextMenu.show(this, mouseX, mouseY);
        }
    }
    
    private JPopupMenu createSubMenu() {
        JPopupMenu subMenu = new JPopupMenu();
        subMenu.setPreferredSize(new Dimension(200, 300)); // Set preferred size for the submenu
        JScrollPane scrollPane = new JScrollPane();
        subMenu.add(scrollPane);
        return subMenu;
    }

    private void updateSubMenu(String folder) {
        JViewport viewport = ((JScrollPane) subMenu.getComponent(0)).getViewport();
        viewport.removeAll(); // Clear existing content

        Map<String, List<Integer>> folderMap = createFolderMap(tilePaths);
        List<Integer> indices = folderMap.get(folder);

        if (indices != null) {
            JPanel menuPanel = new JPanel();
            menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

            for (int index : indices) {
                JMenuItem menuItem = new JMenuItem("Tile " + index);
                menuItem.setIcon(new ImageIcon(tileImages[index].getScaledInstance(16, 16, Image.SCALE_DEFAULT)));
                menuItem.addActionListener(ev -> {
                    selectedTileIndex = index;
                    repaint();
                });
                menuPanel.add(menuItem);
            }

            JScrollPane scrollPane = (JScrollPane) subMenu.getComponent(0);
            scrollPane.setViewportView(menuPanel);
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

    public class TilePathGenerator {
        public static String[] generateTilePaths(String tilesDirectoryPath, Set<String> initialFolders) {
            List<String> paths = new ArrayList<>();

            for (String folder : initialFolders) {
                System.out.println("Searching folder: " + folder);
                File folderDir = new File(tilesDirectoryPath, folder);
                collectTilePaths(paths, folderDir, folder);
            }

            return paths.toArray(new String[0]);
        }
        
        
    }
    
    private static void collectTilePaths(List<String> paths, File folderDir, String relativePath) {
        File[] files = folderDir.listFiles();
        if (files != null) {
            // Sort folders alphabetically
            Arrays.sort(files, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println("Entering subfolder: " + file.getName());
                    // Recursively search subfolders
                    collectTilePaths(paths, file, relativePath + "/" + file.getName());
                } else if (file.getName().toLowerCase().endsWith(".png")) {
                    // Found a tile image
                    System.out.println("Found tile image: " + file.getName());
                    paths.add("/tiles" + relativePath + "/" + file.getName());
                }
            }
        }
    }

    public static Set<String> getInitialFolders(String tilesDirectoryPath) {
        Set<String> initialFolders = new TreeSet<>();
        File tilesDirectory = new File(tilesDirectoryPath);

        if (tilesDirectory.exists() && tilesDirectory.isDirectory()) {
            File[] folders = tilesDirectory.listFiles(File::isDirectory);
            if (folders != null) {
                for (File folder : folders) {
                    initialFolders.add(folder.getName());
                }
            }
        }

        return initialFolders;
    }

    public static String[] generateTilePaths(String tilesDirectoryPath, Set<String> initialFolders) {
        List<String> paths = new ArrayList<>();

        for (String folder : initialFolders) {
            File folderDir = new File(tilesDirectoryPath, folder);
            File[] tileFiles = folderDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

            if (tileFiles != null) {
                // Sort tile files based on the number in the tile name
                Arrays.sort(tileFiles, (f1, f2) -> {
                    String name1 = f1.getName();
                    String name2 = f2.getName();
                    // Extract numbers from file names
                    int number1 = extractNumber(name1);
                    int number2 = extractNumber(name2);
                    // Compare the extracted numbers
                    return Integer.compare(number1, number2);
                });
                for (File tileFile : tileFiles) {
                    String relativePath = "/tiles/" + folder + "/" + tileFile.getName();
                    paths.add(relativePath);
                }
            }
        }

        return paths.toArray(new String[0]);
    }

    private static int extractNumber(String name) {
        // Extract numbers from the file name
        String numStr = name.replaceAll("[^0-9]", "");
        // Parse the extracted number
        return numStr.isEmpty() ? 0 : Integer.parseInt(numStr);
    }

    
    private void updateSelectedTileIcon() {
        selectedTileIconLabel.setIcon(new ImageIcon(tileImages[selectedTileIndex]));
    }

    private Map<String, List<Integer>> createFolderMap(String[] paths) {
        Map<String, List<Integer>> folderMap = new HashMap<>();
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            String[] pathParts = path.split("/");
            if (pathParts.length > 3) {
                String folder = pathParts[pathParts.length - 2]; 
                if (initialFolders.contains(folder)) {
                    folderMap.computeIfAbsent(folder, k -> new ArrayList<>()).add(i);
                }
            }
        }
        return folderMap;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(zoomLevel, zoomLevel);

        // Ensure that the loop indices do not exceed the dimensions of the map array
        for (int row = 0; row < numRows && row < map.length; row++) {
            if (map[row] != null) { // Check if the row is not null
                for (int col = 0; col < numCols && col < map[row].length; col++) {
                    int tileIndex = map[row][col];
                    BufferedImage tileImage = tileImages[tileIndex];
                    int x = col * (tileSize + spacing) + 20;
                    int y = row * (tileSize + spacing) + 50;
                    g2d.drawImage(tileImage, x - viewPosition.x, y - viewPosition.y, tileSize, tileSize, null);
                }
            }
        }
    }

    
    public static int[][] loadMapFromFile(String filePath) {
        try {
            InputStream is = TileVisualizer.class.getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            List<int[]> lines = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int[] row = new int[parts.length];
                for (int col = 0; col < parts.length; col++) {
                    row[col] = Integer.parseInt(parts[col]);
                }
                lines.add(row);
            }
            br.close();
            return lines.toArray(new int[0][0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static int[][] createMap(int sizeX, int sizeY) {
        int[][] map = new int[sizeX][sizeY];
        // Initialize the map with default values
        for (int row = 0; row < sizeX; row++) {
            for (int col = 0; col < sizeY; col++) {
                // Default value can be 0 or any other value you prefer
                map[row][col] = 0;
            }
        }
        return map;
    }


    
    public static int[][] loadMapFromAbsolutePath(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            List<int[]> lines = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int[] row = new int[parts.length];
                for (int col = 0; col < parts.length; col++) {
                    row[col] = Integer.parseInt(parts[col]);
                }
                lines.add(row);
            }
            br.close();
            return lines.toArray(new int[0][0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void saveMapToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int[] row : map) {
                for (int col = 0; col < row.length; col++) {
                    writer.write(row[col] + (col < row.length - 1 ? " " : ""));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public BufferedImage getSelectedTileIcon() {
        // Assuming selectedTileIconLabel is a JLabel containing the selected tile icon image
        Icon icon = selectedTileIconLabel.getIcon();
        if (icon instanceof ImageIcon) {
            Image image = ((ImageIcon) icon).getImage();
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            return bufferedImage;
        } else {
            return null; // Return null if the icon is not an ImageIcon
        }
    }
    
    public static void main(String[] args) {
    	
    	initialFolders = getInitialFolders("res/tiles/");
    	
        String[] tilePaths = generateTilePaths("res/tiles/", initialFolders);
        
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

        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showSaveDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();
                    visualizer.saveMapToFile(filePath);
                }
            }
        });
        fileMenu.add(saveAsMenuItem);
    
        JMenuItem openMenuItem = new JMenuItem("Open Map");
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();
                    int[][] newMap = TileVisualizer.loadMapFromAbsolutePath(filePath);
                    if (newMap != null) {
                        visualizer.map = newMap;
                        visualizer.currentMapState = visualizer.copyMap(newMap);
                        visualizer.repaint();
                    } else {
                        JOptionPane.showMessageDialog(null, "Error loading map from file.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(openMenuItem);

        JMenuItem createMapMenuItem = new JMenuItem("Create Map");
        createMapMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sizeXStr = JOptionPane.showInputDialog("Enter map width (X):");
                String sizeYStr = JOptionPane.showInputDialog("Enter map height (Y):");
                
                try {
                    int sizeX = Integer.parseInt(sizeXStr);
                    int sizeY = Integer.parseInt(sizeYStr);
                    visualizer.map = createMap(sizeX, sizeY);
                    visualizer.currentMapState = visualizer.copyMap(visualizer.map);
                    visualizer.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileMenu.add(createMapMenuItem);
        
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
                    int totalWidth = textWidth + iconImage.getWidth() + 10; // Adjust spacing according to your preference
                    int textX = getWidth() - totalWidth;
                    int textY = (getHeight() + fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;
                    int iconX = textX + textWidth + 5; // Add spacing between text and icon
                    int iconY = (getHeight() - iconImage.getHeight()) / 2;

                    // Draw the text
                    g.drawString(getText(), textX, textY);

                    // Draw the scaled icon
                    double scaleFactor = 2; // Adjust scaling factor as needed
                    int scaledIconWidth = (int) (iconImage.getWidth() * scaleFactor);
                    int scaledIconHeight = (int) (iconImage.getHeight() * scaleFactor);
                    int scaledIconX = iconX + (iconImage.getWidth() - scaledIconWidth) / 2;
                    int scaledIconY = iconY + (iconImage.getHeight() - scaledIconHeight) / 2;
                    g.drawImage(iconImage, scaledIconX, scaledIconY, scaledIconWidth, scaledIconHeight, null);
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