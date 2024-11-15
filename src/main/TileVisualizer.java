package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import menu.CustomLabelRenderer;
import menu.CustomPanelRenderer;
import menu.CustomPopupMenuRenderer;
import stamps.StampCreationTool;
import stamps.StampTile;
import stamps.StampTool;

public class TileVisualizer extends CustomPanelRenderer {
	
	private static final long serialVersionUID = 1L;

	public String[] tilePaths;
    
    public BufferedImage[] tileImages;
    
    public BufferedImage[] originalTileImages;
    
    int[][] map;
    
    int numRows;
    
    String mapFilePath;
    
    boolean isFileOpen = false;
    
    int numCols;
    
    int tileSize = 50;
    
    public int selectedTileIndex = 0;
    
    int spacing = 10;
    
    Point viewPosition = new Point(0, 0);
    
    CustomLabelRenderer selectedTileIconLabel;
    
    public boolean isInContextMenu = false;
    
    boolean isSubMenuVisible = false;
    
    boolean mousePressed;
    
    int lastX, lastY;
    
    Stack<int[][]> undoStack = new Stack<>();
    
    Stack<int[][]> redoStack = new Stack<>();
    
    int[][] currentMapState;
    
    public CustomPopupMenuRenderer subMenu;
    
    double zoomLevel = 1.0;
    
    static Set<String> initialFolders = null;
    
    public Point mousePosition;
    
    public boolean showPreview = true;
    
    public boolean collision = false;
    
    public int originalTileSize;
    
    public float previewTransparency = 0.5f;
    
    List<StampTile> stampTiles = new ArrayList<>();
    
    boolean stampToolActive = false;
    
    StampTool stampTool;
    
    StampCreationTool stampCreationTool;
    
    ContextMenu contextMenu;
    
    public boolean isInMenuBar;

    public TileVisualizer(String[] tilePaths, int[][] map, int viewX, int viewY, ContextMenu contextMenu) {
    	
    	super(52, 37, 47);
    	
        this.tilePaths = tilePaths;
        
        this.setMap(map);
        
        this.contextMenu = contextMenu;
        
        this.setViewPosition(new Point(viewX, viewY));
        
        this.setCurrentMapState(copyMap(map));
        
        this.selectedTileIconLabel = new CustomLabelRenderer();
        
        this.selectedTileIconLabel.setPreferredSize(new Dimension(32, 32));

        calculateDimensions();
        
        loadTileImages();
        
        setFocusable(true);
        
        addMouseListener(createMouseListener());
        
        addMouseMotionListener(createMouseMotionListener());
        
        addMouseWheelListener(createMouseWheelListener());

        this.stampTool = new StampTool();
    }

