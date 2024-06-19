package menu;

import javax.swing.*;
import java.awt.*;

public class CustomInternalFrameRenderer extends JInternalFrame {

    private static final long serialVersionUID = 1L;

    public CustomInternalFrameRenderer(String title) {
    	
        super(title, true, true, true, true);
        
        initComponents();
    }

    public CustomInternalFrameRenderer() {

    	super("", true, true, true, true);
    	
    	initComponents();
	}

	private void initComponents() {
    	
        setSize(300, 200);
        
        setLocation(100, 100);
        
        setLayout(new BorderLayout());

        JLabel label = new JLabel("This is a custom internal frame.");
        
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(label, BorderLayout.CENTER);
    }

    @Override
    public void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
    }

    @Override
    public void updateUI() {

        super.updateUI();
    }
}