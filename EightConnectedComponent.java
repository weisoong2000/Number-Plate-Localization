import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
public class EightConnectedComponent {

    private BufferedImage srcImg;
    private WritableRaster raster;
    private final int height, width;
    private int [][]imgAry;
    private int [] EQAry;
    private BoundingBox []boxes;
    private int[] pixelCount;
    int[][] NP;
    private int newLabel;

    public EightConnectedComponent(BufferedImage bin_img) {
        this.srcImg = bin_img;
        this.width = bin_img.getWidth();
        this.height = bin_img.getHeight();
        raster = srcImg.getRaster();
    }

    public int[][] EightCCL() {
        imgAry = new int [height+2][width+2];
        newLabel = 0;
        EQAry = new int[(height*width)/2];
        for (int i=0;i<EQAry.length;i++) {
            EQAry[i] = 0;
        }

        for(int i = 0;i<height;i++) {
            for(int j=0; j<width;j++) {
                imgAry[i][j] = 0;
            }
        }
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                int sample = raster.getSample(col, row, 0);
                imgAry[row+1][col+1] = sample;
            }
        }

        pass_one();

        int count = arrangeEQAry();

        pass_two(count);

        Localize();

        return NP;
    }

    public int arrangeEQAry() {
        int count = 0;
        for(int i = 1; i < newLabel+1; i++) {
            if(EQAry[i] == i) {
                count++;
                EQAry[i] = count;
            }
            else {
                EQAry[i] = EQAry[EQAry[i]];
            }
        }
        return count;
    }
    public void pass_one() {
        for(int row=1; row<height+1; row++) {
            for(int col=1; col<width+1; col++) {
                int pixel = imgAry[row][col];

                if(pixel > 0) {
                    int NW = imgAry[row-1][col-1];	//North-West
                    int N = imgAry[row-1][col];		//North
                    int NE = imgAry[row-1][col+1];	//North-East
                    int W = imgAry[row][col-1];		//West

                    if(NW == 0 && N == 0 && NE == 0 && W == 0) {
                        newLabel += 1;
                        imgAry[row][col] = newLabel;
                    }

                    else if((NW != 0 || N != 0 || NE != 0 || W != 0) &&
                            ( (NW != 0 && (NW == N || NW == NE || NW == W)) ||
                                    (N != 0 && (N == NE || N == W)) ||
                                    (NE != 0 && (NE == W)) )
                    ) {
                        int tmp = -1;
                        if(NW != 0) tmp = NW;
                        else if(N != 0) tmp = N;
                        else if(NE != 0) tmp = NE;
                        else if(W != 0) tmp = W;
                        imgAry[row][col] = tmp;
                    }

                    else if(NW != 0 || N != 0 || NE != 0 || W != 0) {
                        int min = newLabel;
                        int max = NW;

                        if (NW != 0 && NW<min) min = NW;
                        if (N>max) max = N;
                        if (N != 0 && N<min) min = N;
                        if (NE>max)  max = NE;
                        if (NE != 0 && NE<min) min = NE;
                        if (W>max) max = W;
                        if (W != 0 && W<min) min = W;

                        imgAry[row][col] = min;
                        EQAry[max] = min;	// Linked or Union
                    }

                }
            }
        }
        for(int row=height; row>1; row--) {
            for(int col=width; col>1; col--) {
                int pixel = imgAry[row][col];

                if(pixel > 0) {

                    int E = imgAry[row][col+1];		//East
                    int SW = imgAry[row+1][col-1];	//South-West
                    int S = imgAry[row+1][col];		//South
                    int SE = imgAry[row+1][col+1];	//South-East


                    if( (E != pixel && E != 0) || (SW != pixel && SW != 0) ||
                            (S != pixel && S != 0) || (SE != pixel && SE != 0) ) {
                        int min = pixel;
                        int max = pixel;
                        if(E != 0 && E<min) min = E;
                        if(E>max) max = E;
                        if(SW != 0 && SW<min) min = SW;
                        if(SW>max) max = SW;
                        if(S != 0 && S<min) min = S;
                        if(S>max) max = S;
                        if(SE !=0 && SE<min) min = SE;

                        imgAry[row][col] = min;
                        EQAry[max] = min;
                    }

                }
            }
        }

    }

    public void pass_two(int count) {
        pixelCount = new int[count+1];
        boxes = new BoundingBox[count+1];

        for(int i=0; i< count+1; i++) {
            pixelCount[i] = 0;
            boxes[i] = new BoundingBox((height*width)/4);
        }

        for(int row=1; row<height+1; row++) {
            for(int col=1; col<width+1; col++) {
                int pixel = imgAry[row][col];

                if(pixel>0) {
                    if(pixel != EQAry[pixel]) {
                        imgAry[row][col] = EQAry[pixel];
                    }

                    if(boxes[imgAry[row][col]].minrow > row) boxes[imgAry[row][col]].minrow = row-1;
                    if(boxes[imgAry[row][col]].mincol > col) boxes[imgAry[row][col]].mincol = col-1;
                    if(boxes[imgAry[row][col]].maxrow < row) boxes[imgAry[row][col]].maxrow = row-1;
                    if(boxes[imgAry[row][col]].maxcol < col) boxes[imgAry[row][col]].maxcol = col-1;
                }

                pixelCount[imgAry[row][col]]++;
            }
        }

        for(int i=0;i<pixelCount.length;i++) {
            if(pixelCount[i] != 0) {
            }
        }

    }

    public void Localize() {
        NP = new int [2][2];

        for(int i=0; i<2;i++) {
            for(int j=0;j<2;j++) {
                NP[i][j] = 0;
            }
        }

        int tmp_index = -1;
        int pixelObj = 0;
        for(int i=1;i<boxes.length;i++) {
            int lpHeight = boxes[i].maxrow - boxes[i].minrow+1;
            int lpWidth = boxes[i].maxcol - boxes[i].mincol+1;

            if(lpHeight>81) continue;

            double ratio = (double)lpWidth / (double)lpHeight;
            
            // 54/97 number plate detected from the dataset
            if(ratio < 3.6 && ratio > 1.9 && pixelCount[i] >= 1000 && pixelCount[i] <=10000 && lpHeight>=20 && lpWidth >= 50) {
                if(pixelObj < pixelCount[i]) {
                    pixelObj = pixelCount[i];
                    tmp_index = i;
                }
            }

            if(ratio<=6 && ratio>=3.6) {
                int size = lpWidth * lpHeight;
                if(((double)pixelCount[i]/(double)size)>=0.8) {

                    if(pixelCount[i] < 1000 ||pixelCount[i]>10000 || lpHeight<20 || lpWidth < 50) continue;

                    NP[0][0] = boxes[i].mincol;
                    NP[0][1] = boxes[i].minrow;
                    NP[1][0] = boxes[i].maxcol;
                    NP[1][1] = boxes[i].maxrow;
                }
            }

        }

        // if there is no plate detect within ratio 2.9 - 6.0, keep the highest probability
        if(NP[0][0] == 0 && NP[0][1] == 0 && NP[1][0] == 0 && NP[1][1] == 0 && tmp_index>-1 && pixelObj>0) {
            NP[0][0] = boxes[tmp_index].mincol;
            NP[0][1] = boxes[tmp_index].minrow;
            NP[1][0] = boxes[tmp_index].maxcol;
            NP[1][1] = boxes[tmp_index].maxrow;
        }

    }
}