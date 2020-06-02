package ro.uvt.asavoaei.andreea.weatherrecord;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherRecordJobService extends JobService {
    private static final String TAG = WeatherRecordJobService.class.getSimpleName();
    public static String statusStr;
    public static LocalDateTime currentTimeStamp;
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static String formattedTimeStamp;

    public static void setCurrentTimeStamp(){
        currentTimeStamp = LocalDateTime.now();
        formattedTimeStamp = currentTimeStamp.format(formatter);
        statusStr += "[" + formattedTimeStamp + "]: ";
    }
    @Override
    public boolean onStartJob(JobParameters params) {
        statusStr = "";
        Log.d(TAG, "Job started");
        setCurrentTimeStamp();
        statusStr += "Job started\n";
        doBackgroundWork(params, getApplicationContext());
        return true;
    }

    private void doBackgroundWork(final JobParameters params, final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                new JsonTask(context).execute("http://www.meteoromania.ro/wp-json/meteoapi/v2/starea-vremii");
                Log.d(TAG, "Job finished");
                setCurrentTimeStamp();
                statusStr += "Job finished\n";
                jobFinished(params, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        setCurrentTimeStamp();
        statusStr += "Job cancelled before completion\n";
        return true;
    }

    public static class JsonTask extends AsyncTask<String, String, String> {

        private WeakReference<Context> contextWeakReference;
        private Context context;
        private FirebaseDatabase database = FirebaseDatabase.getInstance();
        private DatabaseReference weatherRecordReference = database.getReference("weather-record");
        private DatabaseReference databaseReference = database.getReference();

        public JsonTask (Context context){
            contextWeakReference = new WeakReference<>(context);
            this.context = context;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");
            setCurrentTimeStamp();
            statusStr += "onPreExecute()\n";
        }

        protected String doInBackground(String... params) {

            fetchItems(params[0]);
            Query query = databaseReference.child("weather-record").orderByChild("city").equalTo("TIMISOARA");
            Log.d(TAG, "BBB");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "CCC");
                    if (dataSnapshot.exists()) {
                        // dataSnapshot is the "weather-record" node with all children with city Timisoara
                        Log.d(TAG, "ON");
                        for (DataSnapshot weatherRecord : dataSnapshot.getChildren()) {
                            Log.d(TAG, "Timisoara: " + weatherRecord.getValue(WeatherRecord.class).toString());

                        }
                    }else{
                        Log.d(TAG, "OFF");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return "";

        }

        private void writeToDB(WeatherRecord weatherRecord){
            String key = weatherRecordReference.push().getKey();
            if(key != null) {
                weatherRecordReference.child(key).setValue(weatherRecord, new DatabaseReference.CompletionListener(){

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null){
                            Log.d(TAG, "The data has been successfully written");
                        }else{
                            Log.d(TAG, "DB writing failed");
                        }
                    }
                });
            }
        }

        private void writeToFile(String data, Context context) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("log.txt", Context.MODE_APPEND));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
                setCurrentTimeStamp();
                statusStr += "File write failed\n";
            }
        }

        private void fetchItems(String urlString){
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try{
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                JSONObject jsonObject = new JSONObject(buffer.toString());
                parseItems(jsonObject);
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void parseItems(JSONObject jsonObject) throws IOException, JSONException {
            JSONArray weatherRecordJsonArray = jsonObject.getJSONArray("features");

            for(int i = 0; i < weatherRecordJsonArray.length(); i++){
                WeatherRecord currentWeatherRecord = new WeatherRecord();
                JSONObject currentFeatureObject = weatherRecordJsonArray.getJSONObject(i);

                JSONObject geometryObject = currentFeatureObject.getJSONObject("geometry");
                JSONArray coordinatesArray = geometryObject.getJSONArray("coordinates");
                currentWeatherRecord.setLatitude(Double.valueOf(coordinatesArray.getString(0)));
                currentWeatherRecord.setLongitude(Double.valueOf(coordinatesArray.getString(1)));
                JSONObject propertiesObject = currentFeatureObject.getJSONObject("properties");
                currentWeatherRecord.setCity(propertiesObject.getString("nume"));
                currentWeatherRecord.setHumidity(propertiesObject.getInt("umezeala"));
                currentWeatherRecord.setNebulosity(propertiesObject.getString("nebulozitate"));
                currentWeatherRecord.setTemperature(Double.valueOf(propertiesObject.getString("tempe")));
                String windSpeedStr = propertiesObject.getString("vant").replace("\\", ")");
                if(windSpeedStr.matches("\\w*")){
                    currentWeatherRecord.setWindSpeed(0.0);
                }else{
                    currentWeatherRecord.setWindSpeed(Double.valueOf(windSpeedStr.split("m")[0]));
                }
                currentWeatherRecord.setPressure(Double.valueOf(propertiesObject.getString("presiunetext").split("mb")[0]));
                String[] date = propertiesObject.getString("actualizat").split("&nbsp;ora&nbsp;");
                currentWeatherRecord.setRecordingDate(date[0]);
                currentWeatherRecord.setRecordingHour(date[1]);
                Log.d(TAG, " " + currentWeatherRecord);
                Log.d(TAG, "Writing to DB");
                setCurrentTimeStamp();
                statusStr += "Writing to DB\n";
                writeToDB(currentWeatherRecord);
                Log.d(TAG, "Finished");
                setCurrentTimeStamp();
                statusStr += "Finished\n";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute");
            setCurrentTimeStamp();
            statusStr += "onPostExecute()\n";
            writeToFile(statusStr, context);
        }
    }
}