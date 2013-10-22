package nl.avans.plugin.debug.statement;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

public class HoiStringetje implements IJavaValue {

	@Override
	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunch getLaunch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		// TODO Auto-generated method stub
		return "type";
	}

	@Override
	public String getValueString() throws DebugException {
		// TODO Auto-generated method stub
		return "hoi";
	}

	@Override
	public boolean isAllocated() throws DebugException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSignature() throws DebugException {
		// TODO Auto-generated method stub
		return "String";
	}

	@Override
	public String getGenericSignature() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IJavaType getJavaType() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNull() {
		// TODO Auto-generated method stub
		return false;
	}
	
}