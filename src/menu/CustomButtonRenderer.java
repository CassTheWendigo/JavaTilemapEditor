package menu;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CustomButtonRenderer extends JButton {

	private static final long serialVersionUID = 1L;

	public CustomButtonRenderer(String text) {
		
        super(text);
        
        setOpaque(true);
        
        setBackground(new Color(211, 203, 190));
        
        setCustomFont();
    }

    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();

        if (getModel().isArmed() || getModel().isSelected() || getModel().isRollover()) {
        	
            g2d.setColor(new Color(255, 255, 255));
        } 
        else {
        	
            g2d.setColor(getBackground());
        }
        
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(new Color(52, 37, 47));
        
        FontMetrics fm = g2d.getFontMetrics();
        
        int textWidth = fm.stringWidth(getText());
        
        int textHeight = fm.getAscent();

        int x = (getWidth() - textWidth) / 2;
        
        int y = (getHeight() + textHeight) / 2 - fm.getDescent();

        g2d.drawString(getText(), x, y);

        g2d.dispose();
    }
    
    @Override
    protected void paintBorder(Graphics g) {
    	
        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setColor(getModel().isArmed() || getModel().isSelected() ? Color.WHITE : Color.BLACK); // Border color
        
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
       
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