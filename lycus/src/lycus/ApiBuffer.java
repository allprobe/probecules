package lycus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;

public class ApiBuffer {
	private int currentDPsBatchesFile;
	
	public ApiBuffer()
	{
		this.createDataPointsBatchesFolder();
		File[] dataPointsBatchesFiles=this.checkDataPointsBatchesFolder();
		if(dataPointsBatchesFiles.length==0)
			this.setCurrentDPsBatchesFile(0);
		this.sendExistingDataPointsBatchesFilesApi(dataPointsBatchesFiles);
	} 
	
	public int getCurrentDPsBatchesFile() {
		return currentDPsBatchesFile;
	}
	public void setCurrentDPsBatchesFile(int currentDPsBatchesFile) {
		this.currentDPsBatchesFile = currentDPsBatchesFile;
	}
	private boolean createDataPointsBatchesFolder()
	{
		File historyDir = new File("data_points_batches");

		// if the directory does not exist, create it
		if (!historyDir.exists()) {
		    boolean result = false;
		    try{
		    	historyDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		    	SysLogger.Record(new Log("Unable to create data_points_batches folder!",LogType.Error));  
		    	return false;
		    }        
		    if(result) {    
		    	SysLogger.Record(new Log("Folder data_points_batches created.",LogType.Info));  
		    	return true;
		    }
		}
		return true;
	}
	public void sendDataPointsApi(String results)
	{
		if(ApiStages.insertDatapointsBatches(results))
			return;
			try {
				File new_data_points_batches_file=new File("data_points_batches/"+System.currentTimeMillis());
				FileUtils.writeStringToFile(new_data_points_batches_file,results,"UTF-8");
				
			} catch (Exception e) {
				SysLogger.Record(new Log("Unable to write data_points_batches locally, data lost!",LogType.Error));
			}
			SysLogger.Record(new Log("Writing data_points_batches to Disk...",LogType.Info));
	}
	private File[] checkDataPointsBatchesFolder()
	{
		 File data_points_batches = new File("data_points_batches");
		  File[] directoryListing = data_points_batches.listFiles();
		  if (directoryListing != null) {
			  Arrays.sort(directoryListing, new Comparator<File>(){
				    public int compare(File f1, File f2)
				    {
				        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
				    } });
			  return directoryListing;
		  } else {
			  SysLogger.Record(new Log("Unable to determine data_points_batches folder!",LogType.Error));
			  return null;
		  }
	}
	private void sendExistingDataPointsBatchesFilesApi(File[] dataPointsBatchesFiles) {
		for(int i=0;i<dataPointsBatchesFiles.length;i++)
		{
			String stringFile;
			try {
				stringFile = GeneralFunctions.readFile(dataPointsBatchesFiles[i].getPath());
			} catch (IOException e) {
				SysLogger.Record(new Log("Unable to read file: "+dataPointsBatchesFiles[i].toString()+", check immedietly!",LogType.Error));
				continue;
			}
			if(!ApiStages.insertDatapointsBatches(stringFile))
				continue;
			dataPointsBatchesFiles[i].delete();
		}
	}
}