    int[][] copyMap(int[][] original) {
    	
        int[][] copy = new int[original.length][];
        
        for (int i = 0; i < original.length; i++) {
        	
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        
        return copy;
    }

    void updateTileMap(int[][] newMapState) {
    	
        undoStack.push(copyMap(getCurrentMapState()));
        
        setCurrentMapState(copyMap(newMapState));
        
        checkCollision();
        
        redoStack.clear();
    }

    public void undo() {
    	
        if (!undoStack.isEmpty()) {
        	
            int[][] previousState = undoStack.pop();
            
            redoStack.push(copyMap(getCurrentMapState()));
            
            setMap(copyMap(previousState));
            
            checkCollision();
            
            repaint();
            
            setCurrentMapState(copyMap(previousState));
        }
    }

    public void redo() {
    	
        if (!redoStack.isEmpty()) {
        	
            int[][] nextState = redoStack.pop();
            
            undoStack.push(copyMap(getCurrentMapState()));
            
            setMap(nextState);
            
            checkCollision();
            
            repaint();
            
            setCurrentMapState(copyMap(nextState));
        }
    }

    MouseAdapter createMouseListener() {
    	
        return new MouseAdapter() {
        	
            @Override
            public void mousePressed(MouseEvent e) {
            	
                if (SwingUtilities.isLeftMouseButton(e)) {
                	
                    handleLeftClick(e);
                } 
                else if (SwingUtilities.isMiddleMouseButton(e)) {
                	
                    handleMiddleClick(e);
                } 
                else if (SwingUtilities.isRightMouseButton(e)) {
                	
                    contextMenu.showContextMenu(e);
                }
                
            	
                checkCollision();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            	
                if (SwingUtilities.isMiddleMouseButton(e) && !isInMenu()) {
                	
                    mousePressed = false;
                }
                
                updateSelectedTileIcon();
                
            	
                checkCollision();
            }

            boolean isInMenu() {
            	
            	if (subMenu != null) {
            		
            		return subMenu.isVisible();
            	}
            	else {
            		
            		return isInMenuBar;
            	}
            }

            void handleLeftClick(MouseEvent e) {
            	
                int mouseX = mousePosition.x;
                
                int mouseY = mousePosition.y;

                int col = (mouseX - 20) / (tileSize + spacing);
                
                int row = (mouseY - 20) / (tileSize + spacing);
                
                if (col >= 0 && col < numCols && row >= 0 && row < numRows) {
                    
                	if (stampTool.isActive() && stampTool.getStampPattern(stampTool.currentStampIndex) != null) {
                        
                		int[][] stamp = stampTool.getStampPattern(stampTool.currentStampIndex);
                		
                        int stampRows = stamp.length;
                        
                        int stampCols = stamp[0].length;

                        for (int i = 0; i < stampRows; i++) {
                        	
                            for (int j = 0; j < stampCols; j++) {
                            	
                                int stampTileIndex = stamp[i][j];
                                
                                int mapRow = row + i;
                                
                                int mapCol = col + j;

                                if (mapRow >= 0 && mapRow < numRows && mapCol >= 0 && mapCol < numCols) {
                                    
                                	getMap()[mapRow][mapCol] = stampTileIndex;
                                }
                            }
                        }
                    } 
                	else {
                		
                        getMap()[row][col] = selectedTileIndex;
                    }
                    
                    updateSelectedTileIcon();
                    
                    int[][] newMapState = copyMap(getMap());
                    
                    updateTileMap(newMapState);

                    repaint();
                }
            }

            void handleMiddleClick(MouseEvent e) {
            	
                lastX = e.getX();
                
                lastY = e.getY();

                int mouseX = e.getX();
                
                int mouseY = e.getY();
                
                int x = (int) ((mouseX + getViewPosition().x) / getZoomLevel());
                
                int y = (int) ((mouseY + getViewPosition().y) / getZoomLevel());
                
                int col = (x - 20) / (int) (tileSize + spacing);
                
                int row = (y - 20) / (int) (tileSize + spacing);

                if (e.isControlDown()) {
                	
                    mousePressed = true;
                } 
                else {
                	
                    if (col >= 0 && col < numCols && row >= 0 && row < numRows && !isInMenu()) {
                    	
                        selectedTileIndex = getMap()[row][col];

                        updateSelectedTileIcon();
                        
                        repaint();
                    }
                }
            }
        };
    }

    public int getSelectedTileIndex() {
    	
        checkCollision();
    	
        return selectedTileIndex;
    }

    MouseMotionAdapter createMouseMotionListener() {
    	
        return new MouseMotionAdapter() {
        	
            @Override
            public void mouseDragged(MouseEvent e) {
            	
                checkCollision();
            	
                if (mousePressed && !isInContextMenu && SwingUtilities.isMiddleMouseButton(e) && e.isControlDown()) {
                	
                    int dx = e.getX() - lastX;
                    
                    int dy = e.getY() - lastY;
                    
                    lastX = e.getX();
                    
                    lastY = e.getY();
                    
                    viewPosition.translate(-dx, -dy);
                    
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            	
                checkCollision();
            	
                int scaledX = (int) ((e.getX() + getViewPosition().x) / getZoomLevel());
                
                int scaledY = (int) ((e.getY() + getViewPosition().y) / getZoomLevel());
                
                mousePosition = new Point(scaledX, scaledY);
                
                repaint();
            }
        };
    }

    MouseAdapter createMouseWheelListener() {
    	
        return new MouseAdapter() {
        	
            @Override
            
            public void mouseWheelMoved(MouseWheelEvent e) {
            	
                if (!isInContextMenu && e.isControlDown()) {
                	
                    int mouseX = e.getX();
                    
                    int mouseY = e.getY();

                    double zoomFactor = (e.getPreciseWheelRotation() < 0) ? 1.1 : 0.9;
                    
                    double oldZoomLevel = getZoomLevel();
                    
                    double newZoomLevel = oldZoomLevel * zoomFactor;
                    
                    setZoomLevel(newZoomLevel);
                    
                    setZoomLevel(Math.max(0.1, getZoomLevel()));

                    double scale = newZoomLevel / oldZoomLevel;
                    
                    int newMouseX = (int) (mouseX * scale);
                    
                    int newMouseY = (int) (mouseY * scale);

                    viewPosition.x = (int) ((viewPosition.x + mouseX) * scale - newMouseX);
                    
                    viewPosition.y = (int) ((viewPosition.y + mouseY) * scale - newMouseY);

                    repaint();              
                }
                
                checkCollision();
            	
                int scaledX = (int) ((e.getX() + getViewPosition().x) / getZoomLevel());
                
                int scaledY = (int) ((e.getY() + getViewPosition().y) / getZoomLevel());
                
                mousePosition = new Point(scaledX, scaledY);
                
                repaint();
            }
        };
    }

    void calculateDimensions() {
    	
        numRows = getMap().length;
        
        numCols = getMap()[0].length;
    }

    void loadTileImages() {
    	
        if (tilePaths == null || tilePaths.length == 0) {
        	
            return;
        }

        tileImages = new BufferedImage[tilePaths.length];
        
        for (int i = 0; i < tilePaths.length; i++) {
        	
            try {
            	
                String path = tilePaths[i];
                
                InputStream inputStream = getClass().getResourceAsStream(path);
                
                if (inputStream != null) {
                	
                    tileImages[i] = ImageIO.read(inputStream);
                }
            } 
            catch (IOException e) {
            	
                e.printStackTrace();
            }
        }
    }

    public void updateSelectedTileIcon() {

    	if (selectedTileIndex >= (tilePaths.length / 2)) {
    		collision = true;
    	}
    	else {
    		collision = false;
    	}
    	
        if (tileImages == null || tileImages.length == 0) {
        	
            return;
        }

        if (selectedTileIndex < 0 || selectedTileIndex >= tileImages.length) {
        	
            return;
        }

        if (tileImages[selectedTileIndex] == null) {
        	
            return;
        }

        selectedTileIconLabel.setIcon(new ImageIcon(tileImages[selectedTileIndex]));
    }

    class FolderMouseListener extends MouseAdapter {
    	
        final String folder;

        public FolderMouseListener(String folder) {
        	
            this.folder = folder;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        	
            if (!isSubMenuVisible) {
            	
                if (subMenu == null) {
                	
                    subMenu = contextMenu.createSubMenu();
                }
                
                contextMenu.updateSubMenu(folder);
                
                Point location = e.getComponent().getLocationOnScreen();
                
                subMenu.setLocation(location.x + e.getComponent().getWidth(), location.y);
                
                subMenu.setVisible(true);
                
                isSubMenuVisible = true;
            }
        }
    }

    public static Set<String> getInitialFolders(String tilesDirectoryPath) {
    	
        Set<String> initialFolders = new LinkedHashSet<>();
        
        initialFolders.add("main");

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

    public static String[] generateTilePaths(Set<String> initialFolders) {
    	
        List<String> paths = new ArrayList<>();

        String tilesFolderPath = "res" + File.separator + "tiles" + File.separator;

        for (int pass = 0; pass < 2; pass++) {
        	
            for (String folder : initialFolders) {
            	
                File folderDir = new File(tilesFolderPath, folder);

                File[] tileFiles = folderDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

                if (tileFiles != null) {
                	
                    Arrays.sort(tileFiles, new AlphanumericComparator());

                    for (File tileFile : tileFiles) {
                    	
                        String relativePath = "/tiles/" + folder + "/" + tileFile.getName();
                        
                        paths.add(relativePath);
                    }
                }
            }
        }

        return paths.toArray(new String[0]);
    }


    static class AlphanumericComparator implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {

            String s1 = removeExtension(f1.getName());
            
            String s2 = removeExtension(f2.getName());

            return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
        }

        private String removeExtension(String filename) {
        	
            int dotIndex = filename.lastIndexOf('.');
            
            return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
        }
    }

    Map<String, List<Integer>> createFolderMap(String[] paths) {
    	
        Map<String, List<Integer>> folderMap = new HashMap<>();
        
        for (int i = 0; i < paths.length; i++) {
        	
            String path = paths[i];
            
            String[] pathParts = path.split("/");
            
            if (pathParts.length > 3) {
            	
                String folder = pathParts[pathParts.length - 2];
                
                if (getInitialFolders().contains(folder)) {
                	
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

        g2d.scale(getZoomLevel(), getZoomLevel());

        for (int row = 0; row < numRows && row < getMap().length; row++) {
        	
            if (getMap()[row] != null) {
            	
                for (int col = 0; col < numCols && col < getMap()[row].length; col++) {
                	
                    int tileIndex = getMap()[row][col];
                    
                    BufferedImage tileImage = tileImages[tileIndex];
                    
                    int x = col * (tileSize + spacing) + 20;
                    
                    int y = row * (tileSize + spacing) + 20;

                    int drawX = x - (int) (getViewPosition().x / getZoomLevel());
                    
                    int drawY = y - (int) (getViewPosition().y / getZoomLevel());

                    if (tileIndex >= tilePaths.length / 2) {

                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                        
                        g2d.setColor(new Color(255, 192, 203));
                        
                        g2d.fillRect(drawX, drawY, tileSize, tileSize);
                    }

                    g2d.drawImage(tileImage, drawX, drawY, tileSize, tileSize, null);

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }
        }

        if (showPreview && mousePosition != null) {
        	
            int previewX = mousePosition.x;
            
            int previewY = mousePosition.y;
            
            int col = (previewX - 20) / (tileSize + spacing);
            
            int row = (previewY - 20) / (tileSize + spacing);

            if (col >= 0 && col < numCols && row >= 0 && row < numRows) {
            	
                if (stampTool.isActive() && stampTool.getStampPattern(stampTool.currentStampIndex) != null) {
                   
                	int[][] stamp = stampTool.getStampPattern(stampTool.currentStampIndex);
                	
                    int stampRows = stamp.length;
                    
                    int stampCols = stamp[0].length;

                    for (int i = 0; i < stampRows; i++) {
                    	
                        for (int j = 0; j < stampCols; j++) {
                        	
                            int stampTileIndex = stamp[i][j];
                            
                            int x = 20 + (col + j) * (tileSize + spacing);
                            
                            int y = 20 + (row + i) * (tileSize + spacing);

                            if (stampTileIndex >= 0 && stampTileIndex < tileImages.length) {
                            	
                                BufferedImage stampTileImage = tileImages[stampTileIndex];
                                
                                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, previewTransparency));

                                if (stampTileIndex >= (tilePaths.length / 2)) {
                                	
                                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                                    
                                    g2d.setColor(new Color(255, 192, 203));
                                    
                                    g2d.fillRect(x - (int) (getViewPosition().x / getZoomLevel()), y - (int) (getViewPosition().y / getZoomLevel()), tileSize, tileSize);
                                    
                                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, previewTransparency));
                                }

                                g2d.drawImage(stampTileImage, x - (int) (getViewPosition().x / getZoomLevel()), y - (int) (getViewPosition().y / getZoomLevel()), tileSize, tileSize, null);

                                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                            }
                        }
                    }
                } 
                else {
                	
                    int x = 20 + col * (tileSize + spacing);
                    
                    int y = 20 + row * (tileSize + spacing);

                    if (selectedTileIndex >= 0 && selectedTileIndex < tileImages.length) {
                    	
                        BufferedImage selectedTileImage = tileImages[selectedTileIndex];
                        
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, previewTransparency));

                        if (collision) {
                        	
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                            
                            g2d.setColor(new Color(255, 192, 203)); // Pink color
                            
                            g2d.fillRect(x - (int) (getViewPosition().x / getZoomLevel()), y - (int) (getViewPosition().y / getZoomLevel()), tileSize, tileSize);
                            
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, previewTransparency));
                        }

                        g2d.drawImage(selectedTileImage, x - (int) (getViewPosition().x / getZoomLevel()), y - (int) (getViewPosition().y / getZoomLevel()), tileSize, tileSize, null);

                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    }
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
            
        } 
        catch (IOException e) {
        	
            e.printStackTrace();
            return null;
        }
    }

