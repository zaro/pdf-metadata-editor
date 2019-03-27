package pmedit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

public class FileDropMessage extends JComponent {
	
	static String openFilesText = "Add file(s)";
	
	protected void paintComponent(Graphics g1) {
    	Graphics2D g = (Graphics2D)g1;
    	Dimension d = getSize();

    	// Draw mask
    	g.setColor(new Color(128, 128, 128, 128));
    	g.fillRect(0, 0, d.width, d.height);

    	Font font = g.getFont().deriveFont((float) 36.0); 
    	g.setFont(font);
    	FontMetrics metrics = g.getFontMetrics(font);
    	int textHeight = metrics.getHeight();
    	int textWidth1 = metrics.stringWidth(openFilesText);
    	int x = 3, y = 3;
    	int width = d.width - 6, height = (d.height - 6);

    	g.setColor(new Color(255, 255, 255, 170));
    	g.fill(new RoundRectangle2D.Float(x, y, width, height, 5, 5));
    	
    	// Draw borders
    	g.setColor(new Color(64, 64, 64, 192));
    	g.setStroke(new BasicStroke(3));
    	g.draw(new RoundRectangle2D.Float(x, y, width, height, 5, 5));
    	
    	
    	// Finally draw text
    	g.setColor(new Color(64, 64, 64, 192));
    	g.drawString(openFilesText, (width - textWidth1)/2, (height - textHeight)/2);


    }

}
