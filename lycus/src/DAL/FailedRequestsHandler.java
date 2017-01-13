package DAL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import GlobalConstants.Constants;
import GlobalConstants.GlobalConfig;
import GlobalConstants.Enums.ApiAction;
import Interfaces.IFailedRequestsHandler;
import Utils.Logit;

public class FailedRequestsHandler implements IFailedRequestsHandler {

	private static FailedRequestsHandler failsHandler = null;
	private static File failedRequestsDir = null;
	private Object lockFiles = new Object();

	public static FailedRequestsHandler getInstance() {
		if (failsHandler == null)
			failsHandler = new FailedRequestsHandler();
		return failsHandler;
	}

	protected FailedRequestsHandler() {
		failedRequestsDir = new File("failed_requests");
		if (!failedRequestsDir.exists()) {
			Logit.LogInfo("Creating directory for failed requests...");
			boolean result = false;

			try {
				failedRequestsDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				Logit.LogFatal("FailedRequestsHandler - FailedRequestsHandler()",
						"Unable to create failed requests direcotry!", se);
			}
			if (result) {
				Logit.LogInfo("Failed requests directory was created.");
			}
		}
	}

	@Override
	public void addRequest(ApiRequest request) {

		try {

			String fileContent = request.getRequestBody().toString();

			File file = new File(
					failedRequestsDir.getName() + "/" + System.currentTimeMillis() + "." + request.getAction().name());

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(fileContent);
			bw.close();

		} catch (Exception e) {
			Logit.LogFatal("FailedRequestsHandler", "Unable to write failed request to file!", e);
		}

	}

	@Override
	public int getNumberOfFailedRequests() {
		return new File("failed_requests").list().length;
	}

	@Override
	public void executeRequests() {

		Logit.LogInfo("Executing failed requests.");

		synchronized (lockFiles) {
		if (this.getNumberOfFailedRequests() == 0)
			return;
		List<File> files = getFilesOrganized();
		for (final File failedRequestFile : files) {
			JSONObject obj = null;
				try {
					obj = (JSONObject) new JSONParser()
							.parse(new String(Files.readAllBytes(failedRequestFile.toPath())));
				} catch (ParseException | IOException e) {
					Logit.LogError("FailedRequestsHandler - executeRequests",
							"Unable to read failed api request file! E: " + e.getMessage());
				}
				try {
					if (DAL.getInstanece().put(
							ApiAction.valueOf(FilenameUtils.getExtension(failedRequestFile.getName())), obj) != null)
						failedRequestFile.delete();
					else
						return;
				} catch (Exception e) {
					Logit.LogError("FailedRequestsHandler - executeRequests",
							"Failed to send failed api request! E: " + e.getMessage());
				}
			}
		}
	}

	private List<File> getFilesOrganized() {
		List<File> organizedFiles = new ArrayList<File>();
		try {
			for (final File failedRequestFile : failedRequestsDir.listFiles()) {
				String fileName = failedRequestFile.getName();
				long fileNameTime = Long.parseLong(FilenameUtils.getBaseName(fileName));

				if (organizedFiles.size() == 0) {
					organizedFiles.add(failedRequestFile);
					continue;
				}
				boolean added = false;
				for (int i = 0; i < organizedFiles.size(); i++) {
					if (Long.parseLong(FilenameUtils.getBaseName(organizedFiles.get(i).getName())) > fileNameTime) {
						organizedFiles.add(i, failedRequestFile);
						added = true;
						break;
					}
				}
				if (!added)
					organizedFiles.add(failedRequestFile);
			}
		} catch (Exception e) {
			Logit.LogDebug("test");
		}
		return organizedFiles;
	}

}
