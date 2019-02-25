package huffman;
import java.io.*;
import java.util.PriorityQueue;
/*
 ***************************************************
 *----------HUFFAMAN CODING COMPRESSION------------*
 ***************************************************
 */

public class Huffzip {
    //Global resources
    static int[] freq = new int[256];
    static String[] ss = new String[256];
    static TREE Root;
    static int count;
    static PriorityQueue<TREE> pq = new PriorityQueue<TREE>();

    //TREE class with auto sorting functionality
    static class TREE implements Comparable<TREE>{
        TREE lchild;
        TREE rchild;
        public String code;
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
    static void calFreq(String fname){
        try {
            File file = new File(fname);
            FileInputStream file_in = new FileInputStream(file);
            DataInputStream data_in =  new DataInputStream(file_in);
            while(true){
                try {
                    byte bt = data_in.readByte();
                    freq[to(bt)]++;
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
     **********************************************************************/
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
     ****************************************************************/
    static void MakeTree(){
        int i;
        pq.clear();

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
     **********************************************************************/
    static void dfs(TREE now,String st){
        now.code = st;
        if((now.lchild == null) && (now.rchild == null)){
            ss[now.value] = st;
            System.out.println("Integer : "+now.value+" String : "+st);
            return;
        }
        if(now.lchild != null)
            dfs(now.lchild,st+"0");
        if(now.rchild != null)
            dfs(now.rchild,st+"1");
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
            for(i=0;i<256;i++){
                if(freq[i]!=0){
                    Byte bt = (byte)i;
                    data_out.writeByte(bt);
                    data_out.writeInt(freq[i]);
                }
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
            System.out.println("here!!!!!!!!!");
        }finally {
            File file = new File(fname);
            file.delete();
        }

    }

    public static void zip(String args) {
        System.out.println("In Huffman Zip!");
        File f = new File(args);
        calFreq(args);
        MakeTree();
        if(count > 1)
            dfs(Root,"");
        fakezip(args,f.getParent()+"\\fakezipped.txt");
        realzip(f.getParent()+"\\fakezipped.txt",f.getParent()+"\\"+f.getName()+".huffz");
    }
}
