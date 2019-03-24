package editdb;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;  
//@SuppressWarnings({"serial", "rawtypes","unchecked"})
public class editdb extends JFrame{  

	String dbname= null;	
	double zoom=2;
//--
	drawboard board;
	toolbox tool;
	JButton openfile;
	String  current_floor="1";
	Line2D selected_line=null;
	Name selected_name=null;
	Vector<Line2D> line;
	Vector<Point2D> points;
	Vector<Name> namelist;
	Statement stmt;
	
	
	public editdb() throws SQLException, ClassNotFoundException
	{
		openfile =new JButton("打开文件");
		openfile.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					loadFile();
				} catch (ClassNotFoundException | SQLException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		this.add(openfile,BorderLayout.NORTH);
		this.setSize(800, 600);
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				try
				{
					if(stmt!=null)
						stmt.close();
				} catch (SQLException e1)
				{
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});

	}
	
//	显示地图类
	class drawboard extends JPanel
	{
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
				try
				{
					drawmap(g2);
					drawroomname(g2);
					drawedge(g2);
					drawpoint(g2);
					
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
		}
		
	    public drawboard()
	    {  
	      //设置窗口背景颜色  
	      setBackground(Color.white);  
	      //加入鼠标监听
	      addMouseListener(new mouse());
	      addMouseWheelListener(new mouseWheel());
	      setFocusable(true);
	      setFocusable(true);
	      addKeyListener(new deletelisten());
	    }  
	    
//		鼠标监听事件
	    boolean Newname=false;
	    class mouse extends MouseAdapter
		{
			public void mousePressed(MouseEvent e)
			{
				Point2D point2d = e.getPoint();
				if (Newname)
				{
					try
					{	Newname=false;
						New_Name(point2d);
						setCursor(Cursor.getDefaultCursor());
					} catch (SQLException e1)
					{
						e1.printStackTrace();
					}
				}
				else
				{
					select_line(point2d);
					if (selected_line == null)
						select_name(point2d);
				}
//				获取焦点
			   requestFocus();
			}
		}

//	    滚轮事件
	    class mouseWheel implements MouseWheelListener
	    {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
		        if(e.getWheelRotation()==1){
		            zoom-=0.05;
		            repaint();
		            System.out.println("滑轮向后...."+zoom);
		        }
		        if(e.getWheelRotation()==-1){
		        	zoom+=0.05;
		        	repaint();
		            System.out.println("滑轮向前...."+zoom);
		        }
			}
	    }
	}
	
//	工具栏类
	class toolbox extends JPanel
	{ 
		JComboBox<Vector> floorchoser;  //下拉框
		JTextField weightField;
		JTextField nameField;
		public toolbox() throws SQLException
		{   
	       //网格布局1行4列  
	        this.setLayout(new GridLayout(1, 4));
	        this.add(openfile);
	        this.add(choosefloor());
	        this.add(change_weight());
	        this.add(change_name());
	        this.add(new_name());
	    }  
			
		public JPanel choosefloor() throws SQLException
		{
		    JPanel panel;    //面板  
	        JLabel label = new JLabel("选择楼层："); 
	        if(stmt!=null)
	        	floorchoser = new JComboBox<Vector>(getfloor()); 
			floorchoser.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange() == ItemEvent.SELECTED)
					{
						setfloor((String) floorchoser.getSelectedItem());
						System.out.println("获取到当前楼层为：" + current_floor);
					}
				}
			});
	        panel = new JPanel(); 
	        panel.add(label);  
	        panel.add(floorchoser); 
	        return panel;
		}
		
		public JPanel change_weight()
		{
	        JLabel label = new JLabel("路线权重");
	        weightField = new JTextField("1",10);
	        JButton update_weight = new JButton("提交");
	        update_weight.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					// System.out.println("文本框字段："+textField.getText());
					try
					{
						setweight(weightField.getText());
					} catch (SQLException e1)
					{
						e1.printStackTrace();
					}
				}
			});
	        JPanel panel = new JPanel();
	        panel.add(label);
	        panel.add(weightField);
	        panel.add(update_weight);
	        return panel;
		}
		
		public JPanel change_name()
		{
	        JLabel label = new JLabel("房间名");
	        nameField = new JTextField("",10);
	        JButton update_name = new JButton("提交");
	        update_name.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try{
						setname(nameField.getText());
					} catch (SQLException e1){
						e1.printStackTrace();}
				}
			});
	        JPanel panel = new JPanel();
	        panel.add(label);
	        panel.add(nameField);
	        panel.add(update_name);
	        return panel;
		}
	
		public JButton new_name()
		{
			JButton newname=new JButton("增加房间名");
			newname.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					board.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					board.Newname=true;
				}
			});
			return newname;
		}
	}

