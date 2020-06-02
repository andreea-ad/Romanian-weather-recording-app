package ro.uvt.asavoaei.andreea.weatherrecord;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class App extends Application {

    private LocalDateTime currentTimeStamp;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private String formattedTimeStamp;
    private String statusStr;
    @Override
    public void onCreate() {
        super.onCreate();
        ComponentName componentName = new ComponentName(this, WeatherRecordJobService.class);
        JobInfo info = new JobInfo.Builder(1, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(60 * 60 * 1000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        setCurrentTimeStamp();
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("App>", "Job scheduled");
            statusStr += "Job scheduled\n";
            writeToFile(statusStr, getApplicationContext());
        } else {
            Log.d("App>", "Job schedule failed");
            statusStr += "Job schedule failed\n";
            writeToFile(statusStr, getApplicationContext());
        }
    }

    public void setCurrentTimeStamp() {
        currentTimeStamp = LocalDateTime.now();
        formattedTimeStamp = currentTimeStamp.format(formatter);
        statusStr += "[" + formattedTimeStamp + "]: ";
    }

    private void writeToFile(String data, Context context) {
        try {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("log.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            setCurrentTimeStamp();
            statusStr += "File write failed\n";
        }
    }
}
