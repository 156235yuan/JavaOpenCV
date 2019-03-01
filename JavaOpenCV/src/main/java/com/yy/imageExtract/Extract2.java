package com.yy.imageExtract;

import static com.yy.imageExtract.constants.Constants.PI;
import static com.yy.imageExtract.constants.Constants.TMP_FOLDER;
import static com.yy.imageExtract.utils.Utils.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.yy.imageExtract.entity.AreaLines;
import com.yy.imageExtract.utils.ImageUtil;
import com.yy.imageExtract.utils.Line;

public class Extract2 {

	public void extract(String input, String output) throws Exception {
		int i = 1;
		Mat src = Imgcodecs.imread(input);
		Mat dst = new Mat();
		// Imgproc.pyrMeanShiftFiltering(src, dst, 50, 10);//均值偏移
		// output(TMP_FOLDER + "/0_meanshift.jpg", dst);

		Mat kernel = new Mat(3, 3, CvType.CV_32F, new Scalar(-1));
		kernel.put(1, 1, 8.9);
		Imgproc.filter2D(src, dst, src.depth(), kernel);// 锐化
		output(TMP_FOLDER + "/" + (i++) + "_sharpening.jpg", dst);

		Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY);// 灰度化
		output(TMP_FOLDER + "/" + (i++) + "_gray.jpg", dst);

		// Imgproc.equalizeHist(dst, dst);//直方图均衡化
		// output(TMP_FOLDER + "/" + (i++) + "_equalizeHist.jpg", dst);

		ImageUtil.gammaCorrection(dst, dst, 0.8f);// gamma校正
		output(TMP_FOLDER + "/" + (i++) + "_gamma.jpg", dst);

		Imgproc.GaussianBlur(dst, dst, new Size(5, 5), 0, 0);// 高斯滤波
		output(TMP_FOLDER + "/" + (i++) + "_gaussianBlur.jpg", dst);

