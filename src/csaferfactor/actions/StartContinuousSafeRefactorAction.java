package csaferfactor.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.internal.ui.text.SingleTokenJavaScanner;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import csaferefactor.BufferListener;
import csaferefactor.MyJavaElementChangeReporter;
import csaferefactor.SafeRefactorChangeListener;
import csaferefactor.SingleTokeScanner;
import csaferefactor.Visitor;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class StartContinuousSafeRefactorAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public static final String ELCL_SESSION = "icons/full/elcl16/session.gif"; //$NON-NLS-1$
	public static final String ELCL_DUMP = "icons/full/elcl16/dump.gif"; //$NON-NLS-1$

	public static final String EVIEW_COVERAGE = "icons/full/eview16/coverage.gif"; //$NON-NLS-1$
	public static final String EVIEW_EXEC = "icons/full/eview16/exec.gif"; //$NON-NLS-1$

	public static final String OBJ_SESSION = "icons/full/obj16/session.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERFULL = "icons/full/obj16/markerfull.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERNO = "icons/full/obj16/markerno.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERPARTIAL = "icons/full/obj16/markerpartial.gif"; //$NON-NLS-1$

	private ITrackedNodePosition track;

	/**
	 * The constructor.
	 */
	public StartContinuousSafeRefactorAction() {
	}
	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {

//		 IWorkbenchPage page = PlatformUI.getWorkbench()
//		 .getActiveWorkbenchWindow().getActivePage();
//		
//		 if (page == null)
//		 return;
//		 IEditorPart editorPart = page.getActiveEditor();
//		//
//		// ITextEditor editor = (ITextEditor) editorPart
//		// .getAdapter(ITextEditor.class);
//
//		// IDocumentProvider provider = editor.getDocumentProvider();
//		// IDocument document = provider.getDocument(editor.getEditorInput());

		try {
			IResourceChangeListener listener = new SafeRefactorChangeListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
			// IResourceChangeEvent.PRE_BUILD
					IResourceChangeEvent.POST_BUILD);
			// | IResourceChangeEvent.POST_CHANGE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		 IJavaElement javaElement =
//		 JavaUI.getEditorInputJavaElement(editorPart
//		 .getEditorInput());
//		 JavaCore.addElementChangedListener(new MyJavaElementChangeReporter(), ElementChangedEvent.POST_CHANGE);
		//
		// ICompilationUnit icompilationunit = (ICompilationUnit) javaElement;
		//
		//
		// IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		//
		// ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
		//
		// CompilationUnit compilationUnit;
		//
		// compilationUnit = getCompilationUnit(icu,
		// new NullProgressMonitor());
		// compilationUnit = SharedASTProvider.getAST(icu,
		// SharedASTProvider.WAIT_NO, null);

		// TypeDeclaration type = (TypeDeclaration)
		// compilationUnit.types().get(0);
		// MethodDeclaration method = (MethodDeclaration)
		// type.bodyDeclarations().get(1);
		//
		// try {
		// createMarkerForResource(file, method.getStartPosition(),
		// method.getLength());
		// } catch (CoreException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		//
		// ASTRewrite rewrite = ASTRewrite.create(compilationUnit.getAST());
		// Visitor visitor = new Visitor(rewrite);
		// compilationUnit.accept(visitor);
		// JavaCore.addElementChangedListener(new
		// MyJavaElementChangeReporter(file,compilationUnit,visitor.getTracks()),
		// ElementChangedEvent.POST_RECONCILE);
		//

		//
		// IDocumentProvider provider = editor.getDocumentProvider();
		// IDocument document = provider.getDocument(editor.getEditorInput());

		// IResourceChangeListener listener;
		// try {
		// listener = new SafeRefactorChangeListener(document, rewrite, file,
		// visitor.getTracks());
		// ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
		// // IResourceChangeEvent.PRE_BUILD
		// IResourceChangeEvent.POST_BUILD);
		// // | IResourceChangeEvent.POST_CHANGE);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// ISelectionProvider selectionProvider = ((ITextEditor) editor)
		// .getSelectionProvider();
		// ISelection selection = selectionProvider.getSelection();
		// if (selection instanceof ITextSelection) {
		// ITextSelection textSelection = (ITextSelection) selection;
		// int offset = textSelection.getOffset(); // etc.
		// try {
		// IJavaElement elementAt = root.getElementAt(offset);
		//
		// } catch (JavaModelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		//
		// IDocumentProvider provider = editor.getDocumentProvider();
		// IDocument document = provider.getDocument(editor.getEditorInput());

		// document.
		// System.out.println("editor tracker:");
		// test.EditorTracker editorTracker = new
		// test.EditorTracker(PlatformUI.getWorkbench());

		// document.addDocumentListener(new IDocumentListener() {
		//
		// @Override
		// public void documentAboutToBeChanged(DocumentEvent event) {
		//
		// }
		//
		// @Override
		// public void documentChanged(DocumentEvent event) {
		// System.out.println("test");
		//
		// }
		// });
		// }
	}

	private IToken createToken(Color color) {
		TextAttribute textAttribute = new TextAttribute(color);
		return new Token(textAttribute);
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

//	public static void addAnnotation(IMarker marker, ITextSelection selection,
//			ITextEditor editor) {
//		// The DocumentProvider enables to get the document currently loaded in
//		// the editor
//		IDocumentProvider idp = editor.getDocumentProvider();
//		// This is the document we want to connect to. This is taken from
//		// the current editor input.
//		IDocument document = idp.getDocument(editor.getEditorInput());
//		// The IannotationModel enables to add/remove/change annotation to a
//		// Document
//		// loaded in an Editor
//		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());
//		// Note: The annotation type id specify that you want to create one of
//		// your
//		// annotations
//		SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(
//				"com.ibm.example.myannotation", marker);
//		// Finally add the new annotation to the model
//		iamf.connect(document);
//		iamf.addAnnotation(ma,
//				new Position(selection.getOffset(), selection.getLength()));
//		iamf.disconnect(document);
//	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}