package ro.uvt.asavoaei.andreea.weatherrecord;

import java.util.Objects;

public class WeatherRecord {

    private int id;
    private double latitude;
    private double longitude;
    private String city;
    private int humidity;
    private String nebulosity;
    private double temperature;
    private double windSpeed;
    private double pressure;
    private String recordingDate;
    private String recordingHour;

    public WeatherRecord() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getNebulosity() {
        return nebulosity;
    }

    public void setNebulosity(String nebulosity) {
        this.nebulosity = nebulosity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public String getRecordingDate() {
        return recordingDate;
    }

    public void setRecordingDate(String recordingDate) {
        this.recordingDate = recordingDate;
    }

    public String getRecordingHour() {
        return recordingHour;
    }

    public void setRecordingHour(String recordingHour) {
        this.recordingHour = recordingHour;
    }

    public String toString() {
        return ">> Weather record:\n" + id + "  " + latitude + "  " + longitude + "  " + city + "  " + humidity + "  " + nebulosity + "  " + temperature + "  " + windSpeed + "  " + pressure + "  " + recordingDate + "  " + recordingHour;
    }


    public int hashCode() {
        return Objects.hash(id, latitude, longitude, city, humidity, nebulosity, temperature, windSpeed, pressure, recordingDate);
    }
}