package csaferefactor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodVisitor extends ASTVisitor {
	
	private String target;
	private MethodDeclaration method;
	
	public MethodVisitor(String target) {
		this.target = target;		
	}
	@Override
	public boolean visit(MethodDeclaration node) {
		String methodName = node.getName().getIdentifier();
		if (methodName.equals(target))
			setMethod(node);
		return super.visit(node);
	}
	public MethodDeclaration getMethod() {
		return method;
	}
	public void setMethod(MethodDeclaration method) {
		this.method = method;
	}

}
