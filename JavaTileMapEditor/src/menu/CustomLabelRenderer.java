package menu;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CustomLabelRenderer extends JLabel {

	private static final long serialVersionUID = 1L;

    public CustomLabelRenderer() {
    	
        setCustomFont();
        
        setBackground(new Color(211, 203, 190));
	}
	
	public CustomLabelRenderer(String text) {
		
        super(text);
        
        setForeground(new Color(52, 37, 47));
        
        setBackground(new Color(211, 203, 190));
        
        setOpaque(true);
        
        setCustomFont();
    }
    
	@Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
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