		Imgproc.threshold(dst, dst, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);// 二值化
		output(TMP_FOLDER + "/" + (i++) + "_thresholding.jpg", dst);

		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));

		// Imgproc.dilate(dst, dst, element);//膨胀
		// output(TMP_FOLDER + "/" + (i++) + "_dilate.jpg", dst);

		Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_BLACKHAT, element);// 闭运算
		output(TMP_FOLDER + "/" + (i++) + "_morph_close.jpg", dst);

		// 有些图像多做次腐蚀检测边缘的效果感觉更好些
		Imgproc.erode(dst, dst, element);// 腐蚀
		output(TMP_FOLDER + "/" + (i++) + "_erode.jpg", dst);

		// TODO: 1 Imgproc.Canny(dst, dst, 30, 120, 3);
		double high_thres = Imgproc.threshold(dst, dst, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
		double low_thres = high_thres * 0.5;
		System.out.println("high_thres:" + high_thres + ", low_thres:" + low_thres);
		Imgproc.Canny(dst, dst, low_thres, high_thres); // 边缘检测

		output(TMP_FOLDER + "/" + (i++) + "_canny.jpg", dst);

		// 查找轮廓
		List<MatOfPoint> f_contours = new ArrayList<MatOfPoint>();

		Mat hierarchy = new Mat();
		Imgproc.findContours(dst, f_contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

		// 加粗增强所有找到的轮廓
		Imgproc.drawContours(dst, f_contours, -1, new Scalar(255), 3);
		output(TMP_FOLDER + "/" + (i++) + "_strong.jpg", dst);

		// 再次查找轮廓
		f_contours.clear();
		hierarchy = new Mat();
		Imgproc.findContours(dst, f_contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		MatOfPoint mpoint = getMaximum(f_contours);
		f_contours.clear();
		f_contours.add(mpoint);
		// 画出唯一轮廓
		dst.setTo(new Scalar(0));// 填充为黑色
		Imgproc.drawContours(dst, f_contours, -1, new Scalar(255, 255, 255), 3);
		output(TMP_FOLDER + "/" + (i++) + "_lastContours.jpg", dst);

		// TODO:3
		/*
		 * RotatedRect rect = ContoursUtils.findMaxRect(dst); Point[] rectPoint = new
		 * Point[4]; rect.points(rectPoint); Imgproc.circle(dst, rectPoint[0], 20, new
		 * Scalar(255), 5, Imgproc.LINE_AA); Imgproc.circle(dst, rectPoint[1], 20, new
		 * Scalar(255), 5, Imgproc.LINE_AA); Imgproc.circle(dst, rectPoint[2], 20, new
		 * Scalar(255), 5, Imgproc.LINE_AA); Imgproc.circle(dst, rectPoint[3], 20, new
		 * Scalar(255), 5, Imgproc.LINE_AA); output(TMP_FOLDER + "/" + (i++) +
		 * "_rect.jpg", dst);
		 */

		Mat lines = new Mat();
		// TODO: 2 HoughLinesP(canny, lines, 1, CV_PI / 180, w_proc / 3, w_proc / 3,
		// 20);
		Imgproc.HoughLinesP(dst, lines, 1, PI / 180, 180, 30, 10);// 使用霍夫变换查找线段
		// printLines(src, lines);

		// TODO:
		dd1111(lines, dst, src);

		AreaLines areaLines = putLines(getCenterPoint(dst), lines, src.cols(), src.rows());

		// 获取每个区域的交点坐标
		Point ltp = areaLines.getLeft_top_area().getCrossPoint();
		Point rtp = areaLines.getRight_top_area().getCrossPoint();
		Point rbp = areaLines.getRight_bottom_area().getCrossPoint();
		Point lbp = areaLines.getLeft_bottom_area().getCrossPoint();

		System.out.println("ltp:" + ltp.x + ", y:" + ltp.y);
		System.out.println("rtp:" + rtp.x + ", y:" + rtp.y);
		System.out.println("rbp:" + rbp.x + ", y:" + rbp.y);
		System.out.println("lbp:" + lbp.x + ", y:" + lbp.y);

		Imgproc.circle(dst, ltp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
		Imgproc.circle(dst, rtp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
		Imgproc.circle(dst, rbp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
		Imgproc.circle(dst, lbp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
		output(TMP_FOLDER + "/" + (i++) + "_angle.jpg", dst);

		// 开始做透视变换
		Mat mat = new Mat();
		mat.push_back(new MatOfPoint2f(ltp));
		mat.push_back(new MatOfPoint2f(rtp));
		mat.push_back(new MatOfPoint2f(rbp));
		mat.push_back(new MatOfPoint2f(lbp));

		Size outputSize = getOutputSize(ltp, rtp, rbp, lbp);

		Mat size = new Mat();
		size.push_back(new MatOfPoint2f(new Point(0, 0)));
		size.push_back(new MatOfPoint2f(new Point(outputSize.width, 0)));
		size.push_back(new MatOfPoint2f(new Point(outputSize.width, outputSize.height)));
		size.push_back(new MatOfPoint2f(new Point(0, outputSize.height)));
		Mat pt = Imgproc.getPerspectiveTransform(mat, size);
		Imgproc.warpPerspective(src, src, pt, new Size(outputSize.width, outputSize.height));
		output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);

		// Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);//灰度化
		// output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
		// ImageUtil.gammaCorrection(src, src, 1f/2.2f);//gamma校正
		// output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
		// Imgproc.threshold(src, src, 0, 255, Imgproc.THRESH_OTSU +
		// Imgproc.THRESH_BINARY);//二值化
		// output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);

	}

	/**
	 * 获取图像的中心点
	 * 
	 * @param img
	 * @return
	 */
	private Point getCenterPoint(Mat img) {
		int row = img.rows();
		int col = img.cols();
		int centerX = col / 2 - 1;
		int centerY = row / 2 - 1;
		return new Point(centerX, centerY);
	}

	/**
	 * 输出线段在原始图中的位置，测试时用
	 * 
	 * @param src
	 * @param lines
	 */
	private void printLines(Mat src, Mat lines) {
		for (int i = 0; i < lines.rows(); i++) {
			double[] l = lines.get(i, 0);
			Mat tmp = new Mat();
			src.copyTo(tmp);
			Imgproc.line(tmp, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255), 5, Imgproc.LINE_AA);
			output(TMP_FOLDER + "/a_" + i + ".jpg", tmp);
		}
	}

	/**
	 * 将线段放置到对应区域的对象中
	 * 
	 * @param centerPoint
	 * @param point
	 * @param areaLines
	 * @param line
	 */
	private void putLines(Point centerPoint, Point point, AreaLines areaLines, double[] line) {
		if (point.x <= centerPoint.x && point.y <= centerPoint.y) {// 左上区域
			if (!areaLines.getLeft_top_area().getLines().contains(line)) {
				areaLines.getLeft_top_area().getLines().add(line);
			}
		} else if (point.x > centerPoint.x && point.y <= centerPoint.y) {// 右上区域
			if (!areaLines.getRight_top_area().getLines().contains(line)) {
				areaLines.getRight_top_area().getLines().add(line);
			}
		} else if (point.x <= centerPoint.x && point.y > centerPoint.y) {// 左下区域
			if (!areaLines.getLeft_bottom_area().getLines().contains(line)) {
				areaLines.getLeft_bottom_area().getLines().add(line);
			}
		} else {
			if (!areaLines.getRight_bottom_area().getLines().contains(line)) {// 右下区域
				areaLines.getRight_bottom_area().getLines().add(line);
			}
		}
	}

	/**
	 * 将线段放置到对应区域的对象中
	 * 
	 * @param centerPoint
	 * @param point
	 * @param areaLines
	 * @param line
	 */
	private AreaLines putLines(Point centerPoint, Mat lines, int imgWidth, int imgHeight) {
		AreaLines areaLines = new AreaLines(imgWidth, imgHeight);
		for (int i = 0; i < lines.rows(); i++) {
			double[] line = lines.get(i, 0);
			System.out.println("i:" + i + ", line:" + printArray(line));
			Point p1 = new Point(line[0], line[1]);
			Point p2 = new Point(line[2], line[3]);

			putLines(centerPoint, p1, areaLines, line);
			putLines(centerPoint, p2, areaLines, line);
		}
		return areaLines;
	}

	private String printArray(double[] line) {
		if (line == null || line.length == 0) {
			return "";
		}

		String s = "";
		for (double d : line) {
			s += d + ",";
		}
		return s;
	}

	/**
	 * 获取轮廓最大的
	 * 
	 * @param f_contours
	 * @return
	 */
	private MatOfPoint getMaximum(List<MatOfPoint> f_contours) {
		MatOfPoint mpoint = null;
		double maxArea = 0;
		for (int i = 0; i < f_contours.size(); i++) {
			MatOfPoint2f point2f = new MatOfPoint2f(f_contours.get(i).toArray());
			RotatedRect rect = Imgproc.minAreaRect(point2f);
			double currentArea = rect.size.height * rect.size.width;
			// double currentArea = Imgproc.contourArea(f_contours.get(i));
			if (currentArea > maxArea) {
				mpoint = f_contours.get(i);
				maxArea = currentArea;
			}
		}
		return mpoint;
	}

	public static Size getOutputSize(Point ltp, Point rtp, Point rbp, Point lbp) {
		double h1 = Math.sqrt(Math.pow(ltp.x - lbp.x, 2) + Math.pow(ltp.y - lbp.y, 2));
		double h2 = Math.sqrt(Math.pow(rtp.x - rbp.x, 2) + Math.pow(rtp.y - rbp.y, 2));

		double w1 = Math.sqrt(Math.pow(ltp.x - rtp.x, 2) + Math.pow(ltp.y - rtp.y, 2));
		double w2 = Math.sqrt(Math.pow(lbp.x - rbp.x, 2) + Math.pow(lbp.y - rbp.y, 2));
		return new Size(Math.max(w1, w2), Math.max(h1, h2));
	}

	public void dd1111(Mat lines, Mat srcDest, Mat src) {
		Mat dst = srcDest.clone();

		List<Line> horizontals = new ArrayList<Line>();
		List<Line> verticals = new ArrayList<Line>();

		for (int i = 0; i < lines.rows(); i++) {
			double[] line = lines.get(i, 0);

			double delta_x = line[0] - line[2], delta_y = line[1] - line[3];
			Line l = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));

			if (Math.abs(delta_x) > Math.abs(delta_y)) {
				horizontals.add(l);
			} else {
				verticals.add(l);
			}
		}

		Collections.sort(horizontals, new Comparator<Line>() {
			@Override
			public int compare(Line o1, Line o2) {
				Double y1 = new Double(o1._center.y);
				Double y2 = new Double(o2._center.y);

				return y1.compareTo(y2);
			}
		});
		Collections.sort(verticals, new Comparator<Line>() {
			@Override
			public int compare(Line o1, Line o2) {
				Double y1 = new Double(o1._center.x);
				Double y2 = new Double(o2._center.x);

				return y1.compareTo(y2);
			}
		});

		int size_h = horizontals.size();
		int size_v = verticals.size();
		/*
		 * Point2f computeIntersect = computeIntersect(horizontals.get(0),
		 * verticals.get(0)); Point2f computeIntersect2 =
		 * computeIntersect(horizontals.get(0), verticals.get(size_v - 1)); Point2f
		 * computeIntersect3 = computeIntersect(horizontals.get(size_h - 1),
		 * verticals.get(0)); Point2f computeIntersect4 =
		 * computeIntersect(horizontals.get(size_h - 1), verticals.get(size_v - 1));
		 */

		Point ltp = computeIntersect(horizontals.get(0), verticals.get(0));
		Point rtp = computeIntersect(horizontals.get(0), verticals.get(size_v - 1));
		Point lbp = computeIntersect(horizontals.get(size_h - 1), verticals.get(0));
		Point rbp = computeIntersect(horizontals.get(size_h - 1), verticals.get(size_v - 1));

		System.out.println("ltp:" + ltp.x + ", y:" + ltp.y);
		System.out.println("rtp:" + rtp.x + ", y:" + rtp.y);
		System.out.println("rbp:" + rbp.x + ", y:" + rbp.y);
		System.out.println("lbp:" + lbp.x + ", y:" + lbp.y);

		Imgproc.circle(dst, ltp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
		Imgproc.circle(dst, rtp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
		Imgproc.circle(dst, rbp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
		Imgproc.circle(dst, lbp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
		output(TMP_FOLDER + "/" + "111111111_angle.jpg", dst);

		// 开始做透视变换
		Mat mat = new Mat();
		mat.push_back(new MatOfPoint2f(ltp));
		mat.push_back(new MatOfPoint2f(rtp));
		mat.push_back(new MatOfPoint2f(rbp));
		mat.push_back(new MatOfPoint2f(lbp));

		Size outputSize = getOutputSize(ltp, rtp, rbp, lbp);

		Mat size = new Mat();
		size.push_back(new MatOfPoint2f(new Point(0, 0)));
		size.push_back(new MatOfPoint2f(new Point(outputSize.width, 0)));
		size.push_back(new MatOfPoint2f(new Point(outputSize.width, outputSize.height)));
		size.push_back(new MatOfPoint2f(new Point(0, outputSize.height)));
		Mat pt = Imgproc.getPerspectiveTransform(mat, size);
		Imgproc.warpPerspective(src, src, pt, new Size(outputSize.width, outputSize.height));
		output(TMP_FOLDER + "/" + "1111111_final.jpg", src);

	}

	public Point computeIntersect(Line l1, Line l2) {
		int x1 = (int) l1._p1.x, x2 = (int) l1._p2.x, y1 = (int) l1._p1.y, y2 = (int) l1._p2.y;
		int x3 = (int) l2._p1.x, x4 = (int) l2._p2.x, y3 = (int) l2._p1.y, y4 = (int) l2._p2.y;

		float d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

		Point pt = new Point();

		pt.x = (((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d);
		pt.y = (((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d);

		return pt;

	}
}
