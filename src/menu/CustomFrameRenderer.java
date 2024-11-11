package menu;

import javax.swing.*;
import java.awt.*;

public class CustomFrameRenderer extends JFrame {

    private static final long serialVersionUID = 1L;

    public CustomFrameRenderer(String title) {
    	
        super(title);
        
        initUI();
    }

    public CustomFrameRenderer() {

	}

	private void initUI() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());

        setBackground(new Color(52, 37, 47));
        
        pack();
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}