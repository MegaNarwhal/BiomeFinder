package us.blockbox.biomefinder;

public class CoordDistance implements Comparable<CoordDistance>{
	final Coord coord;
	final double distance;

	public CoordDistance(Coord coord,double distance){
		this.coord = coord;
		this.distance = distance;
	}

	@Override
	public int compareTo(CoordDistance t1){
		if(this.distance == t1.distance){
			return 0;
		}
		if(this.distance > t1.distance){
			return 1;
		}
		return -1;
	}

	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		CoordDistance that = (CoordDistance)o;

		if(Double.compare(that.distance,distance) != 0) return false;
		return coord.equals(that.coord);
	}

	@Override
	public int hashCode(){
		int result;
		long temp;
		result = coord.hashCode();
		temp = Double.doubleToLongBits(distance);
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		return result;
	}
}
