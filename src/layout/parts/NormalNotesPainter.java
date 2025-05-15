package layout.parts;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class NormalNotesPainter extends NotesPainter {
	private static final BasicStroke notesBdStrokeHold = new BasicStroke(0.2f);
	private static final AlphaComposite bdAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
	private static final BasicStroke normalStroke = new BasicStroke(1.0f);
	
	public NormalNotesPainter() {
		
	}
	
	@Override
	public void paintNotes(Context context) {
		Graphics2D g2d = (Graphics2D)context.g;
		g2d.setColor(context.bgColor);
		//g2d.fill3DRect(context.x, context.y, context.w, context.h, true);
		g2d.fillRect(context.x, context.y, context.w, context.h);
        g2d.setStroke(notesBdStrokeHold);
        g2d.setColor(Color.BLACK);
        g2d.setComposite(bdAlpha);
        g2d.drawRect(context.x, context.y, context.w, context.h);
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setStroke(normalStroke);
	}

}
