package main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;

public class StampCreationTool extends JFrame {
 
	private static final long serialVersionUID = 1L;

	private int gridWidth;

    private int gridHeight;

    private int tileSize = 50;

    private int spacing = 5;

    private TileVisualizer tileVisualizer;

    private JPanel gridPanel;

    private JButton nextStampButton;

    private JButton prevStampButton;

    ContextMenu contextMenu;

    private StampTool stampTool;

    private int currentStampIndex;

    private BufferedImage[] translucentTileImages;

    public StampCreationTool(TileVisualizer tileVisualizer, StampTool stampTool, ContextMenu contextMenu) {
        this.tileVisualizer = tileVisualizer;

        this.stampTool = stampTool;

        this.currentStampIndex = stampTool.getCurrentStampIndex();

        this.contextMenu = contextMenu;

        initializeUI();

        setLocationRelativeTo(null);

        this.translucentTileImages = new BufferedImage[tileVisualizer.tileImages.length];

        for (int i = 0; i < tileVisualizer.tileImages.length; i++) {
            this.translucentTileImages[i] = createTranslucentImage(tileVisualizer.tileImages[i], tileVisualizer.previewTransparency); // Adjust alpha as needed
        }

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "decrement");

        getRootPane().getActionMap().put("decrement", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
            	
                tileVisualizer.selectedTileIndex = (tileVisualizer.selectedTileIndex - 1 + tileVisualizer.tilePaths.length) % tileVisualizer.tilePaths.length;

                tileVisualizer.updateSelectedTileIcon();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "increment");

