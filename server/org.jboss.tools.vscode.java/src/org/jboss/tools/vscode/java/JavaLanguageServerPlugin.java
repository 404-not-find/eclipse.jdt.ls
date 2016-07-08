package org.jboss.tools.vscode.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.langs.base.LSPClient;
import org.jboss.tools.langs.base.LSPClient.MessageCallback;
import org.jboss.tools.langs.base.Message;
import org.jboss.tools.vscode.ipc.JsonRpcConnection;
import org.jboss.tools.vscode.ipc.MessageType;
import org.jboss.tools.vscode.ipc.RequestHandler;
import org.jboss.tools.vscode.java.handlers.CompletionHandler;
import org.jboss.tools.vscode.java.handlers.DocumentLifeCycleHandler;
import org.jboss.tools.vscode.java.handlers.DocumentSymbolHandler;
import org.jboss.tools.vscode.java.handlers.ExtensionLifeCycleHandler;
import org.jboss.tools.vscode.java.handlers.FindSymbolsHandler;
import org.jboss.tools.vscode.java.handlers.HoverHandler;
import org.jboss.tools.vscode.java.handlers.NavigateToDefinitionHandler;
import org.jboss.tools.vscode.java.handlers.WorkspaceEventsHandler;
import org.jboss.tools.vscode.java.managers.DocumentsManager;
import org.jboss.tools.vscode.java.managers.ProjectsManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class JavaLanguageServerPlugin implements BundleActivator, MessageCallback {

	public static JavaLanguageServerPlugin instance;
	private static BundleContext context;
	private ProjectsManager pm;
	private DocumentsManager dm;
	private JsonRpcConnection connection;
	private LSPClient client;

	static BundleContext getContext() {
		return context;
	}

	public JavaLanguageServerPlugin() {
		instance = this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		JavaLanguageServerPlugin.context = bundleContext;
		pm = new ProjectsManager();
		dm = new DocumentsManager(connection,pm);
		client = LSPClient.getInstance();
		client.connect(this);
	}

	/**
	 * @return
	 */
	private List<RequestHandler> handlers() {
		List<RequestHandler> handlers = new ArrayList<RequestHandler>();
		handlers.add(new ExtensionLifeCycleHandler(pm));
		handlers.add(new DocumentLifeCycleHandler(dm));
		handlers.add(new CompletionHandler(dm));
		handlers.add(new HoverHandler(dm));
		handlers.add(new NavigateToDefinitionHandler(dm));
		handlers.add(new WorkspaceEventsHandler(pm,dm));
		handlers.add(new DocumentSymbolHandler(dm));
		handlers.add(new FindSymbolsHandler());
		return handlers;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if(client != null){
			client.shutdown();
		}
		JavaLanguageServerPlugin.context = null;
		connection = null;	
	}
	
	public JsonRpcConnection getConnection(){
		return connection;
	}
	
	public static void log(MessageType type, String msg) {
		instance.connection.logMessage(type, msg);
	}

	@Override
	public void messageReceived(Message message) {
		
	System.out.print(	message.getJsonrpc());
		
	}
}