//	加载文件
	public void loadFile() throws ClassNotFoundException, SQLException
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.CANCEL_OPTION)
		{
			return;
		}
		File fileName = fileChooser.getSelectedFile();

		fileName.canRead();
		if (fileName == null || fileName.getName().equals(""))
		{
			JOptionPane.showMessageDialog(fileChooser, "Invalid File Name", "Invalid File Name",
					JOptionPane.ERROR_MESSAGE);
		} else
		{
//			System.out.println("文件打开成功");
			dbname = fileName.getAbsolutePath();
			init_sqlite();
			this.remove(openfile);
			board = new drawboard();
			tool = new toolbox();
			this.add(tool,BorderLayout.NORTH);
			this.add(board,BorderLayout.CENTER);
			this.setSize(1300, 800);
			this.setVisible(true);
			this.repaint();
		}
	}

//  初始化数据库
	public void init_sqlite() throws SQLException, ClassNotFoundException
	{
		Connection c = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection("jdbc:sqlite:" + dbname);
		stmt = c.createStatement();
		stmt.executeUpdate("PRAGMA synchronous = OFF");
	}

//	delete键监听
	class deletelisten implements KeyListener
	{
			@Override
			public void keyReleased(KeyEvent e)
			{
				System.out.println("键盘监听正常");
				if (e.getKeyCode()==KeyEvent.VK_DELETE||e.getKeyCode()==KeyEvent.VK_BACK_SPACE)
				{
					System.out.println("成功触发事件");
					if(selected_name!=null)
						delete_name();
				}
			}

			@Override
			public void keyTyped(KeyEvent e)
			{
				// TODO 自动生成的方法存根
				
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				// TODO 自动生成的方法存根
				
			}
	}
	
//	获取楼层
	public Vector getfloor() throws SQLException
	{
		Vector result=new Vector<>();
		ResultSet table=stmt.executeQuery("SELECT name FROM sqlite_master WHERE type=\"table\"");
		while (table.next())
		{
			if(table.getString("name").indexOf("map")!=-1)
			{
				String floor=table.getString("name");
				floor=floor.replace("_", "-");
				floor=floor.substring(3);
				result.add(floor);
			}
		}
		table.close();
		Collections.sort(result);
		current_floor=result.get(0).toString();
		return result;
	}

//	设置当前楼层
	public void setfloor(String floor)
	{
		current_floor=floor;
		selected_line=null;
		board.repaint();
	}

//	绘制地图
	public void drawmap(Graphics2D g2) throws NumberFormatException, SQLException
	{
		ResultSet result = stmt.executeQuery("select * from map"+current_floor.replace("-", "_"));
		while (result.next())
		{
			if (result.getString("text_line").indexOf("points") != -1)
			{
				String textline=result.getString("text_line");
				textline = textline.substring(textline.indexOf("\"") + 1, textline.indexOf(" \""));
				// System.out.println(textline);
				String[] temp = textline.split(" ");
				
				Point2D start=new Point2D.Double(Double.parseDouble(temp[0].split(",")[0])* zoom,Double.parseDouble( temp[0].split(",")[1])* zoom);
				Point2D end=new Point2D.Double(Double.parseDouble(temp[1].split(",")[0])* zoom,Double.parseDouble( temp[1].split(",")[1])* zoom);
				Line2D line2d = new Line2D.Double(start, end);
				g2.draw(line2d);
			}
		}
		result.close();
	}

