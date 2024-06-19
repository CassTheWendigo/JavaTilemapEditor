package menu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomViewportRenderer extends JViewport {

    private static final long serialVersionUID = 1L;

    public CustomViewportRenderer(Component view) {
    	
        super();

        setView(view);

        setOpaque(true);
        
        setBackground(new Color(240, 240, 240));
        
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }
}