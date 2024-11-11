package menu;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CustomPanelRenderer extends JPanel {

	private static final long serialVersionUID = 1L;

	public CustomPanelRenderer(int red, int green, int blue) {
		
        setOpaque(true);
        
		setBackground(new Color(red, green, blue));

		try {
			
			Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/EarlyGameboy.ttf")).deriveFont(12f);
		
			setFont(customFont);		
		}
        catch (FontFormatException e) {

			e.printStackTrace();
		} 
        catch (IOException e) {
	
			e.printStackTrace();
		}
    }

	public CustomPanelRenderer(BorderLayout borderLayout, int red, int green, int blue) {
		
		super(borderLayout);
		
		setBackground(new Color(red, green, blue));
	}

	public CustomPanelRenderer(GridBagLayout gridBagLayout, int red, int green, int blue) {

		super(gridBagLayout);
		
		setBackground(new Color(red, green, blue));
	}

	public CustomPanelRenderer(FlowLayout flowLayout) {
		
		super(flowLayout);
		
    	setBackground(new Color(211, 203, 190));
	}

	public CustomPanelRenderer(BorderLayout borderLayout) {
		
		super(borderLayout);
		
		setBackground(new Color(211, 203, 190));
	}
	
	public CustomPanelRenderer() {
		
		super();
		
		setBackground(new Color(211, 203, 190));
	}

	@Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(getBackground());
        
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.dispose();
    }
}