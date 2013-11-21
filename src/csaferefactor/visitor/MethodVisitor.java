package csaferefactor.visitor;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * Visits a method node and save it if it matches a given signature.
 * 
 * @author SPG - <a href="http://www.dsc.ufcg.edu.br/~spg"
 *         target="_blank">Software Productivity Group</a>
 * @author Gustavo Soares
 * @author Jeanderson Candido
 */
public class MethodVisitor extends ASTVisitor {

	private String targetMethodSignature;
	private MethodDeclaration method;

	/**
	 * Creates a method visitor that will search a method node to compare the
	 * visited signature method with the <i>targetSignature</i>.
	 * 
	 * @param targetSignature
	 *            The signature to be searched
	 */
	public MethodVisitor(String targetSignature) {
		this.targetMethodSignature = targetSignature;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		String signatureToCompare = node.getName().getIdentifier();
		StringBuilder signatureBuilder = new StringBuilder(signatureToCompare);

		signatureBuilder.append("(");
		signatureBuilder = extractParameters(node, signatureBuilder);
		signatureBuilder.append(")");

		signatureToCompare = signatureBuilder.toString();
		if (signatureToCompare.equals(targetMethodSignature))
			this.method = node;

		return super.visit(node);
	}

	/**
	 * Extracts the parameters on the given method node to the given
	 * <i>signatureBuilder</i>.
	 * 
	 * @param node
	 *            The method node to be checked
	 * @param signatureBuilder
	 *            the auxiliary signature builder
	 * @return The given signature builder
	 */
	private StringBuilder extractParameters(MethodDeclaration node,
			StringBuilder signatureBuilder) {

		List<?> parameters = node.parameters();
		for (int i = 0; i < parameters.size(); i++) {
			Object object = parameters.get(i);
			if (object instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration parameter = (SingleVariableDeclaration) object;
				signatureBuilder.append(parameter.getType().toString());

				// has more parameters
				if (i < (parameters.size() - 2))
					signatureBuilder.append(", ");
			}
		}
		return signatureBuilder;
	}

	/**
	 * @return The method found by this visitor according to its targetSignature
	 */
	public MethodDeclaration getMethod() {
		return method;
	}

}
