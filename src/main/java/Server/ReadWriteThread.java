/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.ByteArrayOutputStream;
import java.net.Socket;

/**
 *
 * @author psy888
 */
public class ReadWriteThread extends Thread{
    private Socket client;
    private byte[] buf = new byte[2048]; //2kb
    private int statusCode;
    private ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
    
    public ReadWriteThread (Socket cli){
        this.client = cli;
    }
    
    @Override
    public void run(){
       try{
           System.out.println("Соединение установлено с : " + client.getInetAddress() + ":" + client.getPort());
           while(true){
               //----------------------Получение--------------------
               BAOS.reset();
               
               do{
                   int cnt = client.getInputStream().read(this.buf,0, this.buf.length);
                   if(cnt==-1)throw new Exception("пакет данных получен");
                   this.BAOS.write(buf, 0, cnt);
               }while(this.client.getInputStream().available()>0);
               //-----------Обработка-- перевод в строку---------------------
               byte[] a = BAOS.toByteArray();
               String string = new String(a, 0, a.length);
               System.out.println(">>>Recived from client : \n" + string);
               //---------полученная строка делится на массив заголовков 
               String[] arr    =   string.split("\r\n");
              
               String response = "";
               
               //output sample
               /*
                GET / HTTP/1.1
                User-Agent: Dalvik/2.1.0 (Linux; U; Android 8.0.0; Android SDK built for x86_64 Build/OSR1.180418.019)
                Host: 192.168.13.129:4000
                Connection: Keep-Alive
                Accept-Encoding: gzip
               */
               if(arr.length==0 || (!arr[0].startsWith("GET")&&!arr[0].startsWith("POST")))
               {
                //отправляем чсообщение об ошибке-----------------------
                   response ="Bad Request";
                   statusCode = 400;
                   /*response =   "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/html; charset=utf-8\r\n"
                            + "Content-length: 5\r\n"
                            + "\r\n"
                            + "Bad Request";*/
                }
               else
               {
                   String query = string.substring(string.indexOf("command"));
                   System.out.println(">>>Recieved query : "+ query);
                   String command = "upper"; //значение команды по умолчанию
                   String[]    params  =   query.split("&");
                    for(int i=0;i<params.length;i++)
                    {
                        //деление на "Ключ" - "Значение"
                        String[] keyValue =   params[i].split("=");
                        //------------Перебор----------
                        if(keyValue[0].contentEquals("command"))
                        {
                            command =   keyValue[1];
                        }
                        else
                        
                        if(keyValue[0].contentEquals("string"))
                        {
                            try{
                            response = keyValue[1];
                            }catch(ArrayIndexOutOfBoundsException e){
                             //   response = "";
                            }
                        }
                    }
                    //-------выполнение строковых операций ------
                    if(response.length()!=0){
                        if(command.contentEquals("upper"))          response = response.toUpperCase();
                        else if(command.contentEquals("lower"))     response = response.toLowerCase();
                        else if(command.contentEquals("cnt"))     response = String.valueOf(response.length());
                        else if(command.contentEquals("mirror"))
                        {
                            char[] z    =   response.toCharArray();
                            for(int i=0;i<z.length/2; i++)
                            {
                                char t  =   z[i];
                                z[i]    =   z[z.length-1-i];
                                z[z.length-1-i] =   t;
                            }
                            response = new String(z);
                        }
                        response = "<h2>" + response +"</h2>";
                        statusCode = 200;
                    }
                    else{
                        //отправляем чсообщение об ошибке-----------------------
                    response =  "No Content";
                    statusCode = 203;
                    }
                      //отправка результата------------
                    byte[] s    =   response.getBytes("UTF8");
                    String pattern =   "HTTP/1.1 "+statusCode+" OK\r\n"
                        + "Content-Type: text/html; charset=utf-8\r\n"
                        + "Content-length: "+String.valueOf(s.length)+"\r\n"
                        + "\r\n";
                    System.out.println(">>>Result : "+ pattern);
                    byte[] b    =   pattern.getBytes("UTF8");
                    this.client.getOutputStream().write(b, 0, b.length);
                    this.client.getOutputStream().write(s, 0, s.length);
                    System.out.println(pattern + "  "+response);
               }
               
               /**
                switch (rgRequestOptions.getCheckedRadioButtonId())
                {
                    case R.id.rbUpper:
                        query += "upper";
                        break;
                    case R.id.rbLower:
                        query += "lower";
                        break;
                    case R.id.rbMirror:
                        query += "mirror";
                        break;
                    case R.id.rbCount:
                        query += "cnt";
                        break;                 
                        
                }
                */
           }
           
           
       }catch(Exception e){
           System.out.println("Disconecting " + e.getMessage());
       }
    }
    
    
}