//	画出导航线
	public void drawedge(Graphics2D g2) throws SQLException
	{
		line=new Vector<>();
		points=new Vector<>();
		ResultSet result = stmt.executeQuery("select * from edge where floor='"+current_floor+"'");
		while (result.next())
		{
			g2.setColor(Color.blue);
			String temp0 = result.getString("pre_node");
			String temp1 = result.getString("be_node");
			Point2D start = new Point2D.Double(Double.parseDouble(temp0.split(",")[0]) * zoom,
					Double.parseDouble(temp0.split(",")[1]) * zoom);
			Point2D end = new Point2D.Double(Double.parseDouble(temp1.split(",")[0]) * zoom,
					Double.parseDouble(temp1.split(",")[1]) * zoom);
			Line2D line2d = new Line2D.Double(start, end);
//			用不同颜色画出被选中的线段
			if (selected_line != null)
				if (line2d.getX1() == selected_line.getX1() & line2d.getX2() == selected_line.getX2()
						& line2d.getY1() == selected_line.getY1() & line2d.getY2() == selected_line.getY2())
					g2.setColor(Color.green);
			line.add(line2d);
			points.add(start);
			points.add(end);
			g2.draw(line2d);
			result.next();		//防止重复输出线段，具体可查看edge表的结构
		}
		result.close();
	}

//	画出导航点
	public void drawpoint(Graphics2D g2) throws SQLException
	{
		g2.setColor(Color.RED);
		Enumeration<Point2D> enu = points.elements();
		while (enu.hasMoreElements())
		{
			Point2D point = enu.nextElement();
			g2.draw(new Ellipse2D.Double(point.getX() - 5, point.getY() - 5, 10, 10));
		}
	}
	
//	画出文字
	public void drawroomname(Graphics2D g2) throws SQLException
	{
		namelist=new Vector<>();
		ResultSet result = stmt.executeQuery("select * from room where floor='"+current_floor+"'");
		while (result.next())
		{	
			g2.setColor(Color.DARK_GRAY);
			String coordinate=result.getString("coordinate");
			double X=Double.parseDouble(coordinate.split(",")[0])*zoom;
			double Y=Double.parseDouble(coordinate.split(",")[1])*zoom;
			String roomname=result.getString("room_name");
			if (selected_name != null)
			if(X ==selected_name.X & Y== selected_name.Y)
				g2.setColor(Color.green);
			namelist.add(new Name(roomname, X, Y));
			g2.drawString(roomname, (float)X, (float)Y);
		}
	}

//	选中线段
	public void select_line(Point2D point2d)
	{			
		boolean select_flag=false;
		Enumeration<Line2D> enumeration = line.elements();
		while (enumeration.hasMoreElements())
		{
			Line2D line2d = enumeration.nextElement();
			if (PointToSegDist(point2d, line2d) < 5)
			{
				select_flag = true;
				//如果再次点击被选中线条，取消选中
				if (selected_line != null)
					if (line2d.getX1() == selected_line.getX1() & line2d.getX2() == selected_line.getX2()
							& line2d.getY1() == selected_line.getY1() & line2d.getY2() == selected_line.getY2())
						select_flag = false;
				selected_line = line2d;
				break;
			}
		}
		if (select_flag)
		{
			try
			{
				tool.weightField.setText(getweight());
			} catch (SQLException e1)
			{
				e1.printStackTrace();
			}
		} else
			selected_line = null;
		repaint();
	}
	
