package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import menu.CustomFileChooserRenderer;
import menu.CustomFrameRenderer;
import menu.CustomLabelRenderer;
import menu.CustomMenuBarRenderer;
import menu.CustomMenuItemRenderer;
import menu.CustomMenuRenderer;
import menu.CustomOptionPaneRenderer;
import menu.CustomPanelRenderer;
import menu.CustomRadioButtonMenuItemRenderer;
import menu.CustomSliderRenderer;
import menu.CustomTextFieldRenderer;
import stamps.StampCreationTool;
import stamps.StampTool;

public class Main {
	

    public static void main(String[] args) {
    	
        SwingUtilities.invokeLater(() -> {
        	
            createAndShowGUI();
        });
    }
    
    public static void createAndShowGUI() {
    	
        CustomFrameRenderer frame = new CustomFrameRenderer("TheCasualWendigo's Tile Editor");
    	
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        TileVisualizer.setInitialFolders(TileVisualizer.getInitialFolders("res/tiles/"));

        String[] tilePaths = TileVisualizer.generateTilePaths(TileVisualizer.getInitialFolders());

        int[][] map = TileVisualizer.createMap(50, 50);
        
        TileVisualizer visualizer = new TileVisualizer(tilePaths, map, 0, 0, null);
        
        visualizer.setVisible(true);
        
    	setupMenus(frame, visualizer);
        
    	frame.add(visualizer);
    	
        frame.setVisible(true);
    }
    
