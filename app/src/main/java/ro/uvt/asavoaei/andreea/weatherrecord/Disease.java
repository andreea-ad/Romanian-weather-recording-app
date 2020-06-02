package ro.uvt.asavoaei.andreea.weatherrecord;

import java.util.Objects;

public class Disease {
    private String diseaseName;
    public Disease(String diseaseName){
        this.diseaseName = diseaseName;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disease disease = (Disease) o;
        return Objects.equals(diseaseName, disease.diseaseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diseaseName);
    }
}
