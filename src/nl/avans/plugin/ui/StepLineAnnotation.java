package nl.avans.plugin.ui;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;

/**
 * Explanation for a single line in the editor
 * 
 * @author paulwagener
 * 
 */
public class StepLineAnnotation extends Annotation implements
		LineBackgroundListener {

	private int textOffset = 300;
	private StepLine stepLine;

	public StepLineAnnotation(StepLine stepLine) {
		super(StepDisplayer.ANNOTATION_TYPE, false, "");
		this.stepLine = stepLine;
	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		StyledText styledText = (StyledText) event.widget;
		int lineOffset = styledText.getOffsetAtLine(stepLine.getLine());
		
		if (event.lineOffset == lineOffset) {
			event.lineBackground = StepAnnotationPainter.BACKGROUND_COLOR;
		}
	}
	
	@Override
	public String getText() {
		return stepLine.getExplanation();
	}
	
	/**
	 * Returns how far from the left the explanation should be displayed (in pixels)
	 * @return
	 */
	public int getTextOffset() {
		return textOffset;
	}

	public int getLine() {
		return stepLine.getLine();
	}

	public void setTextOffset(int textOffset) {
		this.textOffset = textOffset;
		
	}

	public boolean isBold() {
		return stepLine.isBold();
	}

}