//	选中房间名
	public void select_name(Point2D point2d)
	{
		boolean select_flag=false;
		
		Enumeration<Name> enumeration = namelist.elements();
		while (enumeration.hasMoreElements())
		{
			Name roomname = enumeration.nextElement();
			Line2D line2d=new Line2D.Double(roomname.X,roomname.Y-5,roomname.X+roomname.name.length()*9,roomname.Y-5);
			if (PointToSegDist(point2d, line2d) < 5)
			{
				System.out.println(roomname.X+" "+roomname.Y);
				select_flag = true;
				//如果再次点击被选中线条，取消选中
				if (selected_name != null)
					if (roomname.X ==selected_name.X & roomname.Y== selected_name.Y)
						select_flag = false;
				selected_name = roomname;
				break;
			}
		}
		if (select_flag)
		{
			tool.nameField.setText(getname());
		} else
			selected_name = null;
		repaint();
	}

//	计算点到线段的距离
	public double PointToSegDist(Point2D point2d, Line2D line2d)
	{
		double x=point2d.getX();
		double y=point2d.getY();
		double x1=line2d.getX1();
		double y1=line2d.getY1();
		double x2=line2d.getX2();
		double y2=line2d.getY2();
		
		double cross = (x2 - x1) * (x - x1) + (y2 - y1) * (y - y1);
		if (cross <= 0) 
			return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));

		double d2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
		if (cross >= d2)
			return Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2));

		double r = cross / d2;
		double px = x1 + (x2 - x1) * r;
		double py = y1 + (y2 - y1) * r;
		return Math.sqrt((x - px) * (x - px) + (py - y) * (py - y));
	}

//	设置被选中线的权重
	public void setweight(String weight) throws SQLException
	{
		if (selected_line!=null)
		{
			System.out.println("数据库操作：update edge set weight='"+weight+"' where pre_node like '"+selected_line.getX1()/zoom+"%,"+selected_line.getY1()/zoom+"%' and be_node like '"+selected_line.getX2()/zoom+"%,"+selected_line.getY2()/zoom+"%'");
			System.out.println("数据库操作：update edge set weight='"+weight+"' where be_node like '"+selected_line.getX1()/zoom+"%,"+selected_line.getY1()/zoom+"%' and pre_node like '%"+selected_line.getX2()/zoom+"%,"+selected_line.getY2()/zoom+"%'");
			stmt.executeUpdate("update edge set weight='"+weight+"' where pre_node like '"+selected_line.getX1()/zoom+"%,"+selected_line.getY1()/zoom+"%' and be_node like '"+selected_line.getX2()/zoom+"%,"+selected_line.getY2()/zoom+"%'");
			stmt.executeUpdate("update edge set weight='"+weight+"' where be_node like '"+selected_line.getX1()/zoom+"%,"+selected_line.getY1()/zoom+"%' and pre_node like '%"+selected_line.getX2()/zoom+"%,"+selected_line.getY2()/zoom+"%'");
		}
		else
			JOptionPane.showMessageDialog(null, "未选中任何线段", "错误", JOptionPane.ERROR_MESSAGE);

	}
	
//	获取被选中线的权重
	public String getweight() throws SQLException
	{

		if (selected_line != null)
		{
			System.out.println("数据库操作：select * from edge where pre_node like '" + selected_line.getX1()/zoom+ "%,"
					+ selected_line.getY1()/zoom + "%' and be_node like '" + selected_line.getX2()/zoom + "%," + selected_line.getY2()/zoom + "%'");
			ResultSet resultSet = stmt.executeQuery("select * from edge where pre_node like '" + selected_line.getX1()/zoom+ "%,"
					+ selected_line.getY1()/zoom + "%' and be_node like '" + selected_line.getX2()/zoom + "%," + selected_line.getY2()/zoom + "%'");
			System.out.println("获取权重："+resultSet.getString("weight"));
			return resultSet.getString("weight");
			
			
		} else
			return "1";
	}
	
