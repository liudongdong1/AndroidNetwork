package editdb;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * @author ldd
 * @date 2019.3.24
 * @function  socket connection server side,a simple test
 * */
public class SocketServer implements Runnable {

    //创建一个ServerSocket对象
    private ServerSocket serverSocket;
    //创建一个Socket对象
    private Socket client;

    public void run() {
        try {
            //实例化ServerSocket对象,设置服务端端口8080
            serverSocket = new ServerSocket(8080);
            System.out.println("服务器已启动，等待客户端连接...");
            while (true) {
                //不断循环，接受客户端的访问
                client = serverSocket.accept();
                System.out.println("客户端已连接");
                System.out.println("============");
                String str="hello world";
                try {
                    // 向客户端发送信息
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream())),
                            true);
                    System.out.println("服务器发送:" + str);
                    out.println("服务器：" + str);
                    // 接收客户端信息
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
                    str = in.readLine();
                    System.out.println("客户端:" + str);



                    //本次会话完成,关闭输入输入出流
                    in.close();
                    out.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                } finally {
                    //关闭输入Socket对象
                    client.close();
                    System.out.println("============");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        //创建线程
        Thread thread = new Thread(new SocketServer());
        //开启线程
        thread.start();
    }
}