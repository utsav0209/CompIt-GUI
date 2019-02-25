package shannon;

import java.io.*;
import java.util.*;
/*
 ***************************************************
 *-----------SHANNON FANO COMPRESSION--------------*
 ***************************************************
 */

public class Sfzip{
    //Global resources
    static int[][] freq = new int[256][2];
    static String[] ss = new String[256];
    static int count;
    static int start;

    /***************************************************************
     *calculate frequency
     ***************************************************************/
    static void calFreq(String fname){
        try {
            File file = new File(fname);
            FileInputStream file_in = new FileInputStream(file);
            DataInputStream data_in =  new DataInputStream(file_in);
            while(true){
                try {
                    byte bt = data_in.readByte();
                    int index = to(bt);
                    freq[index][0] = index;
                    freq[index][1]++;
                }catch (EOFException eof){
                    break;
                }
            }
            file_in.close();
            data_in.close();
        }catch (Exception e){
            System.out.println("Exception : "+e);
        }
    }

    /*********************************************************************
     *byte to int conversation
     *********************************************************************/
    static int to(byte b){
        int index = b;
        if(index < 0){
            index = ~b;
            index += 1;
            index ^= 255;
            index += 1;
        }
        return index;
    }
    /*********************************************************************
     *Sorting according to frequency
     *********************************************************************/
    static void sortByFreq(){
        Arrays.sort(freq, new Comparator<int[]>() {
            @Override
            public int compare(final int[] entry1,
                               final int[] entry2) {
                if (entry1[1] > entry2[1])
                    return 1;
                else if(entry1[1] == entry2[1])
                    return 0;
                else
                    return -1;
            }
        });
    }

    /*********************************************************************
     *Generate coding according to frequency
     *********************************************************************/
    static void generateCodes(int l,int h) {
        int pack1 = 0, pack2 = 0, diff1 = 0, diff2 = 0;
        int i, j, d, k=0;
        if ((l + 1) == h || l == h || l > h) {
            if (l == h || l > h)
                return;
            ss[freq[h][0]] = ss[freq[h][0]] + "0";
            ss[freq[l][0]] = ss[freq[l][0]] + "1";
            return;
        }else{
            for(i=l;i<=h-1;i++){
                pack1 += freq[i][1];
            }
            pack2 += freq[h][1];
            diff1 = pack1 - pack2;

            if(diff1 < 0)
                diff1 *= -1;
            j=2;
            while(j!=h-l+1){
                k=h-j;
                pack1=0;
                pack2=0;
                for(i=l;i<=k;i++)
                    pack1 += freq[i][1];
                for(i=h;i>k;i--)
                    pack2 += freq[i][1];
                diff2 = pack1 - pack2;
                if(diff2 < 0)
                    diff2 *= -1;
                if(diff2 >= diff1)
                    break;
                diff1 = diff2;
                j++;
            }
            k++;
            for(i=l;i<=k;i++) {
                ss[freq[i][0]] += "1";
            }
            for(i=k+1;i<=h;i++) {
                ss[freq[i][0]] += "0";
            }
            generateCodes(l,k);
            generateCodes(k+1,h);
        }
    }

    /*********************************************************************
     *Create a fake zipped file
     **********************************************************************/
    static void fakezip(String fname,String fname1){
        int i;
        File filei, fileo;
        filei = new File(fname);
        fileo = new File(fname1);

        try{
            FileInputStream file_in = new FileInputStream(filei);
            DataInputStream data_in = new DataInputStream(file_in);
            PrintStream ps = new PrintStream(fileo);

            while(true){
                try {
                    byte bt = data_in.readByte();
                    ps.print(ss[to(bt)]);
                } catch (EOFException eof) {
                    System.out.println("End of File");
                    break;
                }
            }
            file_in.close();
            data_in.close();
            ps.close();
        }catch(IOException e){
            System.out.println(e);
        }
    }

    /*********************************************************************
     *Create a real zipped file
     **********************************************************************/
    static void realzip(String fname,String fname1){
        int i;
        File filei, fileo;
        filei = new File(fname);
        fileo = new File(fname1);

        try{
            FileInputStream file_in = new FileInputStream(filei);
            DataInputStream data_in = new DataInputStream(file_in);
            FileOutputStream file_out = new FileOutputStream(fileo);
            DataOutputStream data_out = new DataOutputStream(file_out);

            data_out.writeInt(count);
            for(i=start;i<256;i++){
                Byte bt = (byte)i;
                data_out.writeByte((byte)freq[i][0]);
                data_out.writeInt(freq[i][1]);
                //   System.out.println((byte)freq[i][0]+":"+freq[i][1]);
            }

            long textbits;
            textbits = filei.length() % 8;
            textbits = (8 - textbits) % 8;
            int exbits = (int)textbits;
            data_out.writeInt(exbits);
            System.out.println("extrabits : "+(int)exbits);
            byte bt=0;
            while(true){
                try{
                    bt=0;
                    byte ch;
                    for(exbits=0;exbits<8;exbits++){
                        ch = data_in.readByte();
                        bt*=2;
                        if(ch == '1')
                            bt++;
                    }
                    data_out.writeByte(bt);
                }catch (EOFException e){
                    int x;
                    if(exbits != 0){
                        for(x=exbits;x<8;x++){
                            bt*=2;
                        }
                        data_out.write(bt);
                    }
                    break;
                }
            }
            file_in.close();
            file_out.close();
            data_in.close();
            data_out.close();
        }catch(IOException e){
            System.out.println(e);
        }finally {
            File file = new File(fname);
            file.delete();
        }

    }

    public static void zip(String args){
        System.out.println("Shannon-fano Zipping!");
        File f = new File(args);
        String fname = args;
        Arrays.fill(ss,"");
        calFreq(fname);
        sortByFreq();
        int x;
        for(x=0;x<256;x++)
            if(freq[x][1]!=0)
                break;
        count = 256-x;
        start = x;
        System.out.println("x:"+count);
        int i;
        if(count==1)
            ss[255] = "0";
        else if(count == 0)
            return;
        else
            generateCodes(x,255);
        fakezip(args,f.getParent()+"\\fakezipped.txt");
        realzip(f.getParent()+"\\fakezipped.txt",f.getParent()+"\\"+f.getName()+".sfz");
    }
}
