package shannon;

import java.io.*;
import java.util.*;
/*
 ***************************************************
 *----------SHANNON FANO De-COMPRESSION------------*
 ***************************************************
 */

public class Sfunzip {
    //Global resources
    static int[][] freq = new int[256][2];
    static String[] ss = new String[256];
    static String[] btost = new String[256];
    static int putit;
    static int count;
    static int start;

    /***************************************************************
     *calculate frequency
     ***************************************************************/
    static void readFreq(String fname){
        int i,ifreq;
        Byte bt;
        try {
            File file = new File(fname);
            FileInputStream file_in = new FileInputStream(file);
            DataInputStream data_in =  new DataInputStream(file_in);
            count = data_in.readInt();
            start = 256-count;
            for (i=start;i<256;i++){
                bt = data_in.readByte();
                freq[i][0] = to(bt);
                ifreq = data_in.readInt();
                freq[i][1] = ifreq;
            }
            file_in.close();
            data_in.close();
        }catch (IOException e){
            System.out.println("IO exception = " + e);
        }
        if(count == 0)
            return;
        else if(count == 1)
            ss[255] = "0";
        else
            generateCodes(start,255);
    }


    /*********************************************************************
     *byte to binary conversation
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
     *Generate coding according to frequency
     *********************************************************************/
    static void generateCodes(int l,int h) {
        int pack1 = 0, pack2 = 0, diff1 = 0, diff2 = 0;
        int i, j, d, k=0;
        if ((l + 1) == h || l == h || l > h) {
            if (l == h || l > h)
                return;
            ss[freq[h][0]] += "0";
            ss[freq[l][0]] += "1";
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
            for(i=l;i<=k;i++)
                ss[freq[i][0]] += "1";
            for(i=k+1;i<=h;i++)
                ss[freq[i][0]] += "0";
            generateCodes(l,k);
            generateCodes(k+1,h);
        }
    }


    /*********************************************************************
     *Integer to string conversion
     *********************************************************************/
    static void createBin(){
        int i,j;
        String temp = "";
        for(i = 0; i < 256; i++){
            btost[i] = "";
            j=i;
            while(j!=0){
                if(j%2==1)
                    btost[i] += "1";
                else
                    btost[i] += "0";
                j /= 2;
            }
            temp = "";
            for(j=btost[i].length()-1;j>=0;j--){
                temp += btost[i].charAt(j);
            }
            btost[i] = temp;
        }
        btost[0] = "0";
    }

    static String makeeight(String b){
        String ret = "";
        int i;
        int len = b.length();
        for(i=0;i<(8-len);i++){
            ret+="0";
        }
        ret+=b;
        return ret;
    }

    static int got(String temp){
        int i;
        for(i=0;i<256;i++){
            if(ss[i].compareTo(temp) == 0 ){
                putit = i;
                return 1;
            }
        }
        return 0;
    }

    /*********************************************************************
     *Read zipped file adn generate unzipped file
     *********************************************************************/
    static void readBin(String zip,String unzip){
        File file1,file2;
        int ok,bt,exbits=0;
        byte b;
        int i,j;
        String bigone="";
        file1 = new File(zip);
        file2 = new File(unzip);

        try{
            FileInputStream file_in = new FileInputStream(file1);
            DataInputStream data_in = new DataInputStream(file_in);
            FileOutputStream file_out = new FileOutputStream(file2);
            DataOutputStream data_out = new DataOutputStream(file_out);
            try{
                count = data_in.readInt();
                System.out.println("Count : "+count);
                for(i = 0;i < count; i++){
                    b = data_in.readByte();
                    j = data_in.readInt();
                }
                exbits = data_in.readInt();
                System.out.println(exbits);
            }catch (EOFException e){
                System.out.println(e);
            }
            while(true){
                try {
                    b = data_in.readByte();
                    bt = to(b);
                    bigone += makeeight(btost[bt]);

                    while(true){
                        ok = 1;
                        String temp = "";
                        for(i = 0; i < bigone.length()-exbits; i++){
                            temp += bigone.charAt(i);
                            if(got(temp)==1){
                                data_out.write(putit);
                                ok = 0;
                                String s = "";
                                for(j = temp.length();j < bigone.length() ; j++){
                                    s+=bigone.charAt(j);
                                }
                                bigone = s;
                                break;
                            }
                        }
                        if(ok==1)
                            break;
                    }
                }catch (Exception e){
                    System.out.println("Readbin1 : "+e);
                    break;
                }
            }
            file_out.close();
            data_out.close();
            file_in.close();
            data_in.close();
        }catch (IOException e){
            System.out.println("Readbean2 : "+e);
        }
    }

    public static void unzip(String args) {
        System.out.println("Shanon-fano Unzipping!");
        int i;
        Arrays.fill(ss,"");
        Arrays.fill(btost,"");
        String fname = args;
        String fname1 = args.substring(0,args.length()-4);
        readFreq(fname);
        createBin();
        readBin(fname,fname1);
    }
}
