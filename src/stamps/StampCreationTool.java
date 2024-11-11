package stamps;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.*;
import main.ContextMenu;
import main.TileVisualizer;
import menu.CustomButtonRenderer;
import menu.CustomFileChooserRenderer;
import menu.CustomFrameRenderer;
import menu.CustomLabelRenderer;
import menu.CustomMenuItemRenderer;
import menu.CustomOptionPaneRenderer;
import menu.CustomPanelRenderer;
import menu.CustomScrollPaneRenderer;
import menu.CustomTextFieldRenderer;

public class StampCreationTool extends CustomFrameRenderer {

    private static final long serialVersionUID = 1L;

    private int gridWidth;

    private int gridHeight;

    private int tileSize = 50;

    private int spacing = 5;

    private TileVisualizer tileVisualizer;

    public GridPanel gridPanel;

    private JButton nextStampButton;

    private JButton prevStampButton;

    private JButton saveToFileButton;

    private JButton loadFromFileButton;

    public ContextMenu contextMenu;

    CustomPanelRenderer mainPanel;

    CustomPanelRenderer controlPanel;

    CustomTextFieldRenderer widthField;

    CustomTextFieldRenderer heightField;

    CustomButtonRenderer createGridButton;

    private Point mousePosition;

    public StampTool stampTool;

    private int currentStampIndex;

    private BufferedImage[] translucentTileImages;

    public CustomMenuItemRenderer selectedTileMenuItem;

    int lastX, lastY;

    public StampCreationTool(TileVisualizer tileVisualizer, StampTool stampTool, ContextMenu contextMenu) {

    	super();

    	this.tileVisualizer = tileVisualizer;

        this.stampTool = stampTool;

        this.currentStampIndex = stampTool.getCurrentStampIndex();

        this.contextMenu = contextMenu;

        initializeUI();

        setLocationRelativeTo(null);

        this.translucentTileImages = new BufferedImage[tileVisualizer.tileImages.length];

        for (int i = 0; i < tileVisualizer.tileImages.length; i++) {

            this.translucentTileImages[i] = createTranslucentImage(tileVisualizer.tileImages[i], tileVisualizer.previewTransparency);
        }

        setupKeyBindings();
    }

    private void initializeUI() {

        setTitle("Stamp Creation Tool");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setAlwaysOnTop(true);

        mainPanel = new CustomPanelRenderer(new BorderLayout(), 52, 37, 47);

        controlPanel = new CustomPanelRenderer();

        widthField = new CustomTextFieldRenderer(3, 52, 37, 47, 255, 255, 255);

        heightField = new CustomTextFieldRenderer(3, 52, 37, 47, 255, 255, 255);

        createGridButton = new CustomButtonRenderer("Create Grid")
        		;
        nextStampButton = new CustomButtonRenderer(">");

        prevStampButton = new CustomButtonRenderer("<");

        saveToFileButton = new CustomButtonRenderer("Save to File");

        loadFromFileButton = new CustomButtonRenderer("Load from File");

        updateArrowButtonsState();

        nextStampButton.addActionListener(e -> {

            if (stampTool.getAllStampPatterns().size() > 1) {

                currentStampIndex = (currentStampIndex + 1) % stampTool.getAllStampPatterns().size();

                stampTool.setCurrentStampIndex(currentStampIndex);

                loadStampPattern(currentStampIndex);

                updateArrowButtonsState();
            }
        });

        prevStampButton.addActionListener(e -> {

            if (stampTool.getAllStampPatterns().size() > 1) {

                currentStampIndex = (currentStampIndex - 1 + stampTool.getAllStampPatterns().size()) % stampTool.getAllStampPatterns().size();

                stampTool.setCurrentStampIndex(currentStampIndex);

                loadStampPattern(currentStampIndex);

                updateArrowButtonsState();
            }
        });

        createGridButton.addActionListener(e -> {
            try {

                gridWidth = Integer.parseInt(widthField.getText());

                gridHeight = Integer.parseInt(heightField.getText());

                createGrid(gridWidth, gridHeight);
            } 
            catch (NumberFormatException ex) {

                CustomOptionPaneRenderer.showMessageDialog(this, "Please enter valid integers for grid size.");
            }
        });

        saveToFileButton.addActionListener(e -> {

            CustomFileChooserRenderer fileChooser = new CustomFileChooserRenderer();

            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

                @Override
                public boolean accept(java.io.File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".dat");
                }

                @Override
                public String getDescription() {

                    return "DAT files (*.dat)";
                }
            });

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                java.io.File file = fileChooser.getSelectedFile();

