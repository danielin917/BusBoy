package solarcar.util.filters;



public class  AverageFilter implements SolarFilter {

    private double smoothedPoint;
    private double[] points;
    private int curPos;

    public AverageFilter(int numPoints) {
        	this.curPos = 0;
			this.points = new double[numPoints];
			
			for(int i = 0; i < numPoints; i++) {
				this.points[i] = 0;
			}
    }

    @Override
    public void addPoint(double point) {
        int x = 0;
        this.curPos++;
		this.curPos %= this.points.length;
		this.points[this.curPos] = point;

        for (int i = 0; i < this.points.length ; i++) {
            x += this.points[i];
        }
        this.smoothedPoint = x / this.points.length;
    }

    @Override
    public double filteredPoint() {
        return smoothedPoint;
    }

    @Override
    public void setParams(String args[]) {
    }
}