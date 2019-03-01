package com.yy.easyDemo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Test {

	public static String input = "D:\\opencv\\javacv\\image\\4.jpg";
	public static String inputVideo = "D:\\opencv\\javacv\\image\\1.mp4";
	public static String out = "D:\\opencv\\javacv\\image\\out.jpg";

	public static void main(String[] args) throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Mat src = Imgcodecs.imread(input);
		Mat dst = Imgcodecs.imread(out);
		// show(src, "原图");

		// canddy(src, dst);
		videoCapture(dst);
	}

	public static void videoCapture(Mat dst) throws Exception {

		// 参数为0时从摄像头读入视频
		VideoCapture capture = new VideoCapture(0);

		boolean hasRead = true;
		int i = 0;
		while (hasRead) {
			Mat frame = dst;
			hasRead = capture.read(frame);
			// show(frame, "视频播放");
			i++;

			canddy(dst, dst);

			Thread.sleep(1000);
		}

		System.out.println("i:" + i);
	}

	public static void canddy(Mat src, Mat dst) {
		// 创建一个与原图类型、大小相同的矩阵，并灰度图像
		dst.create(src.size(), src.type());
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY);
		// 使用 3*3 内核降噪
		Imgproc.blur(dst, dst, new Size(3, 3));
		// 运行 Canny 算子
		Imgproc.Canny(dst, dst, 3, 9, 3);
		show(dst, "边缘检测");

		// Imgcodecs.imwrite("D:\\opencv\\javacv\\image\\out22.jpg", dst);
	}

	public static void blur(Mat src, Mat dst, Size size) {
		// 模糊
		Imgproc.blur(src, dst, size);
		ImageViewer imageViewer = new ImageViewer(dst, "模糊");
		imageViewer.imshow();
	}

	public static void erode(Mat src, Mat dst) {
		// 图像腐蚀
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
		Imgproc.erode(src, dst, element);
		ImageViewer imageViewer = new ImageViewer(dst, "腐蚀");
		imageViewer.imshow();
	}

	public static void show(Mat showMat, String winName) {
		ImageViewer imageViewer = new ImageViewer(showMat, winName);
		imageViewer.imshow();
	}

}
