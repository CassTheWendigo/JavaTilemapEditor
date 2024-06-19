package menu;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CustomFileChooserRenderer extends JFileChooser {

    private static final long serialVersionUID = 1L;

    public CustomFileChooserRenderer(File file) {
        super(file);
    }

    public CustomFileChooserRenderer() {
        super();
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        dialog.setBackground(new Color(255, 255, 200));
        return dialog;
    }

    @Override
    public void approveSelection() {
        File selectedFile = getSelectedFile();
        if (selectedFile.exists()) {
            int result = CustomOptionPaneRenderer.showConfirmDialog(this,
                    "The file already exists. Do you want to overwrite it?", "File exists",
                    JOptionPane.YES_NO_OPTION);

            if (result == CustomOptionPaneRenderer.YES_OPTION) {
                super.approveSelection();
            }
        } else {
            super.approveSelection();
        }
    }

    // Helper method to check if the selected file already exists with the given extension
    public boolean fileExistsWithExtension(String extension) {
        File selectedFile = getSelectedFile();
        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            String extensionWithDot = "." + extension.toLowerCase();
            return new File(filePath + extensionWithDot).exists();
        }
        return false;
    }
}