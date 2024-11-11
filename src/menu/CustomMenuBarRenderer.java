package menu;

import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.basic.BasicMenuBarUI;

public class CustomMenuBarRenderer extends JMenuBar {

    private static final long serialVersionUID = 1L;

    public CustomMenuBarRenderer() {
    	
        setOpaque(true);
        
        setBackground(new Color(52, 37, 47));
        
        setUI(new CustomMenuBarUI());
    }

    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setColor(new Color(211, 203, 190));
        
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
    	
        super.paintBorder(g);

        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setColor(new Color(52, 37, 47));
        
        g2d.setStroke(new BasicStroke(2));
        
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        
        g2d.dispose();
    }

    private static class CustomMenuBarUI extends BasicMenuBarUI {
    	
        @Override
        public void installUI(JComponent c) {
        	
            super.installUI(c);
            
            c.setOpaque(true);
            
            c.setBackground(new Color(40, 43, 48));
        }

        @Override
        public void update(Graphics g, JComponent c) {
        	
            paint(g, c);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
        	
            Graphics2D g2d = (Graphics2D) g.create();
            
            g2d.setColor(new Color(211, 203, 190));
            
            g2d.fillRect(0, 0, c.getWidth(), c.getHeight());
            
            g2d.dispose();
            
            super.paint(g, c);
        }
    }
}
