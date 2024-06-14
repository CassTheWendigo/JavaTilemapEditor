package main;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class ContextMenu {

    private TileVisualizer tileVisualizer;
    
    private JPopupMenu contextMenu;
    
    private boolean subMenuCreated = false;

    public ContextMenu(TileVisualizer tileVisualizer) {
    	
        this.tileVisualizer = tileVisualizer;
        
        contextMenu = new JPopupMenu();
        
        contextMenu.addPopupMenuListener(new PopupMenuListener() {
        	
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            	
                tileVisualizer.isContextMenuVisible = true;
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            	
                tileVisualizer.isContextMenuVisible = false;
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            	
                tileVisualizer.isContextMenuVisible = false;
            }
        });

        // Add mouse listener to handle mouse events for showing context menu
        tileVisualizer.addMouseListener(createMouseListener());
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

    public void showContextMenu(MouseEvent e) {
    	
        int mouseX = e.getX();
        
        int mouseY = e.getY();

        int col = (mouseX - 20) / (int) (tileVisualizer.tileSize * tileVisualizer.getZoomLevel() + tileVisualizer.spacing);
        
        int row = (mouseY - 50) / (int) (tileVisualizer.tileSize * tileVisualizer.getZoomLevel() + tileVisualizer.spacing);

        if (!tileVisualizer.isContextMenuVisible && col >= 0 && col < tileVisualizer.numCols && row >= 0 && row < tileVisualizer.numRows) {

            contextMenu.removeAll();

            Map<String, List<Integer>> folderMap = tileVisualizer.createFolderMap(tileVisualizer.tilePaths);
            
            for (String folder : tileVisualizer.getInitialFolders()) {
            	
                JMenuItem folderMenuItem = new JMenuItem(folder);
                
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
            
            tileVisualizer.isContextMenuVisible = false;
            
            tileVisualizer.repaint();
        }
    }

    JPopupMenu createSubMenu() {
    	
        JPopupMenu subMenu = new JPopupMenu();
        
        subMenu.setPreferredSize(new Dimension(200, 300));
        
        JScrollPane scrollPane = new JScrollPane();
        
        subMenu.add(scrollPane);
        
        return subMenu;
    }

    void updateSubMenu(String folder) {
    	
        JViewport viewport = ((JScrollPane) tileVisualizer.subMenu.getComponent(0)).getViewport();
        
        viewport.removeAll();

        Map<String, List<Integer>> folderMap = tileVisualizer.createFolderMap(tileVisualizer.tilePaths);
        
        List<Integer> indices = folderMap.get(folder);

        if (indices != null) {
        	
            JPanel menuPanel = new JPanel();
            
            menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

            for (int index : indices) {
            	
                JMenuItem menuItem = new JMenuItem("Tile " + index);
                
                menuItem.setIcon(new ImageIcon(tileVisualizer.tileImages[index].getScaledInstance(16, 16, Image.SCALE_DEFAULT)));
                
                menuItem.addActionListener(ev -> {
                	
                    tileVisualizer.selectedTileIndex = index;
                    
                    tileVisualizer.updateSelectedTileIcon();
                    
                    tileVisualizer.repaint();
                });
                
                menuPanel.add(menuItem);
            }

            JScrollPane scrollPane = (JScrollPane) tileVisualizer.subMenu.getComponent(0);
            
            scrollPane.setViewportView(menuPanel);
        }
    }
}