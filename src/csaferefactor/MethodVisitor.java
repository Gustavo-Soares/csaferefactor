package csaferefactor;

import java.util.List;

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
		String methodSignature = node.getName().getIdentifier() + "(";
		List parameters = node.typeParameters();
		for (int i = 0; i < parameters.size() ; i++ ) {
			methodSignature = methodSignature + parameters.get(i).toString();
			if (i < (parameters.size() -2))
				methodSignature = methodSignature + ", ";
		}
		methodSignature = methodSignature + ")";
		if (methodSignature.equals(target))
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
