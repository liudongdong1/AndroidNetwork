package editdb.socket;
import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;
/**
 * @author ldd
 * @date 2019.3.24
 * @function  socket connection server side,SocketThread about logic dealing
 * */
public class SocketThread implements Runnable {
    public static Logger logger=Logger.getLogger("SocketThread");
    //定义处理当前线程的Socket
    Socket socket=null;
    //该线程处理Socket所对应的输入流
    BufferedReader bf=null;

    public SocketThread(Socket s){
        this.socket=s;
        try {
            //初始化该socket对应的输入流
            bf=new BufferedReader(new InputStreamReader(s.getInputStream(),"utf-8"));
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            logger.info("SocketThread UnsupportedEncodingException :===>"+e.getMessage());
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            logger.info("SocketThread Exception :====>"+e.getMessage());
        }
    }
    public void run() {
        // TODO Auto-generated method stub
        try {
            String content=null;
            while ((content=readMessageFromeClient())!=null) {
                //遍历socketList中的每一个socket对象，将读取到的内容向每个socket发送一次
                for (Socket socket : AndroidThreadServer.socketList) {
                    //创建输出流对象
                    OutputStream outputStream;
                    outputStream=socket.getOutputStream();
                    logger.info(content+"\n");
                    outputStream.write((content+"\n").getBytes("utf-8"));
                }
            }
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
            logger.info("Run IOException :===>"+e.getMessage());
        }
    }
    private String readMessageFromeClient(){

        try {
            return bf.readLine();
        } catch (IOException e) {
            //如果发生了异常，表示客户端已近关闭，应该删掉socket
            AndroidThreadServer.socketList.remove(socket);
            e.printStackTrace();
            logger.info("readMessageFromeClient IOException :===>"+e.getMessage());
        }
        return null;
    }

    public static void receiveFile(Socket socket,String path) throws IOException {
        byte[] inputByte = null;
        int length = 0;
        DataInputStream din = null;
        FileOutputStream fout = null;
        try {
            (new File(path)).mkdirs();//如果文件夹不存在，则建立新文件夹
            din = new DataInputStream(socket.getInputStream());
            File file = new File(path+File.separator+din.readUTF());
            if (!file.exists()) {   //文件不存在则创建文件，先创建目录
                file.createNewFile();
            }
            fout = new FileOutputStream(file);
            inputByte = new byte[1024];
            logger.info("开始接收数据...");
            while (true) {
                if (din != null) {
                    length = din.read(inputByte, 0, inputByte.length);
                }
                if (length == -1) {
                    break;
                }
                logger.info("文件长度为："+length);
                fout.write(inputByte, 0, length);
                fout.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fout != null)
                fout.close();
            if (din != null)
                din.close();
            if (socket != null)
                socket.close();
        }
    }
    public static void sendFile(Socket socket,String path)throws IOException{
        int length = 0;
        byte[] sendByte = null;
        DataOutputStream dout = null;
        FileInputStream fin = null;
        try {
            dout = new DataOutputStream(socket.getOutputStream());
            File file = new File(path);
            fin = new FileInputStream(file);
            sendByte = new byte[1024];
            dout.writeUTF(file.getName());
            while((length = fin.read(sendByte, 0, sendByte.length))>0){
                dout.write(sendByte,0,length);
                dout.flush();
            }
        } catch (Exception e) {

        } finally{
            if (dout != null)
                dout.close();
            if (fin != null)
                fin.close();
            if (socket != null)
                socket.close();
        }
    }

}