                if (!file.getName().toLowerCase().endsWith(".dat")) {

                    file = new java.io.File(file.getParentFile(), file.getName() + ".dat");
                }

                try {
                    stampTool.saveToFile(
                    		file.getPath());

                    CustomOptionPaneRenderer.showMessageDialog(this, "Stamps saved successfully.");
                } 
                catch (IOException ex) {

                    CustomOptionPaneRenderer.showMessageDialog(this, "Failed to save stamps.");

                    ex.printStackTrace();
                }

            }

        });

        loadFromFileButton.addActionListener(e -> {

            CustomFileChooserRenderer fileChooser = new CustomFileChooserRenderer();

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                try {

                    int[][] loadedStamp = stampTool.loadFromFile(fileChooser.getSelectedFile().getPath());

                    stampTool.addStampPattern(loadedStamp);

                    loadStamp();

                    CustomOptionPaneRenderer.showMessageDialog(this, "Stamps loaded successfully.");
                } 
                catch (IOException | ClassNotFoundException ex) {

                	CustomOptionPaneRenderer.showMessageDialog(this, "Failed to load stamps.");

                    ex.printStackTrace();
                }
            }
        });

        controlPanel.add(new CustomLabelRenderer("Width:"));

        controlPanel.add(widthField);

        controlPanel.add(new CustomLabelRenderer("Height:"));

        controlPanel.add(heightField);

        controlPanel.add(createGridButton);

        controlPanel.add(prevStampButton);

        controlPanel.add(nextStampButton);

        controlPanel.add(saveToFileButton);

        controlPanel.add(loadFromFileButton);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        gridPanel = new GridPanel();

        CustomPanelRenderer centerPanel = new CustomPanelRenderer(new GridBagLayout(), 52, 37, 47);

        centerPanel.add(gridPanel);

        mainPanel.add(new CustomScrollPaneRenderer(centerPanel), BorderLayout.CENTER);

        mainPanel.setPreferredSize(new Dimension(650,400));

        add(mainPanel);

        pack();

        gridPanel.addMouseListener(createMouseListener());

        gridPanel.addMouseMotionListener(createMouseMotionListener());
    }

    private void setupKeyBindings() {

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "decrement");

        getRootPane().getActionMap().put("decrement", new AbstractAction() {

        	private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {

                if (tileVisualizer.collision) {

                    tileVisualizer.selectedTileIndex = (tileVisualizer.selectedTileIndex - 1 + (tileVisualizer.tilePaths.length / 2)) % tileVisualizer.tilePaths.length;

                    if (tileVisualizer.selectedTileIndex < tileVisualizer.tilePaths.length / 2) {

                    	tileVisualizer.selectedTileIndex += tileVisualizer.tilePaths.length / 2;
                    }
                } 
                else {

                    tileVisualizer.selectedTileIndex = (tileVisualizer.selectedTileIndex - 1 + (tileVisualizer.tilePaths.length / 2)) % (tileVisualizer.tilePaths.length / 2);
                }

                tileVisualizer.updateSelectedTileIcon();

                tileVisualizer.repaint();

                selectedTileMenuItem.repaint();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "increment");

        getRootPane().getActionMap().put("increment", new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {

                if (tileVisualizer.collision) {

                    tileVisualizer.selectedTileIndex = (tileVisualizer.selectedTileIndex + 1) % tileVisualizer.tilePaths.length;

                    if (tileVisualizer.selectedTileIndex < tileVisualizer.tilePaths.length / 2) {

                    	tileVisualizer.selectedTileIndex += tileVisualizer.tilePaths.length / 2;
                    }
                } 
                else {

                    tileVisualizer.selectedTileIndex = (tileVisualizer.selectedTileIndex + 1) % (tileVisualizer.tilePaths.length / 2);
                }

                tileVisualizer.updateSelectedTileIcon();

                tileVisualizer.repaint();

                selectedTileMenuItem.repaint();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "closeContextMenu");

        getRootPane().getActionMap().put("closeContextMenu", new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {

                if (tileVisualizer.subMenu != null) {

                    tileVisualizer.subMenu.setVisible(false);
                }
            }
        });
    }

    MouseMotionAdapter createMouseMotionListener() {

        return new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {

                mousePosition = new Point(e.getX(), e.getY());

                repaint();
            }
        };
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


                tileVisualizer.checkCollision();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                tileVisualizer.updateSelectedTileIcon();


                tileVisualizer.checkCollision();
            }

            private void handleLeftClick(MouseEvent e) {

                int mouseX = e.getX();

                int mouseY = e.getY();

                int col = mouseX / (tileSize + spacing);

                int row = mouseY / (tileSize + spacing);

                if (col >= 0 && col < gridWidth && row >= 0 && row < gridHeight) {

                    stampTool.getStampPattern(currentStampIndex)[row][col] = tileVisualizer.selectedTileIndex;

                    tileVisualizer.updateSelectedTileIcon();

                    repaint();
                }
            }

			private void handleMiddleClick(MouseEvent e) {

                int mouseX = e.getX();

                int mouseY = e.getY();

                int col = mouseX / (tileSize + spacing);

                int row = mouseY / (tileSize + spacing);

                if (col >= 0 && col < gridWidth && row >= 0 && row < gridHeight) {

                	tileVisualizer.selectedTileIndex = stampTool.getStampPattern(currentStampIndex)[row][col];

                    tileVisualizer.updateSelectedTileIcon();

                    repaint();
                }
            }
        };
    }

    public class GridPanel extends CustomPanelRenderer {


        public GridPanel() {

			super(52, 37, 47);
		}

		@Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            int[][] map = stampTool.getStampPattern(currentStampIndex);

            int startX = (getWidth() - (gridWidth * (tileSize + spacing))) / 2;

            int startY = (getHeight() - (gridHeight * (tileSize + spacing))) / 2;

            for (int row = 0; row < gridHeight; row++) {

                for (int col = 0; col < gridWidth; col++) {

                    int tileIndex = map[row][col];

                    int x = startX + col * (tileSize + spacing);

                    int y = startY + row * (tileSize + spacing);

                    if (tileIndex >= tileVisualizer.tilePaths.length / 2) {

                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                        g2d.setColor(new Color(255, 192, 203));

                        g2d.fillRect(x, y, tileSize, tileSize);
                    }

                    BufferedImage tileImage = tileVisualizer.tileImages[tileIndex];

                    g2d.drawImage(tileImage, x, y, tileSize, tileSize, null);

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }

            if (tileVisualizer.showPreview && mousePosition != null) {

                int previewX = mousePosition.x;

                int previewY = mousePosition.y;

                int col = (previewX - startX) / (tileSize + spacing);

                int row = (previewY - startY) / (tileSize + spacing);

                if (col >= 0 && col < gridWidth && row >= 0 && row < gridHeight) {

                    if (stampTool.isActive() && stampTool.getStampPattern(stampTool.currentStampIndex) != null) {

                    	int[][] stamp = stampTool.getStampPattern(stampTool.currentStampIndex);

                    	int stampRows = stamp.length;

                    	int stampCols = stamp[0].length;

                        for (int i = 0; i < stampRows; i++) {

                            for (int j = 0; j < stampCols; j++) {

                                int stampTileIndex = stamp[i][j];

                                int x = startX + (col + j) * (tileSize + spacing);

                                int y = startY + (row + i) * (tileSize + spacing);

                                if (stampTileIndex >= 0 && stampTileIndex < tileVisualizer.tileImages.length) {

                                	BufferedImage stampTileImage = tileVisualizer.tileImages[stampTileIndex];

                                	g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tileVisualizer.previewTransparency));

                                    if (tileVisualizer.collision) {

                                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                                        g2d.setColor(new Color(255, 192, 203));

                                        g2d.fillRect(x, y, tileSize, tileSize);

                                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tileVisualizer.previewTransparency));
                                    }

                                    g2d.drawImage(stampTileImage, x, y, tileSize, tileSize, null);

                                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                                }
                            }
                        }
                    } 
                    else {
                        int x = startX + col * (tileSize + spacing);

                        int y = startY + row * (tileSize + spacing);

                        if (tileVisualizer.selectedTileIndex >= 0 && tileVisualizer.selectedTileIndex < tileVisualizer.tileImages.length) {

                        	BufferedImage selectedTileImage = tileVisualizer.tileImages[tileVisualizer.selectedTileIndex];

                        	g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tileVisualizer.previewTransparency));

                            if (tileVisualizer.collision) {

                                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                                g2d.setColor(new Color(255, 192, 203));

                                g2d.fillRect(x, y, tileSize, tileSize);

                                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tileVisualizer.previewTransparency));
                            }

                            g2d.drawImage(selectedTileImage, x, y, tileSize, tileSize, null);

                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                        }
                    }
                }
            }
        }
    }

    private void createGrid(int width, int height) {

        gridWidth = width;

        gridHeight = height;

        int[][] newStampPattern = new int[height][width];

        for (int row = 0; row < height; row++) {

            for (int col = 0; col < width; col++) {

                newStampPattern[row][col] = 0;
            }
        }

        stampTool.addStampPattern(newStampPattern);

        currentStampIndex = stampTool.getAllStampPatterns().size() - 1;

        stampTool.setCurrentStampIndex(currentStampIndex);

        gridPanel.setPreferredSize(new Dimension(gridWidth * (tileSize + spacing), gridHeight * (tileSize + spacing)));

        gridPanel.revalidate();

        pack();

        setLocationRelativeTo(null);
    }

    private void loadStampPattern(int index) {

        if (index >= 0 && index < stampTool.getAllStampPatterns().size()) {

            currentStampIndex = index;

            gridPanel.repaint();

            pack();

            setLocationRelativeTo(null);
        } 
        else {

            JOptionPane.showMessageDialog(this, "Invalid stamp pattern index.");
        }
    }

    private void loadStamp() {

        int[][] loadedStamp = stampTool.getStampPattern(currentStampIndex);

        if (loadedStamp != null) {

            gridWidth = loadedStamp[0].length;

            gridHeight = loadedStamp.length;

            gridPanel.setPreferredSize(new Dimension(gridWidth * (tileSize + spacing), gridHeight * (tileSize + spacing)));

            gridPanel.revalidate();

            pack();

            setLocationRelativeTo(null);
        }
    }

    private BufferedImage createTranslucentImage(BufferedImage image, float alpha) {

        BufferedImage translucentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = translucentImage.createGraphics();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2d.drawImage(image, 0, 0, null);

        g2d.dispose();

        return translucentImage;
    }

    private void updateArrowButtonsState() {

        prevStampButton.setEnabled(stampTool.getAllStampPatterns().size() > 1);

        nextStampButton.setEnabled(stampTool.getAllStampPatterns().size() > 1);
    }
}