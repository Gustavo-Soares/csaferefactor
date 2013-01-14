package csaferfactor.actions;

import java.io.IOException;

import org.eclipse.compare.CompareUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import csaferefactor.CompareInput;
import csaferefactor.SafeRefactorPlugin;


/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class StartAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public static final String ELCL_SESSION = "icons/full/elcl16/session.gif"; //$NON-NLS-1$
	public static final String ELCL_DUMP = "icons/full/elcl16/dump.gif"; //$NON-NLS-1$

	public static final String EVIEW_COVERAGE = "icons/full/eview16/coverage.gif"; //$NON-NLS-1$
	public static final String EVIEW_EXEC = "icons/full/eview16/exec.gif"; //$NON-NLS-1$

	public static final String OBJ_SESSION = "icons/full/obj16/session.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERFULL = "icons/full/obj16/markerfull.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERNO = "icons/full/obj16/markerno.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERPARTIAL = "icons/full/obj16/markerpartial.gif"; //$NON-NLS-1$



	/**
	 * The constructor.
	 */
	public StartAction() {
	}
	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {


		SafeRefactorPlugin saferefactor = new SafeRefactorPlugin();
		try {
			saferefactor.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		CompareUI.openCompareEditor(new CompareInput());
		 
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