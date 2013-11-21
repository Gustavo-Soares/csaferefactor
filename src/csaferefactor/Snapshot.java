package csaferefactor;

import java.util.concurrent.Future;

import org.designwizard.main.DesignWizard;

/**
 * 
 * @author SPG - <a href="http://www.dsc.ufcg.edu.br/~spg"
 *         target="_blank">Software Productivity Group</a>
 * @author Gustavo Soares
 * @author Jeanderson Candido
 */
public class Snapshot {

	private Future<DesignWizard> futureDesignWizard;
	private Future<Boolean> futureIsServerLoaded;
	private String serverName;
	private String path;

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
