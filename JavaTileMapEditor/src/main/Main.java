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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Hashtable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main {
	
    public static void main(String[] args) {
    	
        JFrame frame = new JFrame("TheCasualWendigo's Tile Editor");
    	
        TileVisualizer.setInitialFolders(TileVisualizer.getInitialFolders("res/tiles/"));

        String[] tilePaths = TileVisualizer.generateTilePaths(TileVisualizer.getInitialFolders());

        int[][] map = TileVisualizer.createMap(50, 50);

        TileVisualizer visualizer = new TileVisualizer(tilePaths, map, 0, 0, null);

        StampTool stampTool = new StampTool();

        StampCreationTool stampCreationTool = new StampCreationTool(visualizer, stampTool, null);

        visualizer.stampTool = stampTool;

        visualizer.stampCreationTool = stampCreationTool;

        ContextMenu contextMenu = new ContextMenu(visualizer);

        visualizer.contextMenu = contextMenu;

        stampCreationTool.contextMenu = contextMenu;

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
                    	
                        visualizer.setMap(newMap);
                        
                        visualizer.setCurrentMapState(visualizer.copyMap(newMap));
                        
                        visualizer.setViewPosition(new Point(0, 0));
                        
                        visualizer.setZoomLevel(1);
                        
                        visualizer.repaint();
                    } 
                    else {
                    	
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
            	
                JTextField xField = new JTextField(5);
                
                JTextField yField = new JTextField(5);

                JPanel createMapPanel = new JPanel();

                createMapPanel.add(new JLabel("Enter map width (x):"));
                
                createMapPanel.add(xField);
                
                createMapPanel.add(Box.createHorizontalStrut(15));
                
                createMapPanel.add(new JLabel("Enter map height (y):"));
                
                createMapPanel.add(yField);

                JOptionPane.showConfirmDialog(null, createMapPanel, "Please enter X and Y values", JOptionPane.OK_CANCEL_OPTION);

                try {
                	
                    int sizeX = Integer.parseInt(xField.getText());
                    
                    int sizeY = Integer.parseInt(yField.getText());
                    
                    visualizer.setMap(visualizer.createMap(sizeX, sizeY));
                    
                    visualizer.setCurrentMapState(visualizer.copyMap(visualizer.getMap()));
                    
                    visualizer.numCols = sizeX;
                    
                    visualizer.numRows = sizeY;
                    
                    visualizer.setViewPosition(new Point(0, 0));
                    
                    visualizer.setZoomLevel(1);
                    
                    visualizer.repaint();
                } 
                catch (NumberFormatException ex) {
                	
                    JOptionPane.showMessageDialog(null, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        fileMenu.add(createMapMenuItem);

        menuBar.add(fileMenu);

        JMenu brushMenu = new JMenu("Brush");

        JRadioButtonMenuItem previewMenuItem = new JRadioButtonMenuItem("Toggle Preview");

        previewMenuItem.setSelected(visualizer.showPreview);
        
        previewMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.showPreview = !visualizer.showPreview;
                
                visualizer.repaint();
            }
        });

        brushMenu.add(previewMenuItem);

        JRadioButtonMenuItem stampMenuItem = new JRadioButtonMenuItem("Toggle Stamp");
        
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
            }
        });
        
        brushMenu.add(stampMenuItem);

        JMenuItem openStampToolMenuItem = new JMenuItem("Open Stamp Creation Tool");
        
        openStampToolMenuItem.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.stampCreationTool.setVisible(true);
            }
        });
        
        brushMenu.add(openStampToolMenuItem);

        JPanel transparencyPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel transparencyLabel = new JLabel("Preview Transparency");
        
        transparencyLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        gbc.gridx = 0;
        
        gbc.gridy = 0;
        
        gbc.anchor = GridBagConstraints.WEST;
        
        transparencyPanel.add(transparencyLabel, gbc);

        JSlider transparencySlider = new JSlider(0, 100, 50); 
        
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

        JMenuItem selectedTileMenuItem = new JMenuItem() {
        	
            @Override
            public void paint(Graphics g) {
            	
                super.paint(g);

                String labelText = "Selected Tile: " + visualizer.selectedTileIndex + " ";
                
                BufferedImage iconImage = visualizer.getSelectedTileIcon();
                
                if (iconImage != null) {
                	
                    FontMetrics fontMetrics = g.getFontMetrics();
                    
                    int textWidth = fontMetrics.stringWidth(labelText);
                    
                    int totalWidth = textWidth + 10;
                    
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
            	
                String labelText = "Selected Tile: " + visualizer.selectedTileIndex + " ";
                
                BufferedImage iconImage = visualizer.getSelectedTileIcon();
                
                FontMetrics fontMetrics = getFontMetrics(getFont());
                
                int textWidth = fontMetrics.stringWidth(labelText);
                
                int iconWidth = iconImage != null ? iconImage.getWidth() : 0;
                
                int totalWidth = textWidth + iconWidth + 20;
                
                return new Dimension(totalWidth, super.getPreferredSize().height);
            }
        };

        visualizer.addMouseListener(new MouseAdapter() {
        	
            @Override
            public void mousePressed(MouseEvent e) {
            	
                visualizer.updateSelectedTileIcon();
                
                selectedTileMenuItem.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            	
                visualizer.updateSelectedTileIcon();
                
                selectedTileMenuItem.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            	
                visualizer.updateSelectedTileIcon();
                
                selectedTileMenuItem.repaint();
            }
        });

        Action decrementAction = new AbstractAction() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.selectedTileIndex = (visualizer.selectedTileIndex - 1 + tilePaths.length) % tilePaths.length;
                
                visualizer.updateSelectedTileIcon();
                
                selectedTileMenuItem.repaint();
            }
        };

        Action incrementAction = new AbstractAction() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                visualizer.selectedTileIndex = (visualizer.selectedTileIndex + 1) % tilePaths.length;
                
                visualizer.updateSelectedTileIcon();
                
                selectedTileMenuItem.repaint();
            }
        };

        Action closeAction = new AbstractAction() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                if (visualizer.subMenu != null) {
                	
                    visualizer.subMenu.setVisible(false);
                }
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

        menuBar.add(selectedTileMenuItem);

        visualizer.addPropertyChangeListener(new PropertyChangeListener() {
        	
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
            	
                if (evt.getPropertyName().equals("selectedTileIndex")) {
                	
                    String labelText = "Selected Tile: " + evt.getNewValue() + " ";
                    
                    selectedTileMenuItem.setText(labelText);
                    
                    visualizer.updateSelectedTileIcon();
                    
                    selectedTileMenuItem.repaint();
                    
                    visualizer.repaint();
                }
            }
        });

        frame.setJMenuBar(menuBar);
        
        visualizer.setBackground(new Color(40, 43, 48));
        
        frame.add(visualizer);
        
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
}