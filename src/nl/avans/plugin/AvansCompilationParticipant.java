package nl.avans.plugin;

import java.util.ArrayList;
import java.util.List;

import nl.avans.plugin.debug.BreakpointListener;
import nl.avans.plugin.debug.BreakpointListener.TerminatorListener;
import nl.avans.plugin.debug.ProgramExecutionManager;
import nl.avans.plugin.debug.StepRecorderBreakpoint;
import nl.avans.plugin.model.ProgramExecution;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.core.BreakpointManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class AvansCompilationParticipant extends CompilationParticipant implements TerminatorListener {

	private ProgramExecution programExecution;

	@Override
	public boolean isActive(IJavaProject project) {
		return true;

	}
	
	BreakpointListener listener;

	@Override
	public void buildFinished(IJavaProject project) {
		programExecution = new ProgramExecution();
		IPackageFragment[] packages;
		try {
			packages = project.getPackageFragments();

			for (IPackageFragment mypackage : packages) {
				if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
					System.out.println("Package " + mypackage.getElementName());
					for (ICompilationUnit unit : mypackage
							.getCompilationUnits()) {

						prepCompilationUnit(unit);

					}

				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StepRecorderBreakpoint breakpoint = null;
		ProgramExecution programExecution = new ProgramExecution();
		try {
			breakpoint = new StepRecorderBreakpoint(programExecution, project.findType("TienTeller"), 0, 0);
		} catch (DebugException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (JavaModelException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		if(listener == null)
			listener = new BreakpointListener(project, this);
		IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		
		JDIDebugPlugin.getDefault().addJavaBreakpointListener(listener);
		
		
		try {
			breakpointManager.addBreakpoint(breakpoint);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		IVMInstall vmInstall = null;
		
		try {
			vmInstall = JavaRuntime.getVMInstall(project);
			if (vmInstall == null)
				vmInstall = JavaRuntime.getDefaultVMInstall();
			if (vmInstall != null) {
				IVMRunner vmRunner = vmInstall
						.getVMRunner(ILaunchManager.DEBUG_MODE);
				
				if (vmRunner != null) {
					String[] classPath = null;
					try {
						classPath = JavaRuntime
								.computeDefaultRuntimeClassPath(project);
					} catch (CoreException e) {
					}
					if (classPath != null) {
						VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(
								"TienTeller", classPath);
						

						ILaunchManager manager = DebugPlugin.getDefault()
								.getLaunchManager();
						DebugPlugin.getDefault().addDebugEventListener(listener);
						
						JavaSourceLookupDirector sourceLocator = new JavaSourceLookupDirector();
						sourceLocator.initializeDefaults(DebugPlugin
								.getDefault().getLaunchManager()
								.getLaunchConfigurations()[0]);

						ILaunch launch = new Launch(null,
								ILaunchManager.DEBUG_MODE, sourceLocator);
						

						IType tienTeller = project.findType("TienTeller");
						IMethod main = tienTeller.getMethods()[0];
						String signature = main.getSignature();

						System.out.println(tienTeller + " " + main + " "
								+ signature);

						System.out.println("3 2 1 launch!");
						vmRunner.run(vmConfig, launch, null);
					}
				}
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void prepCompilationUnit(ICompilationUnit unit) throws JavaModelException {
		// TODO Auto-generated method stub
		System.out.println("Prepping" + unit);
		
		String source = unit.getSource();
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray()); // set source
		parser.setResolveBindings(true); // we need bindings later on
		CompilationUnit cu = (CompilationUnit) parser
				.createAST(null /* IProgressMonitor */); // parse

		// For all types in the file
		for (Object typeObject : cu.types()) {
			TypeDeclaration type = (TypeDeclaration) typeObject;

			// For all methods in the type
			for (MethodDeclaration method : type.getMethods()) {

				// For all while-statements in the method
				for (Object statementObject : method.getBody().statements()) {
					System.out.println("Statement: " + statementObject);
				}
			}
		}

	}
	
	private void prepStatementList(List statementList) {
		for(Object statementObject : statementList) {
			Statement statement = (Statement)statementObject;
			//Bla bla
		}
	}

	@Override
	public void debugTerminated() {
		System.out.println("Terminated!");
		DebugPlugin.getDefault().removeDebugEventListener(listener);
		try {
			removeAllBreakpoints();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProgramExecutionManager.getDefault().setProgramExecution(programExecution);
	}

	private void removeAllBreakpoints() throws CoreException {
		
		ArrayList<StepRecorderBreakpoint> stepBreakpoints = new ArrayList<StepRecorderBreakpoint>();
		
		IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		for(IBreakpoint breakpoint : breakpointManager.getBreakpoints()) {
			if(breakpoint instanceof StepRecorderBreakpoint) {
				stepBreakpoints.add((StepRecorderBreakpoint) breakpoint);
			}
		}
		breakpointManager.removeBreakpoints(stepBreakpoints.toArray(new StepRecorderBreakpoint[0]), true);
		
	}
}