        getRootPane().getActionMap().put("increment", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
            	
                tileVisualizer.selectedTileIndex = (tileVisualizer.selectedTileIndex + 1) % tileVisualizer.tilePaths.length;

                tileVisualizer.updateSelectedTileIcon();
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

    private void initializeUI() {
        setTitle("Stamp Creation Tool");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel controlPanel = new JPanel();

        JTextField widthField = new JTextField(3);

        JTextField heightField = new JTextField(3);

        JButton createGridButton = new JButton("Create Grid");

        JButton saveStampButton = new JButton("Save Stamp");

        nextStampButton = new JButton(">");

        prevStampButton = new JButton("<");

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
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid integers for grid size.");
            }
        });

        saveStampButton.addActionListener(e -> {
        	
            saveStamp();

            loadStamp();
        });

        controlPanel.add(new JLabel("Width:"));

        controlPanel.add(widthField);

        controlPanel.add(new JLabel("Height:"));

        controlPanel.add(heightField);
        controlPanel.add(createGridButton);

        controlPanel.add(saveStampButton);

        controlPanel.add(prevStampButton);

        controlPanel.add(nextStampButton);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        gridPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, spacing, spacing));

        gridPanel.setBackground(new Color(40, 43, 48));

        mainPanel.add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        mainPanel.addMouseListener(new MouseAdapter() {
        	
            @Override
            public void mousePressed(MouseEvent e) {
            	
                mainPanel.requestFocusInWindow();
            }
        });

        add(mainPanel);

        pack();
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

        gridPanel.removeAll();

        gridPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(spacing, spacing, spacing, spacing);

        for (int row = 0; row < height; row++) {
        	
            for (int col = 0; col < width; col++) {
            	
                JLabel tileLabel = new JLabel();

                tileLabel.setPreferredSize(new Dimension(tileSize, tileSize));

                tileLabel.setIcon(new ImageIcon(tileVisualizer.tileImages[0].getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT)));

                int finalRow = row;

                int finalCol = col;

                tileLabel.addMouseListener(new MouseAdapter() {
                	
                    @Override
                    public void mousePressed(MouseEvent e) {
                    	
                        if (SwingUtilities.isLeftMouseButton(e)) {
                        	
                            int[][] currentStamp = stampTool.getStampPattern(currentStampIndex);

                            currentStamp[finalRow][finalCol] = tileVisualizer.selectedTileIndex;

                            tileLabel.setIcon(new ImageIcon(tileVisualizer.tileImages[tileVisualizer.selectedTileIndex].getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT)));

                            gridPanel.repaint();
                            
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                        	
                            contextMenu.showContextMenu(e);
                        }
                    }
                });

                tileLabel.addMouseListener(new MouseAdapter() {
                	
                    @Override
                    public void mouseEntered(MouseEvent e) {
                    	
                        tileLabel.setIcon(new ImageIcon(translucentTileImages[tileVisualizer.selectedTileIndex].getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT)));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    	
                        int[][] currentStamp = stampTool.getStampPattern(currentStampIndex);

                        tileLabel.setIcon(new ImageIcon(tileVisualizer.tileImages[currentStamp[finalRow][finalCol]].getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT)));
                    }
                });

                gbc.gridx = col;

                gbc.gridy = row;

                gbc.anchor = GridBagConstraints.CENTER;

                gridPanel.add(tileLabel, gbc);
            }
        }

        gridPanel.revalidate();

        gridPanel.repaint();

        pack();
    }

    private void saveStamp() {
    	
        try {
        	
            int[][] currentStamp = stampTool.getStampPattern(currentStampIndex);

            for (int y = 0; y < currentStamp.length; y++) {
            	
                for (int x = 0; x < currentStamp[0].length; x++) {
                	
                    if (currentStamp[y][x] == -1) {
                    	
                        currentStamp[y][x] = 0;
                    }
                }
            }

            stampTool.updateStampPattern(currentStampIndex, currentStamp);

            JOptionPane.showMessageDialog(this, "Stamp saved successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(this, "Failed to save stamp.");
        }
    }

    private void loadStamp() {
    	
        List<int[][]> allStampPatterns = stampTool.getAllStampPatterns();

        if (!allStampPatterns.isEmpty()) {
        	
            loadStampPattern(currentStampIndex);

            updateArrowButtonsState();
            
        } else {
        	
            JOptionPane.showMessageDialog(this, "No stamp saved yet.");
        }
    }

    private void loadStampPattern(int index) {
    	
        List<int[][]> allStampPatterns = stampTool.getAllStampPatterns();

        if (!allStampPatterns.isEmpty() && index >= 0 && index < allStampPatterns.size()) {
        	
            int[][] savedStamp = allStampPatterns.get(index);

            gridWidth = savedStamp[0].length;

            gridHeight = savedStamp.length;

            gridPanel.removeAll();

            gridPanel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(spacing, spacing, spacing, spacing);

            for (int y = 0; y < gridHeight; y++) {
            	
                for (int x = 0; x < gridWidth; x++) {
                	
                    JLabel tileLabel = new JLabel();

                    tileLabel.setPreferredSize(new Dimension(tileSize, tileSize));

                    tileLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                    if (savedStamp[y][x] != -1) {
                    	
                        tileLabel.setIcon(new ImageIcon(tileVisualizer.tileImages[savedStamp[y][x]].getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT)));
                    } 
                    else {
                    	
                        tileLabel.setIcon(null);
                    }

                    int finalRow = y;

                    int finalCol = x;

                    tileLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                        	
                            if (SwingUtilities.isLeftMouseButton(e)) {
                            	
                                int[][] currentStamp = stampTool.getStampPattern(currentStampIndex);

                                currentStamp[finalRow][finalCol] = tileVisualizer.selectedTileIndex;

                                tileLabel.setIcon(new ImageIcon(tileVisualizer.tileImages[tileVisualizer.selectedTileIndex].getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT)));

                                gridPanel.repaint();
                            } 
                            else if (SwingUtilities.isRightMouseButton(e)) {
                            	
                                contextMenu.showContextMenu(e);
                            }
                        }
                    });

                    gbc.gridx = x;

                    gbc.gridy = y;

                    gbc.anchor = GridBagConstraints.CENTER;

                    gridPanel.add(tileLabel, gbc);
                }
            }

            gridPanel.revalidate();

            gridPanel.repaint();

            pack();
        } 
        else {
        	
            JOptionPane.showMessageDialog(this, "No stamp pattern found at index " + index);
        }
    }

    private BufferedImage createTranslucentImage(BufferedImage image, float alpha) {
    	
        int width = image.getWidth();

        int height = image.getHeight();

        // Create a BufferedImage with alpha channel
        BufferedImage translucentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Set alpha composite for the translucent effect
        Graphics2D g2d = translucentImage.createGraphics();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2d.drawImage(image, 0, 0, null);

        g2d.dispose();

        return translucentImage;
    }

    private void updateArrowButtonsState() {
    	
        nextStampButton.setEnabled(stampTool.getAllStampPatterns().size() > 1);

        prevStampButton.setEnabled(stampTool.getAllStampPatterns().size() > 1);
    }
}