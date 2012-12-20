package csaferefactor;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

class DeltaPrinter implements IResourceDeltaVisitor {
	
private final ProjectLogger logger;
private final List<String> versions;

//	private String changedContent;
//	private final ASTRewrite rewriter;
//	private final IDocument document;
//	private final List<ITrackedNodePosition> tracks;
//	private final IFile file; 
    public DeltaPrinter(ProjectLogger logger, List<String> versions) {
		this.logger = logger;
//		this.file = file;
//		this.rewriter = rewriter;
//		// TODO Auto-generated constructor stub
//		this.document = document;
//		this.tracks = tracks;
		this.versions = versions;
	}
	public boolean visit(IResourceDelta delta) {
       IResource res = delta.getResource();
       switch (delta.getKind()) {
          case IResourceDelta.ADDED:
//             System.out.print("Resource ");
//             System.out.print(res.getFullPath());
//             System.out.println(" was added.");
             break;
          case IResourceDelta.REMOVED:
//             System.out.print("Resource ");
//             System.out.print(res.getFullPath());
//             System.out.println(" was removed.");
             break;
          case IResourceDelta.CHANGED:
//             System.out.print("Resource ");
//             System.out.print(res.getFullPath());
//             System.out.println(" has changed.");
//             System.out.println("resource type: " + res);
        	  
             if (res.getFileExtension() != null && res.getFileExtension().equals("java") && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
            	 
 				
            	 try {
 					System.out.println("resource mudou: " + res);


 					 String path = logger.log();
 					 versions.add(path);
 					 SafeRefactorJob srJob1 = new
 					 SafeRefactorJob("saferefactor",
 					 1, versions.size(), versions,res);
 					 srJob1.schedule();
 					 // runSafeRefactor(versions.size() - 1,versions.size());
 					
 					 // run saferefactor between the last and the first
// 					 if (versions.size() > 2) {
// 					 // runSafeRefactor(1,versions.size());
// 					 SafeRefactorJob srJob2 = new SafeRefactorJob(
// 					 "saferefactor", 1, versions.size(), versions,res);
// 					 srJob2.schedule();
// 					 }
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}

 			
            	

            	 
             }
             
             break;
       }
       return true; 
    }
 }