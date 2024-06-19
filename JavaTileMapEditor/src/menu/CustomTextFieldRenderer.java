package menu;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CustomTextFieldRenderer extends JTextField {

    private static final long serialVersionUID = 1L;

    public CustomTextFieldRenderer(String text) {
    	
        super(text);
        
        setOpaque(true);
        
        setCustomFont();
        
        setHorizontalAlignment(JTextField.CENTER);
    }

    public CustomTextFieldRenderer(int length) {
    	
        super(length);
        
        setOpaque(true);
        
        setCustomFont();
        
        setHorizontalAlignment(JTextField.CENTER);
    }

    public CustomTextFieldRenderer(int length, int backgroundRed, int backgroundGreen, int backgroundBlue, int foregroundRed, int foregroundGreen, int foregroundBlue) {
        
    	super(length);
        
        setBackground(new Color(backgroundRed, backgroundGreen, backgroundBlue));
        
        setForeground(new Color(foregroundRed, foregroundGreen, foregroundBlue));
        
        setCustomFont();
        
        setOpaque(true);
        
        setHorizontalAlignment(JTextField.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
    	
        super.paintBorder(g);
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
