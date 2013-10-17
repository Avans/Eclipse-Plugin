package nl.avans.plugin;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

public class AvansCompilationParticipant extends CompilationParticipant {

	@Override
	public boolean isActive(IJavaProject project) {
		return true;
		
	}
	@Override
	public void buildFinished(IJavaProject project) {
		try {
			System.out.println("Build finished!" + project.isStructureKnown());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
