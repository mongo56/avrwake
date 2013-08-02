package avrwake.gui.element;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class HyperlinkLabel extends JLabel implements MouseListener {

    private static final long serialVersionUID = 5167616594614061634L;
    private URL url = null;

    public HyperlinkLabel() {
        super();
        addMouseListener(this);
    }
    
    public HyperlinkLabel(String label) {
        super(label);
        addMouseListener(this);
    }

    public HyperlinkLabel(String label, URL url) {
        this(label);
        this.url = url;
        setText("<html><a href=\"\">" + label + "</a></html>");
        setToolTipText("Go to: " + url.getRef());
    }

    public HyperlinkLabel(String label, String tip, URL url) {
        this(label, url);
        setToolTipText(tip);
    }
    
    public void setLabel(String text) {
        super.setText(text);
    }

    public void setURL(URL url) {
        this.url = url;
    }

    public URL getURL() {
        return url;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        HyperlinkLabel self = (HyperlinkLabel) e.getSource();
        if (self.url == null) {
            return;
        }
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(url.toURI());
                    return;
                } catch (Exception exp) {
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Cannot launch browser...\n Please, visit\n" + url.getRef(), "", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
    }
}