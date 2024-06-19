package menu;

import javax.swing.*;
import java.awt.*;

public class CustomOptionPaneRenderer extends JOptionPane {

    private static final long serialVersionUID = 1L;

    public CustomOptionPaneRenderer() {
    	
        setBackground(new Color(211, 203, 190));
    }

    @Override
    public void paintComponent(Graphics g) {
    	
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setColor(getBackground());
        
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.dispose();
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType) {
        
    	CustomOptionPaneRenderer optionPane = new CustomOptionPaneRenderer();
        
    	optionPane.setMessage(message);
        
    	optionPane.setOptionType(optionType);

        CustomButtonRenderer okButton = new CustomButtonRenderer("Confirm");
        
        okButton.addActionListener(e -> {
        	
            optionPane.setValue(JOptionPane.OK_OPTION);
            
            JDialog dialog = (JDialog) SwingUtilities.getWindowAncestor(okButton);
            
            dialog.dispose();
        });

        CustomButtonRenderer cancelButton = new CustomButtonRenderer("Cancel");
        
        cancelButton.addActionListener(e -> {
        	
            optionPane.setValue(JOptionPane.CANCEL_OPTION);
            
            JDialog dialog = (JDialog) SwingUtilities.getWindowAncestor(cancelButton);
            
            dialog.dispose();
        });

        Object[] options = {okButton, cancelButton};
        
        optionPane.setOptions(options);
        
        optionPane.setIcon(null);

        JDialog dialog = optionPane.createDialog(parentComponent, title);
        
        dialog.getContentPane().setBackground(new Color(211, 203, 190));
        
        recursivelySetBackground(dialog.getContentPane(), new Color(211, 203, 190));

        dialog.setVisible(true);

        Object selectedValue = optionPane.getValue();
        if (selectedValue == null) {
            return JOptionPane.CLOSED_OPTION;
        }
        if (selectedValue instanceof Integer) {
            return (Integer) selectedValue;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    private static void recursivelySetBackground(Container parent, Color background) {
        for (Component c : parent.getComponents()) {
            if (c instanceof Container) {
                if (!(c instanceof CustomTextFieldRenderer)) {
                    recursivelySetBackground((Container) c, background);
                }
            }
            if (!(c instanceof CustomTextFieldRenderer)) {
                c.setBackground(background);
            }
        }
    }

    public static final int YES_OPTION = JOptionPane.YES_OPTION;
    
    public static final int NO_OPTION = JOptionPane.NO_OPTION;
    
    public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
    
    public static final int OK_OPTION = JOptionPane.OK_OPTION;
}