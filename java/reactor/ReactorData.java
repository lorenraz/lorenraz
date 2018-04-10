package main.java.reactor;

import java.util.concurrent.ExecutorService;

import main.java.server.AsyncServerProtocol;
import main.java.server.ServerProtocol;
import main.java.tokenizer.*;

import java.nio.channels.Selector;

/**
 * a simple data structure that hold information about the reactor, including getter methods
 */
public class ReactorData<T> {

    private final ExecutorService _executor;
    private final Selector _selector;
    private final AsyncServerProtocol _protocol;
    private final TokenizerFactory<T> _tokenizerMaker;
    
    public ExecutorService getExecutor() {
        return _executor;
    }

    public Selector getSelector() {
        return _selector;
    }

	public ReactorData(ExecutorService _executor, Selector _selector, AsyncServerProtocol _protocol, TokenizerFactory<T> tokenizer) {
		this._executor = _executor;
		this._selector = _selector;
		this._protocol= _protocol;
		this._tokenizerMaker = tokenizer;
	}


	public AsyncServerProtocol getProtocol() {
		return _protocol;
	}

	public TokenizerFactory<T> getTokenizerMaker() {
		return _tokenizerMaker;
	}

}
