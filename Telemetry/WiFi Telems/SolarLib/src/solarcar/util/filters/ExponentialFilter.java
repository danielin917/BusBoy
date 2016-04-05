package solarcar.util.filters;

public class  ExponentialFilter implements SolarFilter {

    private double smoothedPoint;
    private double expSmoothing;

    public ExponentialFilter() {
        expSmoothing = 0.5;
    }
	
	public ExponentialFilter(double alpha) {
		expSmoothing = alpha;
	}

    @Override
    public void addPoint(double point) {
        smoothedPoint = smoothedPoint * (1.0 - expSmoothing) + point * expSmoothing;
    }

    @Override
    public double filteredPoint() {
        return smoothedPoint;
    }

    @Override
    public void setParams(String args[]) {
        expSmoothing = Double.parseDouble(args[0]);
        System.out.println("set smoothing to : " + expSmoothing);
    }
}