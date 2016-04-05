package solarcar.util.filters;


public interface SolarFilter {

    public void addPoint(double point);

    public double filteredPoint();

    public void setParams(String args[]);
}