package csaferefactor.eclipse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

public class CompareInput extends CompareEditorInput {
	public CompareInput() {
		super(new CompareConfiguration());
	}

	protected Object prepareInput(IProgressMonitor pm) {
		CompareItem ancestor = new CompareItem("Common", "contents");
		CompareItem left = new CompareItem("Left", "new contents");
		CompareItem right = new CompareItem("Right", "old contents");
		return new DiffNode(null, Differencer.CONFLICTING, ancestor, left,
				right);
	}

	class CompareItem implements IStreamContentAccessor, ITypedElement,
			IModificationDate {
		private String contents, name;

		CompareItem(String name, String contents) {
			this.name = name;
			this.contents = contents;

		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(contents.getBytes());
		}

		public Image getImage() {
			return null;
		}

		public String getName() {
			return name;
		}

		public String getString() {
			return contents;
		}

		public String getType() {
			return ITypedElement.TEXT_TYPE; 
		}

		@Override
		public long getModificationDate() {
			return 0;
		}
	}
}