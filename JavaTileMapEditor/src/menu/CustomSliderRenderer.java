package menu;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

public class CustomSliderRenderer extends JSlider {

    private static final long serialVersionUID = 1L;

    public CustomSliderRenderer() {
        setUI(new CustomSliderUI(this));
    }

    public CustomSliderRenderer(int min, int max, int value) {
    	
        super(min, max, value);
        
        setUI(new CustomSliderUI(this));
        
        setBackground(new Color(211, 203, 190));
    }

    private static class CustomSliderUI extends BasicSliderUI {

        public CustomSliderUI(JSlider slider) {
        	
            super(slider);
        }

        @Override
        public void paintTrack(Graphics g) {
        	
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setColor(new Color(150, 150, 150));
            
            g2d.fillRect(trackRect.x, trackRect.y + (trackRect.height / 2) - 2, trackRect.width, 4);
        }

        @Override
        public void paintThumb(Graphics g) {
        	
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setColor(new Color(52, 37, 47));
            
            g2d.fillOval(thumbRect.x - (thumbRect.height / 32), 5, thumbRect.height / 2, thumbRect.height / 2);
        }
    }
}
