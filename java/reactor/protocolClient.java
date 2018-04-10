package main.java.reactor;

import main.java.server.AsyncServerProtocol;
import main.java.server.ProtocolCallback;
import main.java.server.ServerProtocol;

public class protocolClient<T> implements AsyncServerProtocol<T> {
	
	AsyncServerProtocol globalProtocol;
	ConnectionHandler callback;
	
	public protocolClient(AsyncServerProtocol p, ConnectionHandler handler){
		this.globalProtocol = p;
		this.callback = handler;
	}

	public void processMessage(T msg, ProtocolCallback<T> callback) {
		globalProtocol.processMessage(msg, callback);
	}

	@Override
	public boolean isEnd(T msg) {
		return globalProtocol.isEnd(msg);
	}

	@Override
	public boolean shouldClose() {
		return globalProtocol.shouldClose();
	}

	@Override
	public void connectionTerminated() {
		globalProtocol.processMessage("KILL", callback);
		
	}

}
