
import java.awt.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
public class Filter {

	public static void filterIt(String imageFullPath, int window , int sigmaD, int sigmaR ) throws Exception{
		BufferedImage lennaimg = null;
		try {
			lennaimg = ImageIO.read(new File(imageFullPath));
			System.out.println("lennaimgHeight: " + lennaimg.getHeight() + " lennaimgWidth: " + lennaimg.getWidth());
			
		} catch (IOException e) {
			throw new Exception("loading image failed");
		}
		int lennaHeight = lennaimg.getHeight();
		int lennaWidth = lennaimg.getWidth();

		//copy image to bigger image and set sides
		BufferedImage biggerImg = new BufferedImage(lennaimg.getWidth() + 6, lennaimg.getHeight() + 6, lennaimg.getType());
		int lennaI = 0;
		int lennaJ = 0;
		for(int i = 3; i < lennaimg.getHeight() + 3; i++){
			lennaJ = 0;
			for (int j = 3 ; j < lennaimg.getWidth() + 3; j++){
				int newval = lennaimg.getRGB(lennaI,lennaJ) & 0xff;
				biggerImg.setRGB(i, j, new Color(newval, newval, newval).getRGB());
				lennaJ++;
			}
			lennaI++;
		}
		
		try{
			File tmpBigImage = new File("C:\\Users\\îàéä\\Desktop\\eclipse\\workspace\\BilateralFilter\\src\\tmpBigImage.jpg");
			ImageIO.write(biggerImg,"jpg",tmpBigImage);
		}
	 catch(IOException e){
		throw new Exception("write to image failed");
	}

		for(int i = 0; i <= lennaimg.getHeight() + 5; i++){
			lennaJ = 0;
			for (int j = 0; j <= lennaimg.getWidth() + 5; j++){
				int newval = 0;
						
				if (i >= 0 && i <= 2 && j <= 2  && j >= 0){ // up left corner
					newval = biggerImg.getRGB(6 - i, 6 - j) & 0xff;
					
				}				
				else if (i >= lennaHeight + 3 && i <= lennaHeight + 5 && j <= lennaWidth + 5  && j >= lennaWidth + 3){ //down right corner
					newval = biggerImg.getRGB( 4 + (2 * lennaHeight) -i, 4 + (2 * lennaWidth) - j) & 0xff;
				}
				else if (i >= 0 && i <= 2 && j <= lennaWidth + 5 && j >= lennaWidth + 3){// up right corner
					newval = biggerImg.getRGB(6 - i, 4 + (2 * lennaWidth) - j) & 0xff;
				}
				else if (i >= lennaHeight + 3 && i <= lennaHeight + 5 && j <= 2  && j>=0){ // down left corner
					newval = biggerImg.getRGB(4 + (2 * lennaHeight) - i, 6 - j) & 0xff;
				}
				
				else if (j > 2 && j < lennaWidth + 3 && i >= 0 && i <= 2){ // top row
					newval = biggerImg.getRGB( 6 - i, j) & 0xff;
				}
				
				else if (j > 2 && j < lennaWidth + 3 && i >= lennaHeight+3 && i <= lennaHeight + 5){ // bottom row
					newval = biggerImg.getRGB( 4 + (2 * lennaHeight) - i, j) & 0xff;
					
				}
				else if (j >= 0 && j <= 2 && i > 2 && i < lennaHeight + 3){ //left column
					newval = biggerImg.getRGB(i, 6 - j) & 0xff;
				}
				else if (j >= lennaWidth + 3 && j <= lennaWidth + 5 && i > 2 && i < lennaHeight + 3){ // right column
					newval = biggerImg.getRGB(i, 4 + (2 * lennaWidth) -j ) & 0xff;
				}
				if (newval != 0) {
					biggerImg.setRGB(i ,j ,new Color(newval, newval, newval).getRGB());
				}
			}
		}
		try{
			File newBigImage = new File("C:\\Users\\îàéä\\Desktop\\eclipse\\workspace\\BilateralFilter\\src\\newBigImage.jpg");
			ImageIO.write(biggerImg, "jpg", newBigImage);
		}
	 catch(IOException e){
		System.out.println("write failed");
	}
		
		BufferedImage filteredImage = new BufferedImage(lennaimg.getHeight(), lennaimg.getHeight(), lennaimg.getType());
		for (int i = 0; i < lennaimg.getHeight(); i++){
			for (int j = 0; j < lennaimg.getWidth(); j++){
				int pixelValue = lennaimg.getRGB(i,j) & 0xff;
				int newValue = calculateNewPixel(i,j, window, biggerImg,pixelValue,sigmaD, sigmaR);
				filteredImage.setRGB(i, j, new Color(newValue, newValue, newValue).getRGB());
			}
		}
		try{
			File outputfile = new File("C:\\Users\\îàéä\\Desktop\\eclipse\\workspace\\BilateralFilter\\src\\filtered.jpg");
			ImageIO.write(filteredImage, "jpg", outputfile);
		} catch(IOException e){
			System.out.println("write failed");
		}
	}
	
	public static int calculateNewPixel(int iPixel, int jPixel, int window, BufferedImage img, int pixelValue, double sigmaD, double sigmaR){
		double ks = 0;
		int gap = window/2;
		for (int i = iPixel - gap + 3; i <= iPixel + gap + 3; i++){
			for (int j = jPixel - gap + 3; j <= jPixel + gap + 3; j++){
				if ((i != iPixel || j != jPixel)){
					
					double geometricDistance = calculateGeometricDistanc(iPixel, jPixel, i,j ,sigmaD);
					int neighborPixelValue = (img.getRGB(i, j))& 0xff;
					double photometricSimilarity = calculatePhotometricSimilarity(pixelValue, neighborPixelValue, sigmaR);
					ks += (geometricDistance * photometricSimilarity);
				}
			}
		}
		
		int newPixelValue=0;
		for (int i = iPixel - gap + 3; i <= iPixel + gap + 3; i++){
			for (int j = jPixel - gap + 3; j <= jPixel + gap + 3; j++){
				if ((i != iPixel || j != jPixel)){
					int neighborPixelValue = (img.getRGB(i, j)) & 0xff;
					double geometricDistance = calculateGeometricDistanc(iPixel, jPixel, i, j, sigmaD);
					double photometricSimilarity = calculatePhotometricSimilarity(pixelValue, neighborPixelValue, sigmaR);
					newPixelValue += geometricDistance * photometricSimilarity * neighborPixelValue;

				}
			}
		}
		newPixelValue=(int)(newPixelValue / ks);
		return newPixelValue;
		
	}
	
	public static double calculateGeometricDistanc(int iPixel, int jPixel,int i, int j, double sigmaD){
		double d= Math.sqrt(((iPixel - i) * (iPixel - i)) + ((jPixel - j) * (jPixel - j)));
		return Math.exp( (0 - d) / (2 * sigmaD) );
	}
	
	public static double calculatePhotometricSimilarity(int pixelValue,int neighborPixelValue, double sigmaR){
		int delta = Math.abs(pixelValue - neighborPixelValue);
		return Math.exp((0 - delta) / (2 * sigmaR * sigmaR));
	}
	
	public static void main (String args[]) throws NumberFormatException, Exception{
		if (!args[1].matches("\\+d") || !args[2].matches("\\+d") || !args[3].matches("\\+d")){
			System.out.println("All parameter except the first one should be an Integer");
			return;
		}
		filterIt(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]) , Integer.parseInt(args[3]));
		
	}
}
