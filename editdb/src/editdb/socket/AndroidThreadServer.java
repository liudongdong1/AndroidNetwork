package editdb.socket;

import editdb.editdb;
import editdb.socket.SocketThread;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/**
 * @author ldd
 * @date 2019.3.24
 * @function  socket connection server side,Socket listening ...
 * */
public class AndroidThreadServer {
    //定义保存所有socket的list对象
    public static List<Socket> socketList=new ArrayList<Socket>();

    public static void main(String[] args) {
        try {
            //创建socket对象，使用端口8080
            ServerSocket serverSocket=new ServerSocket(8080);
            //不断的循环，接受客户端的访问
            while (true) {
                //创建Socket对象，使用accept()方法不断的创建
                Socket socket=serverSocket.accept();
                //将socket对象添加到list集合里
                socketList.add(socket);
                //客户端连接成功后启启动一条SocketThread线程为该客户的服务
                new Thread(new SocketThread(socket)).start();
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            System.out.println("AndroidThreadServer Exception:===>"+e.getMessage());
        }
    }
}
