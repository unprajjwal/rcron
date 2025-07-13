package com.rcron.sdk;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class CronClient {
	private final String host;
	private final int port;

	public CronClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Send a command to the CronServer. Received by the CronServer and handled by the handleClient method.
	 * @param jsonCommand
	 * @return
	 * @throws CronException
	 */
	public String sendCommand(String jsonCommand) throws CronException {
		try (Socket socket = new Socket(host, port);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				Scanner in = new Scanner(socket.getInputStream())) {

			out.println(jsonCommand);
			return in.nextLine();
		} catch (Exception e) {
			throw new CronException("Failed to send command to server at " + host + ":" + port, e);
		}
	}
}
