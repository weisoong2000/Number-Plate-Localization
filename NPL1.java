import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.MemoryImageSource;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class NPL1 {
	int pix_red[], pix_green[], pix_blue[];
	//int pix_grey[];
	int pix_avg[];
	int width,height;
	
	private static final int KERNEL_SIZE = 3;
    private static final int HALF_KERNEL = KERNEL_SIZE / 2;
    private static final float[] SOBEL_X_KERNEL = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
    private static final float[] SOBEL_Y_KERNEL = {-1, -2, -1, 0, 0, 0, 1, 2, 1};
    private static final int[] x_offset = {-1, -1, -1, 0, 1, 1, 1, 0};
    private static final int[] y_offset = {-1, 0, 1, 1, 1, 0, -1, -1};
	
	public BufferedImage readImage(String url) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(url));
		} catch (IOException e) {
		}
		
	   return img;
	}
	
	public void showImage(Image img, String label) throws IOException
	{
		ImageIcon icon=new ImageIcon(img);
		JFrame frame=new JFrame(label);
		frame.setLayout(new FlowLayout());
		frame.setSize(200,300);
		JLabel lbl=new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public BufferedImage convertToGrayScale(BufferedImage image) {
		BufferedImage gray_img = new BufferedImage(
			image.getWidth(),
			image.getHeight(),
			BufferedImage.TYPE_BYTE_GRAY);

		Graphics2D g = gray_img.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		
		return gray_img;
	}
	
	public BufferedImage convertToBinary(BufferedImage image) {
		BufferedImage binary_img = new BufferedImage(
			image.getWidth(),
			image.getHeight(),
			BufferedImage.TYPE_BYTE_BINARY);

		Graphics2D g = binary_img.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		
		return binary_img;
	}
	
	public BufferedImage binaryToGray(BufferedImage binaryImg) {
		BufferedImage grayImg = new BufferedImage(binaryImg.getWidth(), binaryImg.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		for (int y = 0; y < binaryImg.getHeight(); y++) {
		    for (int x = 0; x < binaryImg.getWidth(); x++) {
		        int color = binaryImg.getRGB(x, y);
		        int gray = (color == 0) ? 0 : 128;
		        grayImg.setRGB(x, y, gray);
		    }
		}
		return grayImg;
	}
	
	public BufferedImage equalize(BufferedImage src) {
		BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                src.getType());
		WritableRaster wr = src.getRaster();
		WritableRaster er = nImg.getRaster();
		int totpix= wr.getWidth()*wr.getHeight();
		int[] histogram = new int[256];
		
		for (int x = 0; x < wr.getWidth(); x++) {
		for (int y = 0; y < wr.getHeight(); y++) {
		   histogram[wr.getSample(x, y, 0)]++;
		}
		}
		
		int[] chistogram = new int[256];
		chistogram[0] = histogram[0];
		for(int i=1;i<256;i++){
		chistogram[i] = chistogram[i-1] + histogram[i];
		}
		
		float[] arr = new float[256];
		for(int i=0;i<256;i++){
		arr[i] =  (float)((chistogram[i]*255.0)/(float)totpix);
		}
		
		for (int x = 0; x < wr.getWidth(); x++) {
		for (int y = 0; y < wr.getHeight(); y++) {
		   int nVal = (int) arr[wr.getSample(x, y, 0)];
		   er.setSample(x, y, 0, nVal);
		}
		}
		nImg.setData(er);
		return nImg;
    }
	
	public BufferedImage opening(BufferedImage img) {
        // Create the structuring element
        int[][] structuringElement = new int[KERNEL_SIZE][KERNEL_SIZE];
        structuringElement[HALF_KERNEL][HALF_KERNEL] = 1;

        // Perform erosion
        BufferedImage erodedImg = erosion(img, structuringElement);
        
        // Perform dilation
        BufferedImage openedImg = dilation(erodedImg, structuringElement);
        //BufferedImage openedImg1 = dilation(openedImg, structuringElement);
        //BufferedImage erodedImg1 = erosion(openedImg1, structuringElement);
        
        return openedImg;
    }
	
	public BufferedImage closing(BufferedImage img) {
        // Create the structuring element
        int[][] structuringElement = new int[KERNEL_SIZE][KERNEL_SIZE];
        structuringElement[HALF_KERNEL][HALF_KERNEL] = 1;

        // Perform dilation
        BufferedImage dilatedImg = dilation(img, structuringElement);

        // Perform erosion
        BufferedImage closedImg = erosion(dilatedImg, structuringElement);

        return closedImg;
    }

    private static BufferedImage erosion(BufferedImage img, int[][] structuringElement) {
        BufferedImage erodedImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        // Perform erosion
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int min = Integer.MAX_VALUE;
                for (int i = 0; i < KERNEL_SIZE; i++) {
                    for (int j = 0; j < KERNEL_SIZE; j++) {
                        int yp = y + i - HALF_KERNEL;
                        int xp = x + j - HALF_KERNEL;
                        if (yp >= 0 && yp < img.getHeight() && xp >= 0 && xp < img.getWidth()) {
                            int pixel = img.getRGB(xp, yp);
                            int value = (pixel >> 16) & 0xff;
                            min = Math.min(min, value);
                        }
                    }
                }
                int newPixel = min | (min << 8) | (min << 16);
                erodedImg.setRGB(x, y, newPixel);
            }
        }
        return erodedImg;
    }
	
    private static BufferedImage dilation(BufferedImage img, int[][] structuringElement) {
        BufferedImage dilatedImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        // Perform dilation
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int max = Integer.MIN_VALUE;
                for (int i = 0; i < KERNEL_SIZE; i++) {
                    for (int j = 0; j < KERNEL_SIZE; j++) {
                        int yp = y + i - HALF_KERNEL;
                        int xp = x + j - HALF_KERNEL;
                        if (yp >= 0 && yp < img.getHeight() && xp >= 0 && xp < img.getWidth()) {
                            int pixel = img.getRGB(xp, yp);
                            int value = (pixel >> 16) & 0xff;
                            max = Math.max(max, value);
                        }
                    }
                }
                int newPixel = max | (max << 8) | (max << 16);
                dilatedImg.setRGB(x, y, newPixel);
            }
        }
        return dilatedImg;
    }

