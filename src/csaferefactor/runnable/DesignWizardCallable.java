package csaferefactor.runnable;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.designwizard.main.DesignWizard;

/**
 * 
 * @author SPG - <a href="http://www.dsc.ufcg.edu.br/~spg"
 *         target="_blank">Software Productivity Group</a>
 * @author Gustavo Soares
 * @author Jeanderson Candido
 */
public class DesignWizardCallable implements Callable<DesignWizard> {

	private String path;

	public DesignWizardCallable(String path) {
		this.path = path;
	}

	@Override
	public DesignWizard call() {
		DesignWizard result = null;
		try {
			result = new DesignWizard(path);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
