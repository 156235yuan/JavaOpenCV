package com.yy.imageExtract.utils;

import org.opencv.core.Point;

public class Line2 {

	Point _p1;
	Point _p2;
	Point _center;

	Line2(Point p1, Point p2) {
		_p1 = p1;
		_p2 = p2;
		Point _center = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + ((_center == null) ? 0 : _center.y));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Line2 other = (Line2) obj;
		if (_center == null) {
			if (other._center != null)
				return false;
		} else if (!_center.equals(other._center))
			return false;
		if (_p1 == null) {
			if (other._p1 != null)
				return false;
		} else if (!_p1.equals(other._p1))
			return false;
		if (_p2 == null) {
			if (other._p2 != null)
				return false;
		} else if (!_p2.equals(other._p2))
			return false;
		return true;
	}

}
