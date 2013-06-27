package csaferefactor;

import java.util.concurrent.Future;

import org.designwizard.main.DesignWizard;

public class Snapshot {
	
	
	private Future<Boolean> futureIsServerLoaded;
	private String path;
	
	private Future<DesignWizard> futureDesignWizard;
	
	
	private String serverName;
	
//	private ExecutorService executor = Executors.newSingleThreadExecutor();

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

//	public ExecutorService getExecutor() {
//		return executor;
//	}
//
//	public void setExecutor(ExecutorService executor) {
//		this.executor = executor;
//	}

	

	public Future<Boolean> getFutureIsServerLoaded() {
		return futureIsServerLoaded;
	}

	public void setFutureIsServerLoaded(Future<Boolean> futureIsServerLoaded) {
		this.futureIsServerLoaded = futureIsServerLoaded;
	}

	public Future<DesignWizard> getFutureDesignWizard() {
		return futureDesignWizard;
	}

	public void setFutureDesignWizard(Future<DesignWizard> futureDesignWizard) {
		this.futureDesignWizard = futureDesignWizard;
	}

}
