package pmedit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

public class FileDropSelectMessage extends JComponent {
	Point mousePos;
	public void setDropPos(Point point){
		mousePos = point;
	}
	
	
	static String openFilesText = "Open file(s)";
	static String batchOperationText = "Batch operation";
	
	public boolean isOpenFile() {
		return isOpenFile;
	}

	public boolean isBatchOperation() {
		return isBatchOperation;
	}

	boolean isOpenFile = false, isBatchOperation = false;
	
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
    	int textAscent = metrics.getAscent();
    	int textWidth1 = metrics.stringWidth(openFilesText);
    	int textWidth2 = metrics.stringWidth(batchOperationText);
    	int x = 3, y = 3;
    	int width = d.width - 6, height1 = (d.height - 6)/3, height2 = (d.height - 6) - height1;
    	int y2  = y + height1;

    	g.setColor(new Color(255, 255, 255, 170));
    	int inset = 3;
	
    	// Draw background rectangles
    	if(mousePos != null) {
    		isOpenFile = mousePos.x >= x && mousePos.x <= width && mousePos.y >= y && mousePos.y <= height1;
    		isBatchOperation = mousePos.x >= x && mousePos.x <= width && mousePos.y >= y2 && mousePos.y <= (y2 + height2);
    	}

    	if(isOpenFile){
        	g.fill(new RoundRectangle2D.Float(x, y, width, height1, 5, 5));
    	} else {
    		g.fill(new RoundRectangle2D.Float((width - textWidth1)/2 - inset, (height1 - textHeight)/2 - textAscent - inset, textWidth1 + 2*inset, textHeight+ 2*inset, 3, 3));
    	}
    	if(isBatchOperation){
        	g.fill(new RoundRectangle2D.Float(x, y2, width, height2, 5, 5));
    	}else{
    		g.fill(new RoundRectangle2D.Float((width - textWidth2)/2 - inset, y2 + (height2 - textHeight)/2 - textAscent - inset, textWidth2 + 2*inset, textHeight+ 2*inset, 3, 3));
    	}
    	
    	// Draw borders
    	g.setColor(new Color(64, 64, 64, 192));
    	g.setStroke(new BasicStroke(3));
    	g.draw(new RoundRectangle2D.Float(x, y, width, height1, 5, 5));
    	g.draw(new RoundRectangle2D.Float(x, y2, width, height2, 5, 5));
    	
    	
    	// Finally draw text
    	g.setColor(new Color(64, 64, 64, 192));
    	g.drawString(openFilesText, (width - textWidth1)/2, (height1 - textHeight)/2);
    	g.drawString(batchOperationText, (width - textWidth2)/2, y2 + (height2 - textHeight)/2);


    }

}
