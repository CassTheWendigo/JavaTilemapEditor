package menu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomDesktopPaneRenderer extends JDesktopPane {

    private static final long serialVersionUID = 1L;

    public CustomDesktopPaneRenderer() {
    	
        initComponents();
    }

    private void initComponents() {
    	
        setBorder(new EmptyBorder(10, 10, 10, 10));

        setBackground(new Color(240, 240, 240));
    }

    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
    }
}