//	设置被选中的房间名
	public void setname(String name) throws SQLException
	{
		if (selected_name!=null)
		{
			System.out.println("数据库操作：update room set room_name='"+name+"' where coordinate like '"+selected_name.X/zoom+"%,"+selected_name.Y/zoom+"%'");
			System.out.println("数据库操作：update map"+current_floor.replace("-", "_")+" set text_line=replace(text_line,'"+selected_name.name+"','"+name+"')");
			stmt.executeUpdate("update room set room_name='"+name+"' where coordinate like '"+selected_name.X/zoom+"%,"+selected_name.Y/zoom+"%'");
			stmt.executeUpdate("update map"+current_floor.replace("-", "_")+" set text_line=replace(text_line,'"+selected_name.name+"','"+name+"')");
			repaint();
		}
		else
			JOptionPane.showMessageDialog(null, "未选中任何房间名", "错误", JOptionPane.ERROR_MESSAGE);
	}
	
//	获取被选中的房间名
	public String getname()
	{
		return selected_name.name;
	}
	
//	新建房间名
	public void New_Name(Point2D point2d) throws SQLException
	{
		selected_name=new Name("请输入名字", point2d.getX(), point2d.getY());
		stmt.execute("insert into room(room_name,coordinate,floor) values('"+selected_name.name+"','"+selected_name.X/zoom+","+selected_name.Y/zoom+"','"+current_floor+"')");
		ResultSet resultSet=stmt.executeQuery("select * from map"+current_floor.replace("-", "_")+" order by _id desc");
		stmt.executeUpdate("update map"+current_floor.replace("-", "_")+" set text_line='<text transform=\"matrix(2.67151 0 -0 3.33938 "+selected_name.X/zoom+" "+selected_name.Y/zoom+")\" font-size=\"1.43\" style=\"font-family: txt\" >"+selected_name.name+"</text>' where _id='"+resultSet.getString("_id")+"'");
//		System.out.println("数据库操作:insert into room(room_name,coordinate,floor) values('"+selected_name.name+"','"+selected_name.X/zoom+","+selected_name.Y/zoom+"','"+current_floor+"')"
//				+"\nselect * from map"+current_floor+" order by _id desc"
//				+"\nupdate map"+current_floor.replace("-", "_")+" set text_line='<text transform=\"matrix(2.67151 0 -0 3.33938 "+selected_name.X/zoom+" "+selected_name.Y/zoom+")\" font-size=\"1.43\" style=\"font-family: txt\" >"+selected_name.name+"</text>' where _id='"+resultSet.getString("_id")+"'"
//				+"\ninsert into map"+current_floor.replace("-", "_")+"(text_line) values('</g>\n</svg>')");
		stmt.execute("insert into map"+current_floor.replace("-", "_")+"(text_line) values('</g>\n</svg>')");
		tool.nameField.setText("请输入名字");
		repaint();
	}
	
//	删除房间名
	public void delete_name()
	{
		try
		{
			System.out.println(zoom);
			System.out.println("数据库操作:delete from room where coordinate like '"+selected_name.X/zoom+"%,"+selected_name.Y/zoom+"%'");
			System.out.println("数据库操作:delete from map"+current_floor.replace("-", "_")+" where text_line like '%"+selected_name.X/zoom+"%"+selected_name.Y/zoom+"%'");
			stmt.execute("delete from room where coordinate like '"+selected_name.X/zoom+"%,"+selected_name.Y/zoom+"%'");
			stmt.execute("delete from map"+current_floor.replace("-", "_")+" where text_line like '%"+selected_name.X/zoom+" "+selected_name.Y/zoom+"%'");
			repaint();
			tool.nameField.setText("");
			
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
//	房间名结构
	class Name
	{
		String name;
		double X;
		double Y;
		public Name(String str,double d,double e)
		{
			name=str;
			X=d;
			Y=e;
		}
	}
	
    public static void main(String[] agrs) throws ClassNotFoundException, SQLException{  
//		JFrame.setDefaultLookAndFeelDecorated(true);
    	new editdb();
    }  
}
