package huffman;

import java.io.*;
import java.util.PriorityQueue;

/*
 ***************************************************
 *---------HUFFAMAN CODING DE-COMPRESSION----------*
 ***************************************************
 */

public class Huffunzip {
    //Global resources
    static int[] freq = new int[256];
    static String[] ss = new String[256];
    static String[] btost = new String[256];
    static int putit;
    static TREE Root;
    static int count;
    static PriorityQueue<TREE> pq = new PriorityQueue<>();

    //TREE class with auto sorting functionality
    public static class TREE implements Comparable<TREE>{
        TREE lchild;
        TREE rchild;
        String code;
        public int value;//Index number or say byte value
        public int freq;

        public int compareTo(TREE T){
            if(this.freq < T.freq)
                return -1;
            if(this.freq > T.freq)
                return 1;
            return 0;
        }
    }

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

            for (i=0;i<count;i++){
                bt = data_in.readByte();
                ifreq = data_in.readInt();
                freq[to(bt)] = ifreq;
            }
            file_in.close();
            data_in.close();
        }catch (IOException e){
            System.out.println("IO exception = " + e);
        }

        MakeTree();
        if(count > 1)
            dfs(Root,"");

        for (i=0;i<256;i++) {
            if (ss[i] == null)
                ss[i] = "";
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
     * Generete tree from frequencies
     *********************************************************************/
    static void MakeTree(){
        int i;
        pq.clear();
        count=0;
        for(i=0;i<256;i++){
            if(freq[i] != 0){
                TREE Temp = new TREE();
                Temp.value = i;
                Temp.code = null;
                Temp.freq = freq[i];
                Temp.lchild = null;
                Temp.rchild = null;
                pq.add(Temp);
                count++;
            }
        }

        System.out.println("Maketre: "+count);
        TREE Temp1,Temp2;

        if(count == 0 ){
            return;
        }else if(count == 1){
            for(i=0;i<256;i++){
                if(freq[i] != 0 ) {
                    ss[i] = "0";
                    break;
                }
            }
            return;
        }
        while(pq.size() != 1){
            TREE Temp = new TREE();
            Temp1 = pq.poll();
            Temp2 = pq.poll();
            Temp.lchild = Temp1;
            Temp.rchild = Temp2;
            Temp.freq = Temp1.freq + Temp2.freq;
            pq.add(Temp);
        }
        Root = pq.poll();
    }

    /*********************************************************************
     *travers the tree to generate codes
     *********************************************************************/
    static void dfs(TREE now,String st){
        now.code = st;
        if((now.lchild == null) && (now.rchild == null)){
            ss[now.value] = st;
            return;
        }
        if(now.lchild != null)
            dfs(now.lchild,st+"0");
        if(now.rchild != null)
            dfs(now.rchild,st+"1");
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
        System.out.println("Huffman Unzip!");
        int i;
        for (i = 0; i < 256; i++)
            freq[i] = 0;
        for (i = 0; i < 256; i++)
            ss[i] = "";
        String fname = args;
        String fname1 = args.substring(0,args.length()-6);
        readFreq(fname);
        createBin();
        readBin(fname,fname1);
    }
}

