package menu;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CustomRadioButtonMenuItemRenderer extends JRadioButtonMenuItem {

	private static final long serialVersionUID = 1L;

    private final int checkmarkSize = 12;
    
    private final int checkmarkPadding = 5;
	
	public CustomRadioButtonMenuItemRenderer(String text) {
		
        super(text);
        
        setOpaque(true);
        
        setCustomFont();
    }

    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();

        if (getModel().isArmed() || getModel().isSelected()) {
        	
            g2d.setColor(new Color(255, 255, 255));
        } 
        else {
     
            g2d.setColor(new Color(211, 203, 190));
        }
        
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (getModel().isSelected()) {
        	
            g2d.setColor(new Color(52, 37, 47));
            
            int checkmarkSize = 12;
            
            int checkmarkX = getInsets().left;
            
            int checkmarkY = (getHeight() - checkmarkSize) / 2;
            
            drawCheckmark(g2d, checkmarkX, checkmarkY, checkmarkSize);
        }
        
        FontMetrics fm = g.getFontMetrics();

        int textX = getInsets().left + (isSelected() ? checkmarkSize + checkmarkPadding : 0);
        
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

        g2d.setColor(new Color(52, 37, 47));
        
        g2d.drawString(getText(), textX, textY);
        
        g2d.dispose();
    }
    
    private void drawCheckmark(Graphics2D g2d, int x, int y, int size) {
    	
        g2d.setStroke(new BasicStroke(2));
        
        g2d.drawLine(x, y + size / 2, x + size / 3, y + size);
        
        g2d.drawLine(x + size / 3, y + size, x + size, y);
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