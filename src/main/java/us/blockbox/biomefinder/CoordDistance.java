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
}