    public static int[][] createMap(int sizeX, int sizeY) {
    	
        int[][] map = new int[sizeY][sizeX];

        for (int row = 0; row < sizeY; row++) { 
        	
            for (int col = 0; col < sizeX; col++) { 
            	
                map[row][col] = 0;
            }
        }

        return map;
    }

    public void checkCollision() {
    	
    	if (selectedTileIndex >= (tilePaths.length / 2)) {
    		
    		collision = true;
    	}
    	else {
    		
    		collision = false;
    	}
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
            
        } 
        catch (IOException e) {
        	
            e.printStackTrace();
            
            return null;
        }
    }

    public void saveMapToFile(String filePath) {
    	
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
        	
            for (int[] row : getMap()) {
            	
                for (int col = 0; col < row.length; col++) {
                	
                    writer.write(row[col] + (col < row.length - 1 ? " " : ""));
                    
                }
                
                writer.newLine();
            }
            
        } 
        catch (IOException e) {
        	
            e.printStackTrace();
        }
    }

    public BufferedImage getTileIcon(int tileIndex) {

        return tileImages[tileIndex];
    }

    public BufferedImage getSelectedTileIcon() {

        Icon icon = selectedTileIconLabel.getIcon();
        
        if (icon instanceof ImageIcon) {
        	
            Image image = ((ImageIcon) icon).getImage();
            
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g2d = bufferedImage.createGraphics();
            
            g2d.drawImage(image, 0, 0, null);
            
            g2d.dispose();
            
            return bufferedImage;
        } 
        else {
        	
            return null;
        }
    }

    public void toggleStampTool() {
    	
        stampTool.setActive(!stampTool.isActive());
    }

    public String[] getTilePaths() {
    	
        return tilePaths;
    }

    public static Set<String> getInitialFolders() {
    	
        return initialFolders;
    }

    public static void setInitialFolders(Set<String> initialFolders) {
    	
        TileVisualizer.initialFolders = initialFolders;
    }

    public int[][] getMap() {
    	
        return map;
    }

    public void setMap(int[][] map) {
    	
        this.map = map;
    }

    public int[][] getCurrentMapState() {
    	
        return currentMapState;
    }

    public void setCurrentMapState(int[][] currentMapState) {
    	
        this.currentMapState = currentMapState;
    }

    public Point getViewPosition() {
    	
        return viewPosition;
    }

    public void setViewPosition(Point viewPosition) {
    	
        this.viewPosition = viewPosition;
    }

    public double getZoomLevel() {
    	
        return zoomLevel;
    }
    
    public void zoomIn() {
    	
    	setZoomLevel(getZoomLevel() + .1);
    }
    
    public void zoomOut() {
    	
    	if (getZoomLevel() > .1f) {
    		
    		setZoomLevel(getZoomLevel() - .1);
    	}
    }

    public void setZoomLevel(double zoomLevel) {
    	
        this.zoomLevel = zoomLevel;
    }

    public int getNumRows() {

        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public float getPreviewTransparency() {

        return previewTransparency;
    }

    public int getNumTiles() {

        return numCols * numRows;
    }

    public void setSelectedTileIndex(int i) {
    	
        selectedTileIndex = i;
    }

    public BufferedImage getMapImage() {

        try {
        	
            String path = tilePaths[0];
            
            InputStream inputStream = getClass().getResourceAsStream(path);
            
            if (inputStream != null) {
            	
                originalTileSize = ImageIO.read(inputStream).getWidth();
            }
        } 
        catch (IOException e) {
        	
            e.printStackTrace();
        }
    	
        int totalWidth = numCols * originalTileSize;
        
        int totalHeight = numRows * originalTileSize;

        BufferedImage mapImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = mapImage.createGraphics();

        originalTileImages = new BufferedImage[tilePaths.length];
        
        for (int i = 0; i < tilePaths.length; i++) {
        	
            try {
            	
                String path = tilePaths[i];
                
                InputStream inputStream = getClass().getResourceAsStream(path);
                
                if (inputStream != null) {
                	
                    originalTileImages[i] = ImageIO.read(inputStream);
                }
            } 
            catch (IOException e) {
            	
                e.printStackTrace();
            }
        }
        
        for (int row = 0; row < numRows; row++) {
        	
            for (int col = 0; col < numCols; col++) {
            	
                int tileIndex = map[row][col];
                
                BufferedImage tileImage = originalTileImages[tileIndex];
                
                int x = col * originalTileSize;
                
                int y = row * originalTileSize;
                
                g2d.drawImage(tileImage, x, y, originalTileSize, originalTileSize, null);
            }
        }

        g2d.dispose();

        return mapImage;
    }
}