    public static void setupMenus(CustomFrameRenderer frame, TileVisualizer visualizer) {
    	
    	frame.setBackground(new Color(211, 203, 190));

        StampTool stampTool = new StampTool();

        StampCreationTool stampCreationTool = new StampCreationTool(visualizer, stampTool, null);

        visualizer.stampTool = stampTool;

        visualizer.stampCreationTool = stampCreationTool;

        ContextMenu contextMenu = new ContextMenu(visualizer);

        visualizer.contextMenu = contextMenu;

        stampCreationTool.contextMenu = contextMenu;

        contextMenu.stampCreationTool = stampCreationTool;
        
        CustomMenuBarRenderer menuBar = new CustomMenuBarRenderer();

        menuBar.addMouseListener(new MouseAdapter() {
        	
            @Override
            public void mouseEntered(MouseEvent e) {

            	visualizer.isInMenuBar = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {

            	visualizer.isInMenuBar = false;
            }
        });
        
        CustomMenuRenderer fileMenu = new CustomMenuRenderer("File", 211, 203, 190);

        CustomMenuItemRenderer openMenuItem = new CustomMenuItemRenderer("Open Map");
        
        openMenuItem.addActionListener(e -> {
        	
            CustomFileChooserRenderer fileChooser = new CustomFileChooserRenderer(new File("res"));
            
            int returnValue = fileChooser.showOpenDialog(null);
            
            if (returnValue == CustomFileChooserRenderer.APPROVE_OPTION) {
            	
                File selectedFile = fileChooser.getSelectedFile();
                
                int[][] newMap = TileVisualizer.loadMapFromAbsolutePath(selectedFile.getAbsolutePath());
                
                if (newMap != null) {
                	
                    visualizer.mapFilePath = selectedFile.getAbsolutePath();
                    
                    visualizer.numCols = newMap[0].length;
                    
                    visualizer.numRows = newMap.length;
                    
                    visualizer.setMap(newMap);
                    
                    visualizer.setCurrentMapState(visualizer.copyMap(newMap));
                    
                    visualizer.setViewPosition(new Point(0, 0));
                    
                    visualizer.setZoomLevel(1);
                    
                    visualizer.isFileOpen = true;
                    
                    visualizer.repaint();
                } 
                else {
                	
                    CustomOptionPaneRenderer.showMessageDialog(null, "Error loading map from file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        fileMenu.add(openMenuItem);

        CustomMenuItemRenderer createMapMenuItem = new CustomMenuItemRenderer("Create Map");

        createMapMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	
                CustomTextFieldRenderer xField = new CustomTextFieldRenderer(5, 52, 37, 47, 255, 255, 255);
                
                CustomTextFieldRenderer yField = new CustomTextFieldRenderer(5, 52, 37, 47, 255, 255, 255);
                
                CustomPanelRenderer createMapPanel = new CustomPanelRenderer();

                createMapPanel.add(new CustomLabelRenderer("Enter map width:"));
                
                createMapPanel.add(xField);
                
                createMapPanel.add(Box.createHorizontalStrut(15));
                
                createMapPanel.add(new CustomLabelRenderer("Enter map height:"));
                
                createMapPanel.add(yField);

                int result = CustomOptionPaneRenderer.showConfirmDialog(null, createMapPanel, "Please enter X and Y values", CustomOptionPaneRenderer.OK_CANCEL_OPTION);

                if (result == CustomOptionPaneRenderer.OK_OPTION) {
                	
                    try {
                    	
                        int sizeX = Integer.parseInt(xField.getText());
                        
                        int sizeY = Integer.parseInt(yField.getText());
                        
                        visualizer.setMap(TileVisualizer.createMap(sizeX, sizeY));
                        
                        visualizer.setCurrentMapState(visualizer.copyMap(visualizer.getMap()));
                        
                        visualizer.numCols = sizeX;
                        
                        visualizer.numRows = sizeY;
                        
                        visualizer.isFileOpen = false;
                        
                        visualizer.setViewPosition(new Point(0, 0));
                        
                        visualizer.setZoomLevel(1);
                        
                        visualizer.repaint();
                        
                    } 
                    catch (NumberFormatException ex) {
                    	
                        CustomOptionPaneRenderer.showMessageDialog(null, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        fileMenu.add(createMapMenuItem);
        
        CustomMenuItemRenderer saveMenuItem = new CustomMenuItemRenderer("Save");
        
        saveMenuItem.addActionListener(e -> {
        	
            String filePath = (visualizer.isFileOpen && visualizer.mapFilePath != null) ? visualizer.mapFilePath : "res/maps/map0.txt";
            
            visualizer.saveMapToFile(filePath);
        });
        
        fileMenu.add(saveMenuItem);

        CustomMenuItemRenderer saveAsMenuItem = new CustomMenuItemRenderer("Save As");

        saveAsMenuItem.addActionListener(e -> {
        	
            CustomFileChooserRenderer fileChooser = new CustomFileChooserRenderer(new File("res"));
            
            int returnValue = fileChooser.showSaveDialog(null);
            
            if (returnValue == JFileChooser.APPROVE_OPTION) {
            	
                File selectedFile = fileChooser.getSelectedFile();
                
                visualizer.saveMapToFile(selectedFile.getAbsolutePath());
            }
        });

        fileMenu.add(saveAsMenuItem);

        CustomMenuItemRenderer exportMenuItem = new CustomMenuItemRenderer("Export Image");
        
        exportMenuItem.addActionListener(e -> {
        	
            exportMapAsImage(visualizer);
        });
        
        fileMenu.add(exportMenuItem);

        menuBar.add(fileMenu);

        CustomMenuRenderer viewMenu = new CustomMenuRenderer("View", 211, 203, 190);
        
        CustomMenuItemRenderer resetViewMenuItem = new CustomMenuItemRenderer("Reset View");
        
        resetViewMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.viewPosition = new Point(0,0);
                visualizer.zoomLevel = 1f;
                visualizer.repaint();
            }
        });
        
        viewMenu.add(resetViewMenuItem);
        
        CustomMenuItemRenderer resetPositionMenuItem = new CustomMenuItemRenderer("Reset Pos.");
        
        resetPositionMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.viewPosition = new Point(0,0);
                visualizer.repaint();
            }
        });
        
        viewMenu.add(resetPositionMenuItem);
        
        CustomMenuItemRenderer resetZoomMenuItem = new CustomMenuItemRenderer("Reset Zoom");
        
        resetZoomMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.zoomLevel = 1f;
                visualizer.repaint();
            }
        });
        
        viewMenu.add(resetZoomMenuItem);
        
        CustomMenuItemRenderer zoomInMenuItem = new CustomMenuItemRenderer("Zoom +");
        
        zoomInMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.zoomIn();
                visualizer.repaint();
            }
        });
        
        viewMenu.add(zoomInMenuItem);
        
        CustomMenuItemRenderer zoomOutMenuItem = new CustomMenuItemRenderer("Zoom -");
        
        zoomOutMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.zoomOut();
                visualizer.repaint();
            }
        });
        
        viewMenu.add(zoomOutMenuItem);
        
        menuBar.add(viewMenu);
        
        CustomMenuRenderer brushMenu = new CustomMenuRenderer("Brush", 211, 203, 190);

        CustomRadioButtonMenuItemRenderer previewMenuItem = new CustomRadioButtonMenuItemRenderer("Toggle Preview");

        previewMenuItem.setSelected(visualizer.showPreview);
        
        previewMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.showPreview = !visualizer.showPreview;
                
                visualizer.repaint();
                
                previewMenuItem.repaint();
            }
        });

        brushMenu.add(previewMenuItem);
        
        CustomRadioButtonMenuItemRenderer collisionMenuItem = new CustomRadioButtonMenuItemRenderer("Toggle Collision");

        collisionMenuItem.setSelected(visualizer.collision);
        
        collisionMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.collision = !visualizer.collision;
                
                if (visualizer.collision) {
                	
                	visualizer.selectedTileIndex = visualizer.selectedTileIndex + (visualizer.tilePaths.length / 2);
                	
                	visualizer.updateSelectedTileIcon();
                }
                else {
                	
                	visualizer.selectedTileIndex = visualizer.selectedTileIndex - (visualizer.tilePaths.length / 2);
                	
                	visualizer.updateSelectedTileIcon();
                }
            	
                visualizer.repaint();
                
                if (visualizer.subMenu != null) {
                	
                	visualizer.contextMenu.updateSubMenu(visualizer.contextMenu.currentFolder);                	
                }
                
                collisionMenuItem.repaint();
            }
        });

        brushMenu.add(collisionMenuItem);

        CustomRadioButtonMenuItemRenderer stampMenuItem = new CustomRadioButtonMenuItemRenderer("Toggle Stamp");
        
        stampMenuItem.setSelected(visualizer.stampToolActive);
        
        stampMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                if (!visualizer.stampTool.getAllStampPatterns().isEmpty()) {
                	
                    visualizer.stampToolActive = !visualizer.stampToolActive;
                    
                    visualizer.toggleStampTool();
                } 
                else {
                	
                    visualizer.stampCreationTool.setVisible(true);
                }
                
                stampMenuItem.setSelected(visualizer.stampToolActive);
                
                visualizer.repaint();
                
                stampMenuItem.repaint();
            }
        });
        
        brushMenu.add(stampMenuItem);

        CustomMenuItemRenderer openStampToolMenuItem = new CustomMenuItemRenderer("Open Stamp Creation Tool");
        
        openStampToolMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.stampCreationTool.setVisible(true);
            }
        });
        
        brushMenu.add(openStampToolMenuItem);

        CustomPanelRenderer transparencyPanel = new CustomPanelRenderer(new GridBagLayout(), 211, 203, 190);
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;

        CustomLabelRenderer transparencyLabel = new CustomLabelRenderer("Preview Transparency");
        
        transparencyLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        gbc.gridx = 0;
        
        gbc.gridy = 0;
        
        gbc.anchor = GridBagConstraints.WEST;
        
        transparencyPanel.add(transparencyLabel, gbc);

        CustomSliderRenderer transparencySlider = new CustomSliderRenderer(0, 100, 50); 
        
        transparencySlider.setMajorTickSpacing(50);
        
        transparencySlider.setPaintTicks(true); 
        
        transparencySlider.setPaintLabels(true);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        
        labelTable.put(0, new JLabel("0"));
        
        labelTable.put(50, new JLabel("50"));
        
        labelTable.put(100, new JLabel("100"));
        
        transparencySlider.setLabelTable(labelTable);

        transparencySlider.addChangeListener(new ChangeListener() {
        	
            @Override
            public void stateChanged(ChangeEvent e) {
            	
                int value = transparencySlider.getValue();
                
                visualizer.previewTransparency = (float) value / 100;
                
                visualizer.repaint();
            }
        });
        
        gbc.gridx = 0;
        
        gbc.gridy = 1;
        
        gbc.anchor = GridBagConstraints.CENTER;
        
        transparencyPanel.add(transparencySlider, gbc);

        brushMenu.add(transparencyPanel);

        menuBar.add(brushMenu);

        CustomMenuItemRenderer selectedTileMenuItem = new CustomMenuItemRenderer() {
        	
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
            	
                super.paint(g);

                String labelText;
                
                if (visualizer.collision) {
                    labelText = "Selected Tile: " + (visualizer.selectedTileIndex - visualizer.tilePaths.length / 2) + " Collision";
                }
                else {
                    labelText = "Selected Tile: " + visualizer.selectedTileIndex + " ";
                }
                
                BufferedImage iconImage = visualizer.getSelectedTileIcon();

                if (iconImage != null) {

                    FontMetrics fontMetrics = g.getFontMetrics();
                    
                    int textWidth = fontMetrics.stringWidth(labelText);
                    
                    int textX = 10;
                    
                    int textY = (getHeight() + fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;

                    g.drawString(labelText, textX, textY);

                    double scaleFactor = 2;
                    
                    int scaledIconWidth = (int) (iconImage.getWidth() * scaleFactor);
                    
                    int scaledIconHeight = (int) (iconImage.getHeight() * scaleFactor);
                    
                    int iconX = textX + textWidth + 5;
                    
                    int iconY = (getHeight() - scaledIconHeight) / 2;

                    g.drawImage(iconImage, iconX, iconY, scaledIconWidth, scaledIconHeight, null);
                }
            }

            @Override
            public Dimension getPreferredSize() {
            	
            	if (visualizer.collision) {
            		
                    String labelText = "Selected Tile: " + (visualizer.selectedTileIndex - visualizer.tilePaths.length) + "Collision";
                    
                    BufferedImage iconImage = visualizer.getSelectedTileIcon();
                    
                    FontMetrics fontMetrics = getFontMetrics(getFont());
                    
                    int textWidth = fontMetrics.stringWidth(labelText);
                    
                    int iconWidth = (iconImage != null) ? iconImage.getWidth() : 0;
                    
                    int totalWidth = textWidth + iconWidth + 20;
                    
                    return new Dimension(totalWidth, super.getPreferredSize().height);
            	}
            	else {
            		
                    String labelText = "Selected Tile: " + visualizer.selectedTileIndex + "Collision";
                    
                    BufferedImage iconImage = visualizer.getSelectedTileIcon();
                    
                    FontMetrics fontMetrics = getFontMetrics(getFont());
                    
                    int textWidth = fontMetrics.stringWidth(labelText);
                    
                    int iconWidth = (iconImage != null) ? iconImage.getWidth() : 0;
                    
                    int totalWidth = textWidth + iconWidth + 20;
                    
                    return new Dimension(totalWidth, super.getPreferredSize().height);
            	}
            }
        };

        menuBar.add(selectedTileMenuItem);

        contextMenu.selectedTileMenuItem = selectedTileMenuItem;
        
        stampCreationTool.selectedTileMenuItem = selectedTileMenuItem;
        
        visualizer.addMouseListener(new MouseAdapter() {
        	
            @Override
            public void mousePressed(MouseEvent e) {
            	
                visualizer.updateSelectedTileIcon();
                
                visualizer.repaint();
                
                selectedTileMenuItem.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            	
                visualizer.updateSelectedTileIcon();
                
                visualizer.repaint();	 
                
                selectedTileMenuItem.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            	
                visualizer.updateSelectedTileIcon();
                
                visualizer.repaint();
                
                selectedTileMenuItem.repaint();
            }
        });
        
        Action decrementAction = new AbstractAction() {
        	
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
            	
                if (visualizer.collision) {

                    visualizer.selectedTileIndex = (visualizer.selectedTileIndex - 1 + (visualizer.tilePaths.length / 2)) % visualizer.tilePaths.length;
                    
                    if (visualizer.selectedTileIndex < visualizer.tilePaths.length / 2) {
                        
                    	visualizer.selectedTileIndex += visualizer.tilePaths.length / 2;
                    }
                } 
                else {

                    visualizer.selectedTileIndex = (visualizer.selectedTileIndex - 1 + (visualizer.tilePaths.length / 2)) % (visualizer.tilePaths.length / 2);
                }

                visualizer.updateSelectedTileIcon();
                
                visualizer.repaint();
                
                selectedTileMenuItem.repaint();
            }
        };

        Action incrementAction = new AbstractAction() {
        	
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
            	
                if (visualizer.collision) {

                    visualizer.selectedTileIndex = (visualizer.selectedTileIndex + 1) % visualizer.tilePaths.length;
                    
                    if (visualizer.selectedTileIndex < visualizer.tilePaths.length / 2) {
                       
                    	visualizer.selectedTileIndex += visualizer.tilePaths.length / 2;
                    }
                } 
                else {

                    visualizer.selectedTileIndex = (visualizer.selectedTileIndex + 1) % (visualizer.tilePaths.length / 2);
                }

                visualizer.updateSelectedTileIcon();
                
                visualizer.repaint();
                
                selectedTileMenuItem.repaint();
            }
        };

        Action closeAction = new AbstractAction() {
        	
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
            	
                if (visualizer.subMenu != null) {
                	
                	visualizer.updateSelectedTileIcon();
                	
                    visualizer.repaint();
                	
                    visualizer.subMenu.setVisible(false);
                }
            }
        };

        Action undoAction = new AbstractAction() {
        	
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.undo();
            }
        };

        Action redoAction = new AbstractAction() {
        	
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.redo();
            }
        };

        visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "decrement");
        
        visualizer.getActionMap().put("decrement", decrementAction);
        
        visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "increment");
        
        visualizer.getActionMap().put("increment", incrementAction);
        
        visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "close");
        
        visualizer.getActionMap().put("close", closeAction);
        
        visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        
        visualizer.getActionMap().put("undo", undoAction);
        
        visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo");
        
        visualizer.getActionMap().put("redo", redoAction);

        frame.setJMenuBar(menuBar);
    }
    
    public static void exportMapAsImage(TileVisualizer visualizer) {
    	
        CustomFileChooserRenderer fileChooser = new CustomFileChooserRenderer();
        
        fileChooser.setDialogTitle("Export Map as Image");
        
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        int userSelection = fileChooser.showSaveDialog(null);
        
        if (userSelection == CustomFileChooserRenderer.APPROVE_OPTION) {
        	
            File selectedFile = fileChooser.getSelectedFile();
            
            String fileName = selectedFile.getName();
            
            String parentDir = selectedFile.getParent();

            File parentDirectory = new File(parentDir);
            
            if (!parentDirectory.exists() || !parentDirectory.isDirectory()) {
                
            	CustomOptionPaneRenderer.showMessageDialog(null, "Invalid directory.", "Error", JOptionPane.ERROR_MESSAGE);
                
                return;
            }

            String fullPath = parentDir + File.separator + fileName + ".png";

            if (new File(fullPath).exists()) {
            	
                int result = CustomOptionPaneRenderer.showConfirmDialog(null, "A file with the same name and type already exists. Do you want to overwrite it?", "File exists", JOptionPane.YES_NO_OPTION);

                if (result == CustomOptionPaneRenderer.YES_OPTION) {
                	
                    exportMapImage(selectedFile, visualizer, "png");
                }
            } 
            else {
            	
                exportMapImage(selectedFile, visualizer, "png");
            }
        }
    }

    private static void exportMapImage(File selectedFile, TileVisualizer visualizer, String extension) {
        
    	String filePath = selectedFile.getAbsolutePath();
    	
        BufferedImage mapImage = visualizer.getMapImage();

        try {
        	
            ImageIO.write(mapImage, extension, new File(filePath + "." + extension));
            
            CustomOptionPaneRenderer.showMessageDialog(null, "Map exported successfully as image.", "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } 
        catch (IOException ex) {
        	
            CustomOptionPaneRenderer.showMessageDialog(null, "Error exporting map as image: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}