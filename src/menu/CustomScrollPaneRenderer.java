package menu;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class CustomScrollPaneRenderer extends JScrollPane {

    private static final long serialVersionUID = 1L;

    public CustomScrollPaneRenderer(CustomViewportRenderer view) {
    	
        super(view);
        
        initialize();
    }

    public CustomScrollPaneRenderer() {
    	
        super();
        
        initialize();
    }

    public CustomScrollPaneRenderer(CustomPanelRenderer centerPanel) {
    	
        super(centerPanel);
        
        initialize();
    }

    private void initialize() {
    	
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        customizeScrollBar(getVerticalScrollBar());
        
        customizeScrollBar(getHorizontalScrollBar());
    }

    private void customizeScrollBar(JScrollBar scrollBar) {
    	
        scrollBar.setUI(new CustomScrollBarUI());
        
        scrollBar.setPreferredSize(new Dimension(12, 0));
    }

    private static class CustomScrollBarUI extends BasicScrollBarUI {
    	
        private final Color thumbColor = new Color(52, 37, 47);
        
        @Override
        protected void configureScrollBarColors() {
        	
            super.configureScrollBarColors();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        	
            g.setColor(thumbColor);
            
            g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
        	
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
        	
            return createZeroButton();
        }

        private JButton createZeroButton() {
        	
            JButton button = new JButton();
            
            Dimension zeroDim = new Dimension(0, 0);
            
            button.setPreferredSize(zeroDim);
            
            button.setMinimumSize(zeroDim);
            
            button.setMaximumSize(zeroDim);
            
            return button;
        }
    }
}