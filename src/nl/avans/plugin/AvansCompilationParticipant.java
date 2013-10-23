package nl.avans.plugin;

import java.util.ArrayList;
import java.util.List;

import nl.avans.plugin.debug.JavaDebuggerListener;
import nl.avans.plugin.debug.JavaDebuggerListener.TerminatorListener;
import nl.avans.plugin.debug.ProgramExecutionManager;
import nl.avans.plugin.debug.StepRecorderBreakpoint;
import nl.avans.plugin.debug.statement.AssignmentStepStatement;
import nl.avans.plugin.debug.statement.IfStepStatement;
import nl.avans.plugin.debug.statement.PrintStepStatement;
import nl.avans.plugin.debug.statement.StepStatement;
import nl.avans.plugin.debug.statement.WhileStepStatement;
import nl.avans.plugin.model.ProgramExecution;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
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
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
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
			/**
			 * Remove any previous breakpoints
			 */
			removeAllBreakpoints();

			programExecution = new ProgramExecution();

			/**
			 * Configure the DebuggerListener
			 */
			JavaDebuggerListener debuggerListener = JavaDebuggerListener
					.getDefault();
			debuggerListener.setTerminatorListener(this);
			debuggerListener.setNeverSuspend(true);

			/**
			 * Install temporary breakpoints
			 */
			List<StepRecorderBreakpoint> breakpoints = getBreakpointsForProject(project);
			for (StepRecorderBreakpoint breakpoint : breakpoints) {
				breakpoint.setProgramExecution(programExecution);
			}
			DebugPlugin
					.getDefault()
					.getBreakpointManager()
					.addBreakpoints(
							breakpoints.toArray(new StepRecorderBreakpoint[0]));

			/**
			 * Launch the application in debug mode in the background
			 */
			IVMInstall vmInstall = null;

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
						ILaunchConfiguration launchConfiguration = DebugPlugin
								.getDefault().getLaunchManager()
								.getLaunchConfigurations()[0];

						JavaSourceLookupDirector sourceLocator = new JavaSourceLookupDirector();
						sourceLocator.initializeDefaults(launchConfiguration);

						ILaunch launch = new Launch(null,
								ILaunchManager.DEBUG_MODE, sourceLocator);

						String mainTypeString = launchConfiguration
								.getAttribute(
										"org.eclipse.jdt.launching.MAIN_TYPE",
										"");
						IType mainType = project.findType(mainTypeString);

						VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(
								mainType.getFullyQualifiedName(), classPath);

						System.out.println("3 2 1 launch!");
						vmRunner.run(vmConfig, launch, null);
					}
				}
			}
		} catch (CoreException e1) {
			cleanup();
			e1.printStackTrace();
		}

	}

	private static List<StepRecorderBreakpoint> getBreakpointsForProject(
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

	private static List<StepRecorderBreakpoint> getBreakpointsCompilationUnit(
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

	private static List<StepRecorderBreakpoint> getBreakpointsForStatements(
			List statements, IType type) {
		List<StepRecorderBreakpoint> breakpoints = new ArrayList<StepRecorderBreakpoint>();
		// For all while-statements in the method
		for (Object statementObject : statements) {

			Statement statement = (Statement) statementObject;
			StepStatement stepStatement = null;

			/**
			 * Create a StepStatement that can handle the statement
			 */

			// While statement
			if (statement.getNodeType() == ASTNode.WHILE_STATEMENT) {
				WhileStatement whileStatement = (WhileStatement) statement;
				stepStatement = new WhileStepStatement(whileStatement, type);
				Block block = (Block) whileStatement.getBody();
				breakpoints.addAll(getBreakpointsForStatements(
						block.statements(), type));
			}

			// Variable declaration
			else if (statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
				stepStatement = new AssignmentStepStatement(
						(VariableDeclarationStatement) statement, type);
				
			} else if(statement.getNodeType() == ASTNode.IF_STATEMENT) {
				IfStatement ifStatement = (IfStatement)statement;
				Block thenBlock = (Block)ifStatement.getThenStatement();
				Block elseBlock = (Block)ifStatement.getElseStatement();
				if(thenBlock != null)
					breakpoints.addAll(getBreakpointsForStatements(thenBlock.statements(), type));
				if(elseBlock != null)
					breakpoints.addAll(getBreakpointsForStatements(elseBlock.statements(), type));
				
				stepStatement = new IfStepStatement(ifStatement, type);
			}

			// Expressions
			else if (statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
				Expression expression = ((ExpressionStatement) statement)
						.getExpression();

				// Variable assignment
				if(expression.getNodeType() == ASTNode.ASSIGNMENT) {
					stepStatement = new AssignmentStepStatement(statement, type, (Assignment)expression);
				}
				
				// Print statement
				else if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
					MethodInvocation methodInvocation = (MethodInvocation) expression;
					if (methodInvocation.getExpression() != null
							&& methodInvocation.getExpression().toString()
									.equals("System.out")
							&& methodInvocation.getName().toString()
									.equals("println")) {

						stepStatement = new PrintStepStatement(statement, type,
								methodInvocation);
					}
				}
			}
			// If we found a node, use it to
			if (stepStatement != null) {
				try {
					StepRecorderBreakpoint breakpoint = new StepRecorderBreakpoint(
							type, stepStatement);
					breakpoints.add(breakpoint);
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
		cleanup();
		ProgramExecutionManager.getDefault().setProgramExecution(
				programExecution);
	}

	/**
	 * Undo any weird configuration we may have caused to the debugger
	 * environment
	 */
	private void cleanup() {
		JavaDebuggerListener.getDefault().setNeverSuspend(false);
		try {
			removeAllBreakpoints();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void removeAllBreakpoints() throws CoreException {
		IBreakpointManager breakpointManager = DebugPlugin.getDefault()
				.getBreakpointManager();

		// Find all StepRecorderBreakpoint
		List<StepRecorderBreakpoint> stepBreakpoints = new ArrayList<StepRecorderBreakpoint>();
		for (IBreakpoint breakpoint : breakpointManager.getBreakpoints()) {
			if (breakpoint instanceof StepRecorderBreakpoint) {
				stepBreakpoints.add((StepRecorderBreakpoint) breakpoint);
			}
		}

		// And remove them all at once
		breakpointManager.removeBreakpoints(
				stepBreakpoints.toArray(new StepRecorderBreakpoint[0]), true);

	}
}
