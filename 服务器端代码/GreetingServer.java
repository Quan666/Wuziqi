

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.net.Socket;

import java.io.*;
 
public class GreetingServer extends Thread{
	public static void main(String[] args) throws Exception {
		// 实例化一个list,用于保存所有的User
		List<User> list = new ArrayList<User>();
		// 创建绑定到特定端口的服务器套接字
		ServerSocket serverSocket = new ServerSocket(55555);
		System.out.println("服务端正在开始~");
		// 循环监听客户端连接
		while (true) {
			Socket socket = serverSocket.accept();
			// 每接受一个线程，就随机生成一个一个新用户
			//socket.setSoTimeout(1000*60*5);
			User user = new User(""+Math.round(Math.random() * 10000),"",socket);//可能重复
			System.out.println(user.getId() + "加入对局");
			list.add(user);
			// 创建一个新的线程，接收信息并转发
			ServerThread thread = new ServerThread(user, list);
			thread.start();
		}
		
   }
}

/*
  *  服务器线程的作用主要是:
 *   1.接收来自客户端的信息
 *   2.将接收到的信息解析，并转发给目标客户端
 * */
class ServerThread extends Thread {
 
	private User user;
	private List<User> list;
 
	public ServerThread(User user, List<User> list) {
		this.user = user;
		this.list = list;
	}
 
	public void run() {
		try {
			while (true) {
				//0：创建对战
				//1：落子信息
				//2：悔棋信息
				//3：结束游戏
				//4：求和
				//5：重新开始对战
				//6：认输  发送消息的为输家
				//7：发生错误
				//8：结束联机
				//9：等待对方加入联机，锁定棋盘
				//10：解锁棋盘
				// 信息的格式：(0||1||2||3||4),name,发送人id,x,y,IsWhite,IsGameOver$
				//不断地读取客户端发过来的信息
				//第一个人联机后马上下子会导致错误
				String msg= user.getIn().readUTF();
				System.out.println("收到消息："+msg);
				String[] allstr = msg.split("$");
				String[] str = allstr[allstr.length-1].split(",");
				switch (str[0]) {
				case "0"://返回id
					
					create(str,0);
					int count = 0;
					for(User usr:list) {
						if(usr.getName().equals(user.getName()))
							count++;
					}
					if(count<=1) {
						user.getOut().writeUTF("9"+","+user.getName()+","+user.getId()+","+0+","+0+","+user.ismIswhite()+","+false+"$");
						System.out.println("等待..."+count);
					}else {
						sendToClient(str[1], str[2],"10"+","+user.getName()+","+user.getId()+","+0+","+0+","+user.ismIswhite()+","+false+"$");
						user.getOut().writeUTF("10"+","+user.getName()+","+user.getId()+","+0+","+0+","+user.ismIswhite()+","+false+"$");
						System.out.println("开始..."+count);
					}
					break;
				case "1":
					sendToClient(str[1], str[2],msg); // 转发信息给特定的用户
					break;
				case "2":
					sendToClient(str[1], str[2],msg);
					break;
				case "3":
					sendToClient(str[1], str[2], msg);
					remove(user);// 移除用户
					break;
				case "4":
					
					break;
				case "5":
					//create(str,5);
					sendToClient(str[1], str[2],msg); // 转发信息给特定的用户
					break;
				case "6":
					//create(str,5);
					sendToClient(str[1], str[2],msg); // 转发信息给特定的用户
					break;
				case "7":
					
					break;
				case "8":
					sendToClient(str[1], str[2], msg);
					remove(user);// 移除用户
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("异常");
			try {
				
				//返回错误
				user.getOut().writeUTF("7"+","+user.getName()+","+user.getId()+","+0+","+0+","+user.ismIswhite()+","+false+"$");
				sendToClient(user.getName(), user.getId(), "7"+","+user.getName()+","+user.getId()+","+0+","+0+","+user.ismIswhite()+","+false+"$");
				list.remove(user);
				user.getIn().close();
				user.getOut().close();
				user.getSocket().close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		} finally {
			try {
				list.remove(user);
				sendToClient(user.getName(), user.getId(), "7"+","+user.getName()+","+user.getId()+","+0+","+0+","+user.ismIswhite()+","+false+"$");
				user.getIn().close();
				user.getSocket().close();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}
	private void create(String[] str,int type) throws IOException {
		int flag=0;
		for (User user : list) {
			if(user.getName().equals(str[1]))
				flag++;
		}
		if(flag<2) {
			for (int i=0;i<list.size();i++) {
				if(user.getId().equals(list.get(i).getId())) {
					if(flag==0) {
						user.setName(str[1]);
						user.setmIswhite(true);
					}else {
						user.setName(str[1]);
						user.setmIswhite(false);
					}
					System.out.println("id:"+user.getId()+" name:"+user.getName()+" 白："+user.ismIswhite());
					list.set(i, user);
				}
			}
		}else {
			user.getOut().writeUTF("7"+","+user.getName()+","+user.getId()+","+0+","+0+","+user.ismIswhite()+","+false+"$");
			remove(user);
		}
		//返回id
		user.getOut().writeUTF(type+","+user.getName()+","+user.getId()+","+str[3]+","+str[4]+","+user.ismIswhite()+","+user.ismIsGameOver()+"$");
	}
 
	private void sendToClient(String username,String id, String msg) {
 
		for (User user : list) {
			if (user.getName().equals(username)&&!user.getId().equals(id)) {
				try {
					DataOutputStream out =user.getOut();
					out.writeUTF(msg);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
 
	private void remove(User user2) {
		for (int i=0;i<list.size();i++) {
			if(user2.getName().equals(list.get(i).getName())) {
				list.remove(i);
				try {
					user2.getIn().close();
					user2.getOut().close();
					user2.getSocket().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}














class User {
    private String id;//唯一id
    private Socket socket;
    private int x;
    private int y;
    private boolean mIswhite;
    private boolean mIsGameOver;
    private String name;//联机名
    private DataOutputStream out;
    private DataInputStream in;

    public User(String id,String name,final Socket socket) throws IOException{
        this.id = id;
        this.name = name;
        this.socket = socket;
        OutputStream outToServer = null;
        InputStream inFromServer = null;
        try {
            outToServer = socket.getOutputStream();
            inFromServer = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.out = new DataOutputStream(outToServer);
        this.in  =  new DataInputStream(inFromServer);
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean ismIsGameOver() {
        return mIsGameOver;
    }

    public void setmIsGameOver(boolean mIsGameOver) {
        this.mIsGameOver = mIsGameOver;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean ismIswhite() {
        return mIswhite;
    }

    public void setmIswhite(boolean mIswhite) {
        this.mIswhite = mIswhite;
    }
}