//    public BufferedImage fillHoles(BufferedImage img) {
//        // Create the inverted image
//        BufferedImage invertedImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
//        for (int y = 0; y < img.getHeight(); y++) {
//            for (int x = 0; x < img.getWidth(); x++) {
//                int pixel = img.getRGB(x, y);
//                int value = (pixel >> 16) & 0xff;
//                int invertedValue = value == 0 ? 255 : 0;
//                int invertedPixel = (invertedValue << 16) | (invertedValue << 8) | invertedValue;
//                invertedImg.setRGB(x, y, invertedPixel);
//            }
//        }
//
//        // Perform morphological closing on inverted image
//        BufferedImage closedImg = closing(invertedImg);
//
//        // Invert the closed image
//        for (int y = 0; y < closedImg.getHeight(); y++) {
//            for (int x = 0; x < closedImg.getWidth(); x++) {
//                int pixel = closedImg.getRGB(x, y);
//                int value = (pixel >> 16) & 0xff;
//                int invertedValue = value == 0 ? 255 : 0;
//                int invertedPixel = (invertedValue << 16) | (invertedValue << 8) | invertedValue;
//                closedImg.setRGB(x, y, invertedPixel);
//            }
//        }
//
//        // Perform logical AND with original image
//        BufferedImage filledImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
//        for (int y = 0; y < img.getHeight(); y++) {
//            for (int x = 0; x < img.getWidth(); x++) {
//                int originalPixel = img.getRGB(x, y);
//                int closedPixel = closedImg.getRGB(x, y);
//                int filledPixel = originalPixel & closedPixel;
//                filledImg.setRGB(x, y, filledPixel);
//            }
//        }
//
//        return filledImg;
//    }
    
//	public static int  getGrayScale(int rgb) {
//        int r = (rgb >> 16) & 0xff;
//        int g = (rgb >> 8) & 0xff;
//        int b = (rgb) & 0xff;
//
//        //from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
//        int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);
//        //int gray = (r + g + b) / 3;
//
//        return gray;
//    }
	
