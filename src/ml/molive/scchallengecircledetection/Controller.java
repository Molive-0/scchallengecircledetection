package ml.molive.scchallengecircledetection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.opencv.core.Point;

public class Controller {
  @FXML private ImageView currentFrame;

  private ScheduledExecutorService timer;
  private final VideoCapture capture = new VideoCapture();
  private static final int cameraId = 1; //my hi res camera

  protected void startCamera() {

    this.capture.open(cameraId);

    // if we can't get to my hires camera, use the built in one
    if (!this.capture.isOpened()) {
      this.capture.open(0);
    }

    if (this.capture.isOpened()) {

      // grab a frame every 16 ms (62 frames/sec)
      Runnable frameGrabber =
          () -> {
            Mat frame = grabFrame();
            Image imageToShow = Utils.mat2Image(frame);
            updateImageView(currentFrame, imageToShow);
          };

      this.timer = Executors.newSingleThreadScheduledExecutor();
      this.timer.scheduleAtFixedRate(frameGrabber, 0, 16, TimeUnit.MILLISECONDS);
    } else {
      System.err.println("Camera failed to open");
    }
  }

  private Mat grabFrame() {
    Mat frame = new Mat();
    // check if the capture is open
    if (this.capture.isOpened()) {
      try {
        this.capture.read(frame);

        if (!frame.empty()) {
          // We create two new images, grey and circles. Grey is a grayscale and blurred version of
          // the original image, and circles is a buffer containing the circle data stored in a
          // huge column, where r=x, g=y, and b=radius
          // We then take that data and draw a load of circles on the original colour image
          Mat grey = new Mat();
          Imgproc.cvtColor(frame, grey, Imgproc.COLOR_BGR2GRAY);
          Imgproc.medianBlur(grey, grey, 5);
          Mat circles = new Mat();
          Imgproc.HoughCircles(
              grey,
              circles,
              Imgproc.HOUGH_GRADIENT,
              1.0,
              (double) grey.rows() /1.5,
              100.0,
              30.0,
              100,
              200);
          for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(frame, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(frame, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
          }
        }

      } catch (Exception e) {
        // log the error
        System.err.println("Exception during the image processing: " + e);
      }
    }

    return frame;
  }

  private void updateImageView(ImageView view, Image image) {
    Utils.onFXThread(view.imageProperty(), image);
  }

  protected void setClosed() throws InterruptedException {
    this.timer.shutdown();
    this.timer.awaitTermination(16, TimeUnit.MILLISECONDS);

    if (this.capture.isOpened()) {
      this.capture.release();
    }
  }
}
