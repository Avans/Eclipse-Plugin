package nl.avans.plugin;

import java.util.ArrayList;
import java.util.List;

import nl.avans.plugin.debug.JavaDebuggerListener;
import nl.avans.plugin.debug.JavaDebuggerListener.TerminatorListener;
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
import org.eclipse.jdt.core.dom.ASTNode;
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

public class AvansCompilationParticipant extends CompilationParticipant
		implements TerminatorListener {

	private ProgramExecution programExecution;

	@Override
	public boolean isActive(IJavaProject project) {
		return true;

	}

	@Override
	public void buildFinished(IJavaProject project) {
		try {
			removeAllBreakpoints();
		} catch (CoreException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		programExecution = new ProgramExecution();
		
		List<StepRecorderBreakpoint> breakpoints = getBreakpointsForProject(project);
		System.out.println("Breakpoints: " + breakpoints);
		/*
		 * StepRecorderBreakpoint breakpoint = null; try {
		 * 
		 * } catch (DebugException e2) { // TODO Auto-generated catch block
		 * e2.printStackTrace(); } catch (JavaModelException e2) { // TODO
		 * Auto-generated catch block e2.printStackTrace(); }
		 */

		JavaDebuggerListener debuggerListener = JavaDebuggerListener
				.getDefault();
		debuggerListener.setTerminatorListener(this);
		debuggerListener.setNeverSuspend(true);

		IBreakpointManager breakpointManager = DebugPlugin.getDefault()
				.getBreakpointManager();
		try {
			breakpointManager.addBreakpoints(breakpoints.toArray(new StepRecorderBreakpoint[0]));
		} catch (CoreException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		/*
		try {
			//breakpointManager.addBreakpoint(breakpoint);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

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

	private List<StepRecorderBreakpoint> getBreakpointsForProject(
			IJavaProject project) {
		List<StepRecorderBreakpoint> breakpoints = new ArrayList<StepRecorderBreakpoint>();
		IPackageFragment[] packages;
		try {
			packages = project.getPackageFragments();

			for (IPackageFragment mypackage : packages) {
				if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit : mypackage
							.getCompilationUnits()) {

						List<StepRecorderBreakpoint> compilationUnitBreakpoints = getBreakpointsCompilationUnit(unit);
						breakpoints.addAll(compilationUnitBreakpoints);
					}

				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return breakpoints;
	}

	private List<StepRecorderBreakpoint> getBreakpointsCompilationUnit(
			ICompilationUnit unit) throws JavaModelException {
		List<StepRecorderBreakpoint> breakpoints = new ArrayList<StepRecorderBreakpoint>();
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

				List<StepRecorderBreakpoint> statementBreakpoints = getBreakpointsForStatements(
						method.getBody().statements(), unit.getTypes()[0]);

				breakpoints.addAll(statementBreakpoints);
			}
		}
		return breakpoints;
	}

	private List<StepRecorderBreakpoint> getBreakpointsForStatements(List statements, IType type) {
		List<StepRecorderBreakpoint> breakpoints = new ArrayList<StepRecorderBreakpoint>();
		// For all while-statements in the method
		for (Object statementObject : statements) {

			Statement statement = (Statement) statementObject;
			if (statement.getNodeType() == ASTNode.WHILE_STATEMENT) {
				System.out.println("While Statement: " + statement);
				try {
					StepRecorderBreakpoint breakpoint = new StepRecorderBreakpoint(programExecution, type, statement.getStartPosition(), statement.getStartPosition() + statement.getLength());
					breakpoints.add(breakpoint);
				} catch (DebugException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		}
		return breakpoints;
	}

	@Override
	public void debugTerminated() {
		System.out.println("Terminated!");
		JavaDebuggerListener.getDefault().setNeverSuspend(false);
		try {
			removeAllBreakpoints();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProgramExecutionManager.getDefault().setProgramExecution(
				programExecution);
	}

	private void removeAllBreakpoints() throws CoreException {

		ArrayList<StepRecorderBreakpoint> stepBreakpoints = new ArrayList<StepRecorderBreakpoint>();

		IBreakpointManager breakpointManager = DebugPlugin.getDefault()
				.getBreakpointManager();
		for (IBreakpoint breakpoint : breakpointManager.getBreakpoints()) {
			if (breakpoint instanceof StepRecorderBreakpoint) {
				stepBreakpoints.add((StepRecorderBreakpoint) breakpoint);
			}
		}
		breakpointManager.removeBreakpoints(
				stepBreakpoints.toArray(new StepRecorderBreakpoint[0]), true);

	}
}
