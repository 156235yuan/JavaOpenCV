package com.yy.imageExtract.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import com.yy.imageExtract.entity.AreaLines;

public class ContoursUtils {

	private double pointToLine(int x1, int y1, int x2, int y2, int x0, int y0) {
		double space = 0;
		double a, b, c;
		a = lineSpace(x1, y1, x2, y2);// 线段的长度
		b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离
		c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离
		if (c <= 0.000001 || b <= 0.000001) {
			space = 0;
			return space;
		}
		if (a <= 0.000001) {
			space = b;
			return space;
		}
		if (c * c >= a * a + b * b) {
			space = b;
			return space;
		}
		if (b * b >= a * a + c * c) {
			space = c;
			return space;
		}
		double p = (a + b + c) / 2;// 半周长
		double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
		space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
		return space;
	}

	// 计算两点之间的距离
	private double lineSpace(int x1, int y1, int x2, int y2) {
		double lineLength = 0;
		lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		return lineLength;
	}

	/**
	 * 返回边缘检测之后的最大矩形
	 *
	 * @param cannyMat
	 *            Canny之后的mat矩阵
	 * @return
	 */
	public static RotatedRect findMaxRect(Mat cannyMat) {
		MatOfPoint maxContour = findMaxContour(cannyMat);

		MatOfPoint2f matOfPoint2f = new MatOfPoint2f(maxContour.toArray());

		RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);

		return rect;
	}

	/**
	 * 作用：返回边缘检测之后的最大轮廓
	 *
	 * @param cannyMat
	 *            Canny之后的Mat矩阵
	 * @return
	 */
	public static MatOfPoint findMaxContour(Mat cannyMat) {
		List<MatOfPoint> contours = findContours(cannyMat);
		return contours.get(contours.size() - 1);
	}

	/**
	 * 寻找轮廓，并按照递增排序
	 *
	 * @param cannyMat
	 * @return
	 */
	public static List<MatOfPoint> findContours(Mat cannyMat) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		// 寻找轮廓
		/*
		 * Imgproc.findContours(cannyMat, contours, hierarchy, Imgproc.RETR_LIST,
		 * Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
		 */

		Imgproc.findContours(cannyMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE,
				new Point(0, 0));

		if (contours.size() <= 0) {
			throw new RuntimeException("未找到图像轮廓");
		} else {
			// 对contours进行了排序，按递增顺序
			contours.sort(new Comparator<MatOfPoint>() {
				@Override
				public int compare(MatOfPoint o1, MatOfPoint o2) {
					MatOfPoint2f mat1 = new MatOfPoint2f(o1.toArray());
					RotatedRect rect1 = Imgproc.minAreaRect(mat1);
					Rect r1 = rect1.boundingRect();

					MatOfPoint2f mat2 = new MatOfPoint2f(o2.toArray());
					RotatedRect rect2 = Imgproc.minAreaRect(mat2);
					Rect r2 = rect2.boundingRect();

					return (int) (r1.area() - r2.area());
				}
			});
			return contours;
		}
	}

}
