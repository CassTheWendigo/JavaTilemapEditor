package menu;

import javax.swing.*;
import javax.swing.plaf.basic.BasicPopupMenuUI;

import java.awt.*;

public class CustomPopupMenuRenderer extends JPopupMenu {

    private static final long serialVersionUID = 1L;

    public CustomPopupMenuRenderer() {
    	
        super();
        
        setUI(new CustomPopupMenuUI());
    }

    private static class CustomPopupMenuUI extends BasicPopupMenuUI {
    	
        @Override
        public void installUI(JComponent c) {
        	
            super.installUI(c);
            
            popupMenu.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            
            popupMenu.setBackground(new Color(240, 240, 240));
        }

        @Override
        public void paint(Graphics g, JComponent c) {
        	
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(c.getBackground());
            
            g2d.fillRect(0, 0, c.getWidth(), c.getHeight());

            super.paint(g, c);
        }
    }
}