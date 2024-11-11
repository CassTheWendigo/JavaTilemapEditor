package menu;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CustomMenuRenderer extends JMenu {

    private static final long serialVersionUID = 1L;

    public CustomMenuRenderer(String text, int red, int green, int blue) {
    	
        super(text);
        
        setOpaque(true);
        
        setBackground(new Color(red, green, blue));
        
        setCustomFont();
    }

    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();

        if (getModel().isArmed() || getModel().isSelected() || getModel().isRollover()) {
        	
            g2d.setColor(Color.WHITE);
        } 
        else {
        	
            g2d.setColor(getBackground());
        }
        
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(new Color(52, 37, 47));

        FontMetrics fm = g.getFontMetrics();
        
        int textWidth = fm.stringWidth(getText());
        
        int textHeight = fm.getAscent();
        
        g2d.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 2);

        g2d.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
    	
        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setColor(new Color(52, 37, 47));
        
        g2d.setStroke(new BasicStroke(2));

        Container parent = getParent();
        
        if (parent instanceof CustomMenuBarRenderer) {
        	
        	CustomMenuBarRenderer menuBar = (CustomMenuBarRenderer) parent;
        	
            int index = menuBar.getComponentIndex(this);
            
            if (index == 0) {

                g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());  
            } 
            else if (index == menuBar.getComponentCount() - 1) {

                g2d.drawLine(0, 0, 0, getHeight());
            } 
            else if (index != 1) {

                g2d.drawLine(0, 0, 0, getHeight());
                
                g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
            }
        }

        g2d.dispose();
    }

    private void setCustomFont() {
    	
        try {
        	
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/EarlyGameboy.ttf")).deriveFont(12f);
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            
            ge.registerFont(customFont);
            setFont(customFont);
        } 
        catch (IOException | FontFormatException e) {
        	
            e.printStackTrace();
        }
    }
}
