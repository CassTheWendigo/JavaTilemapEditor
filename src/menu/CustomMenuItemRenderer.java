package menu;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

public class CustomMenuItemRenderer extends JMenuItem {

    public CustomMenuItemRenderer(String text) {
    	
        super(text);
        
        setBackground(new Color(211, 203, 190));
        
        init();
    }

    public CustomMenuItemRenderer() {
    	
        super();
        
        init();
    }

    private void init() {
    	
        setPreferredSize(new Dimension(100, 25));
        
        setBorderPainted(false);
        
        setFocusPainted(false);
        
        setContentAreaFilled(false);

        Container parent = getParent();
        
        float size = (parent instanceof CustomMenuBarRenderer) ? 12f : 10f;
        
        setCustomFont(size);
    }

    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);

        if (isSelected() || getModel().isArmed() || getModel().isRollover()) {
        	
            g.setColor(new Color(255, 255, 255));
        } 
        else {
        	
            g.setColor(new Color(211, 203, 190));
        }
        
        g.fillRect(0, 0, getWidth(), getHeight());

        Icon icon = getIcon();
        
        if (icon != null) {
        	
            int iconX = 5;
            
            int iconY = (getHeight() - icon.getIconHeight()) / 2;
            
            icon.paintIcon(this, g, iconX, iconY);
            
            iconX += icon.getIconWidth() + 5;
        }

        g.setColor(new Color(52, 37, 47));
        
        FontMetrics fm = g.getFontMetrics();
        
        int textHeight = fm.getAscent();
        
        int x = (icon != null) ? icon.getIconWidth() + 15 : 5;
        
        int y = (getHeight() + textHeight) / 2 - 3;
        
        g.drawString(getText(), x, y);
    }

    private void setCustomFont(float size) {
        try {
        	
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/EarlyGameboy.ttf")).deriveFont(size);
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            
            ge.registerFont(customFont);
            setFont(customFont);
        } 
        catch (IOException | FontFormatException e) {
        	
            e.printStackTrace();
        }
    }
}
