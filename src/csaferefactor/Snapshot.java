package csaferefactor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import csaferefactor.runnable.DesignWizardThread;

public class Snapshot {
	
	private String path;
	
	private DesignWizardThread designWizardRunner;
	
	private String serverName;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String threadName) {
		this.serverName = threadName;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public DesignWizardThread getDesignWizardRunner() {
		return designWizardRunner;
	}

	public void setDesignWizardRunner(DesignWizardThread designWizardRunner) {
		this.designWizardRunner = designWizardRunner;
	}

}