//	public BufferedImage sobel (BufferedImage image) {
//    int x = image.getWidth();
//    int y = image.getHeight();
//
//    int maxGval = 0;
//    int[][] edgeColors = new int[x][y];
//    int maxGradient = -1;
//
//    for (int i = 1; i < x - 1; i++) {
//        for (int j = 1; j < y - 1; j++) {
//
//            int val00 = getGrayScale(image.getRGB(i - 1, j - 1));
//            int val01 = getGrayScale(image.getRGB(i - 1, j));
//            int val02 = getGrayScale(image.getRGB(i - 1, j + 1));
//
//            int val10 = getGrayScale(image.getRGB(i, j - 1));
//            int val11 = getGrayScale(image.getRGB(i, j));
//            int val12 = getGrayScale(image.getRGB(i, j + 1));
//
//            int val20 = getGrayScale(image.getRGB(i + 1, j - 1));
//            int val21 = getGrayScale(image.getRGB(i + 1, j));
//            int val22 = getGrayScale(image.getRGB(i + 1, j + 1));
//
//            int gx =  ((-1 * val00) + (0 * val01) + (1 * val02)) 
//                    + ((-2 * val10) + (0 * val11) + (2 * val12))
//                    + ((-1 * val20) + (0 * val21) + (1 * val22));
//
//            int gy =  ((-1 * val00) + (-2 * val01) + (-1 * val02))
//                    + ((0 * val10) + (0 * val11) + (0 * val12))
//                    + ((1 * val20) + (2 * val21) + (1 * val22));
//
//            double gval = Math.sqrt((gx * gx) + (gy * gy));
//            int g = (int) gval;
//
//            if(maxGradient < g) {
//                maxGradient = g;
//            }
//
//            edgeColors[i][j] = g;
//        }
//    }
// 
//    double scale = 255.0 / maxGradient;
//
//    for (int i = 1; i < x - 1; i++) {
//        for (int j = 1; j < y - 1; j++) {
//            int edgeColor = edgeColors[i][j];
//            edgeColor = (int)(edgeColor * scale);
//            edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;
//
//            image.setRGB(i, j, edgeColor);
//
//	        //System.out.println("max : " + maxGradient);
//	        //System.out.println("Finished");
//        }
//    }
//		return image;
//	}
    
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		NPL1 pr = new NPL1();
		
		// Grayscale conversion
		BufferedImage img1 = pr.readImage("Path for test_image.jpg");
		BufferedImage img_gray = pr.convertToGrayScale(img1);
		pr.showImage(img_gray,"Gray Image");
		
		// Median Filtering
		int kernelSize = 7;
		
		BufferedImage filteredImage = new BufferedImage(img_gray.getWidth(), img_gray.getHeight(), img_gray.getType());
		int[] pixels = new int[kernelSize * kernelSize];
        int[] values = new int[kernelSize * kernelSize];
		
		int kernelHalf = kernelSize / 2;
        for (int y = 0; y < img_gray.getHeight(); y++) {
            for (int x = 0; x < img_gray.getWidth(); x++) {
                int w = 0;
                for (int ky = -kernelHalf; ky <= kernelHalf; ky++) {
                    for (int kx = -kernelHalf; kx <= kernelHalf; kx++) {
                        int xCoordinate = x + kx;
                        int yCoordinate = y + ky;
                        if (xCoordinate >= 0 && xCoordinate < img_gray.getWidth() && yCoordinate >= 0 && yCoordinate < img_gray.getHeight()) {
                            int pixel = img_gray.getRGB(xCoordinate, yCoordinate);
                            pixels[w] = pixel;
                            w++;
                        }
                    }
                }
                Arrays.sort(pixels,0,w);
                int median = pixels[w/2];
                filteredImage.setRGB(x, y, median);
            }
        }
        
        pr.showImage(filteredImage,"Median Image");
        
        // histogram equalization
        BufferedImage equalizeImage = new BufferedImage(filteredImage.getWidth(), filteredImage.getHeight(), filteredImage.getType());
        equalizeImage = pr.equalize(filteredImage);
        pr.showImage(equalizeImage,"Histogram Image");
        
        // binarization
        BufferedImage binaryImg = new BufferedImage(equalizeImage.getWidth(), equalizeImage.getHeight(), equalizeImage.getType());
        binaryImg = pr.convertToBinary(equalizeImage);
        pr.showImage(binaryImg,"Binary Image");
        
        // Opening
        BufferedImage openImg = new BufferedImage(binaryImg.getWidth(), binaryImg.getHeight(), binaryImg.getType());
        openImg = pr.opening(binaryImg);
        pr.showImage(openImg,"Opening Image");
        
        // Closing
        BufferedImage closeImg = new BufferedImage(openImg.getWidth(), openImg.getHeight(), openImg.getType());
        closeImg = pr.closing(openImg);
        pr.showImage(closeImg,"Closing Image");
        
        
        // Dilation
        int[][] structuringElement = new int[KERNEL_SIZE][KERNEL_SIZE];
		structuringElement[HALF_KERNEL][HALF_KERNEL] = 1;
		  
		BufferedImage dilImg, dilImg1 = new BufferedImage(closeImg.getWidth(), closeImg.getHeight(), closeImg.getType());
		//dilImg1 = pr.dilation(closeImg, structuringElement);
		dilImg = pr.dilation(closeImg, structuringElement);
		pr.showImage(dilImg,"Dilation Image");
        
//        // Fill Holes
//        BufferedImage fillImg = new BufferedImage(closeImg.getWidth(), closeImg.getHeight(), closeImg.getType());
//        fillImg = pr.fillHoles(closeImg);
//        pr.showImage(fillImg,"Fill Image");
//        
//        // Sobel edge detector
//        BufferedImage sobelImg = new BufferedImage(fillImg.getWidth(), fillImg.getHeight(), fillImg.getType());
//        sobelImg = pr.sobel(fillImg);
//        pr.showImage(sobelImg,"Sobel Image");

//        for (int y = 0; y < closeImg.getHeight()-120; y++) {
//            for (int x = 0; x < closeImg.getWidth(); x++) {
//                int pixel = closeImg.getRGB(x, y);
//                if (pixel == 0xFFFFFFFF) {
//                    //System.out.print("1 ");
//                } else {
//                    //System.out.print("0 ");
//                }
//            }
//            //System.out.println();
//        }

        int condition=1;
        int[][] location;
        do {
	        // Connect component labeling
	        EightConnectedComponent ccl = new EightConnectedComponent(dilImg);
	        location = ccl.EightCCL();
	        condition++;
	        if(condition==10){
	            break;
	        }
        }while(location[0][0]==0&&location[0][1]==0&&location[1][1]==0&&location[1][0]==0);

        DrawBox drawBox = new DrawBox(img1, location);
        drawBox.draw();
	}

}
