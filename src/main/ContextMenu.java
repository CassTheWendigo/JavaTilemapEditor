package main;

import javax.swing.*;
import menu.CustomMenuItemRenderer;
import menu.CustomPanelRenderer;
import menu.CustomPopupMenuRenderer;
import menu.CustomScrollPaneRenderer;
import stamps.StampCreationTool;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class ContextMenu {

    private TileVisualizer tileVisualizer;
    
    private CustomPopupMenuRenderer contextMenu;
    
    private boolean subMenuCreated = false;
    
    CustomMenuItemRenderer selectedTileMenuItem;
    
    StampCreationTool stampCreationTool;
    
    String currentFolder;

    public ContextMenu(TileVisualizer tileVisualizer) {
    	
    	contextMenu = new CustomPopupMenuRenderer();
    	
        this.tileVisualizer = tileVisualizer;

        tileVisualizer.addMouseListener(createMouseListener());
        
        contextMenu.addMouseListener(createContextMenuMouseListener());
    }

    MouseAdapter createMouseListener() {
    	
        return new MouseAdapter() {
        	
            @Override
            public void mousePressed(MouseEvent e) {
            	
                if (SwingUtilities.isRightMouseButton(e)) {
                	
                    showContextMenu(e);
                }
            }
        };
    }

    MouseAdapter createContextMenuMouseListener() {
    	
    	return new MouseAdapter() {
    		
            @Override
            public void mouseEntered(MouseEvent e) {

            	tileVisualizer.isInContextMenu = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {

            	tileVisualizer.isInContextMenu = false;
            }
    	};
    }
    
    public void showContextMenu(MouseEvent e) {
    	
        int mouseX = e.getX();
        
        int mouseY = e.getY();

        int col = (mouseX - 20) / (int) (tileVisualizer.tileSize * tileVisualizer.getZoomLevel() + tileVisualizer.spacing);
        
        int row = (mouseY - 50) / (int) (tileVisualizer.tileSize * tileVisualizer.getZoomLevel() + tileVisualizer.spacing);

        if (!tileVisualizer.isInContextMenu && col >= 0 && col < tileVisualizer.numCols && row >= 0 && row < tileVisualizer.numRows) {

            contextMenu.removeAll();

            for (String folder : TileVisualizer.getInitialFolders()) {
            	
                CustomMenuItemRenderer folderMenuItem = new CustomMenuItemRenderer(folder);
                
                folderMenuItem.addActionListener(ev -> {

                });
                
                folderMenuItem.addMouseListener(new MouseAdapter() {
                	
                    @Override
                    public void mouseEntered(MouseEvent e) {
                    	
                        if (!tileVisualizer.isSubMenuVisible) 
                        {
                            if (!subMenuCreated) {
                            	
                                tileVisualizer.subMenu = createSubMenu();
                                
                                subMenuCreated = true;
                            }
                            
                            updateSubMenu(folder);
                            
                            Point p = folderMenuItem.getLocationOnScreen();
                            
                            tileVisualizer.subMenu.setLocation(p.x + folderMenuItem.getWidth(), p.y);
                            
                            tileVisualizer.subMenu.setVisible(true);
                            
                            tileVisualizer.isSubMenuVisible = true;
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    	
                        if (tileVisualizer.subMenu != null) {
                        	
                            tileVisualizer.updateSelectedTileIcon();
                            
                            tileVisualizer.subMenu.setVisible(false);
                            
                            tileVisualizer.isSubMenuVisible = false;
                        }
                    }
                });
                
                contextMenu.add(folderMenuItem);
            }

            Component sourceComponent = (Component) e.getSource();
            
            contextMenu.show(sourceComponent, e.getX(), e.getY());
        }
    }

    public void hideContextMenu() {
    	
        if (contextMenu != null && contextMenu.isVisible()) {
        	
            contextMenu.setVisible(false);

            tileVisualizer.repaint();
        }
    }

    CustomPopupMenuRenderer createSubMenu() {
    	
    	CustomPopupMenuRenderer subMenu = new CustomPopupMenuRenderer();
        
        subMenu.setPreferredSize(new Dimension(200, 300));
        
        CustomScrollPaneRenderer scrollPane = new CustomScrollPaneRenderer();
        
        subMenu.add(scrollPane);
        
        subMenu.addMouseListener(createContextMenuMouseListener());
        
        return subMenu;
    }

    void updateSubMenu(String folder) {
    	
        CustomScrollPaneRenderer scrollPane = (CustomScrollPaneRenderer) tileVisualizer.subMenu.getComponent(0);
        
        JViewport viewport = scrollPane.getViewport();
        
        viewport.removeAll();

        CustomPanelRenderer menuPanel = new CustomPanelRenderer();
       
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        Map<String, List<Integer>> folderMap = tileVisualizer.createFolderMap(tileVisualizer.tilePaths);
        
        List<Integer> indices = folderMap.get(folder);

        currentFolder = folder;
        
        if (indices != null) {

            int initialTileCount = tileVisualizer.tilePaths.length / 2;

            for (int index : indices) {
            	
                if (tileVisualizer.collision) {

                    if (index >= initialTileCount) {
                    	
                        String menuItemText = "Tile " + (index - (tileVisualizer.tilePaths.length / 2)) + " Collision";
                        
                        CustomMenuItemRenderer menuItem = new CustomMenuItemRenderer(menuItemText);

                        if (index >= 0 && index < tileVisualizer.tileImages.length && tileVisualizer.tileImages[index] != null) {
                            
                        	ImageIcon icon = new ImageIcon(tileVisualizer.tileImages[index].getScaledInstance(16, 16, Image.SCALE_DEFAULT));
                            
                        	menuItem.setIcon(icon);
                        } 
                        else {
                        	
                            System.err.println("No image found for index: " + index);
                        }

                        menuItem.addActionListener(ev -> {
                        	
                            tileVisualizer.selectedTileIndex = index;
                            
                            tileVisualizer.collision = true;
                            
                            tileVisualizer.updateSelectedTileIcon();
                            
                            selectedTileMenuItem.repaint();
                            
                            stampCreationTool.gridPanel.repaint();
                            
                            tileVisualizer.repaint();
                        });

                        menuPanel.add(menuItem);
                    }
                } 
                else {

                    if (index < initialTileCount) {
                    	
                        String menuItemText = "Tile " + index;
                        
                        CustomMenuItemRenderer menuItem = new CustomMenuItemRenderer(menuItemText);

                        if (index >= 0 && index < tileVisualizer.tileImages.length && tileVisualizer.tileImages[index] != null) {
                            
                        	ImageIcon icon = new ImageIcon(tileVisualizer.tileImages[index].getScaledInstance(16, 16, Image.SCALE_DEFAULT));
                            
                        	menuItem.setIcon(icon);
                        } 
                        else {
                        	
                            System.err.println("No image found for index: " + index);
                        }

                        menuItem.addActionListener(ev -> {
                        	
                            tileVisualizer.selectedTileIndex = index;
                            
                            tileVisualizer.collision = false;
                            
                            tileVisualizer.updateSelectedTileIcon();
                            
                            selectedTileMenuItem.repaint();
                            
                            stampCreationTool.gridPanel.repaint();
                            
                            tileVisualizer.repaint();
                        });

                        menuPanel.add(menuItem);
                    }
                }
            }
        }

        viewport.setView(menuPanel);
        
        scrollPane.revalidate();
        
        scrollPane.repaint();
    }
}