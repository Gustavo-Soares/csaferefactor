package csaferefactor.visitor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;


public class ChangeVisitor implements IResourceDeltaVisitor {

	private static final String SAFEREFACTOR_MARKER = "csaferefactor.saferefactorproblem";

	public boolean visit(IResourceDelta delta) {
		IResource res = delta.getResource();
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			break;
		case IResourceDelta.REMOVED:
			break;
		case IResourceDelta.CHANGED:

//			if (isJavFile(delta, res)) {
//
//
//			}
			break;
		}
		return true;
	}

	private boolean isJavFile(IResourceDelta delta, IResource res) {
		return res.getFileExtension() != null
				&& res.getFileExtension().equals("java")
				&& (delta.getFlags() & IResourceDelta.CONTENT) == 0;
	}
}