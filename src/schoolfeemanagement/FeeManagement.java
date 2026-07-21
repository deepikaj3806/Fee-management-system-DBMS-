package schoolfeemanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FeeManagement extends JFrame implements ActionListener
{
JLabel l1,l2,l3,l4,l5,l6,l7,l8;

JTextField t1,t2,t3,t4,t5,t6,t7,t8;

JButton updateBtn,showBtn,receiptBtn;

JTable table;
DefaultTableModel model;

Connection con;
PreparedStatement pst;

String loginUser="";
String loginRole="";
int loginId=0;

FeeManagement()
{
connect();

if(loginCheck()==false)
System.exit(0);

setTitle("Smart School Fee Management");
setSize(1100,600);
setLayout(new FlowLayout());

l1=new JLabel("ID");
add(l1);
t1=new JTextField(5);
add(t1);

l2=new JLabel("Name");
add(l2);
t2=new JTextField(10);
add(t2);

l3=new JLabel("Class");
add(l3);
t3=new JTextField(8);
add(t3);

l4=new JLabel("Total Fee");
add(l4);
t4=new JTextField(7);
add(t4);

l5=new JLabel("Paid");
add(l5);
t5=new JTextField(7);
add(t5);

l6=new JLabel("Due Date");
add(l6);
t6=new JTextField(10);
add(t6);

l7=new JLabel("Scholarship");
add(l7);
t7=new JTextField(5);
add(t7);

l8=new JLabel("Installment");
add(l8);
t8=new JTextField(5);
add(t8);

updateBtn=new JButton("UPDATE");
showBtn=new JButton("SHOW");
receiptBtn=new JButton("RECEIPT");

add(updateBtn);
add(showBtn);
add(receiptBtn);

updateBtn.addActionListener(this);
showBtn.addActionListener(this);
receiptBtn.addActionListener(this);

model=new DefaultTableModel();

model.addColumn("ID");
model.addColumn("Name");
model.addColumn("Class");
model.addColumn("Total");
model.addColumn("Paid");
model.addColumn("Balance");
model.addColumn("Status");
model.addColumn("Due Date");
model.addColumn("Fine");
model.addColumn("Scholarship");
model.addColumn("Installment");

table=new JTable(model);

JScrollPane sp=new JScrollPane(table);
sp.setPreferredSize(new Dimension(1050,350));
add(sp);

if(loginRole.equalsIgnoreCase("Admin"))
{
hideFieldsForAdmin();
updateBtn.setEnabled(false);
}
else
{
showStudentData();
}

setVisible(true);
setDefaultCloseOperation(EXIT_ON_CLOSE);
}

public void connect()
{
try
{
Class.forName("com.mysql.cj.jdbc.Driver");

con=DriverManager.getConnection(
"jdbc:mysql://localhost:3306/school_fee",
"root",
"deepika");
}
catch(Exception e)
{
System.out.println(e);
}
}

public boolean loginCheck()
{
try
{
String user=
JOptionPane.showInputDialog(this,"Enter Username");

String pass=
JOptionPane.showInputDialog(this,"Enter Password");

pst=con.prepareStatement(
"select * from fee where username=? and password=?");

pst.setString(1,user);
pst.setString(2,pass);

ResultSet rs=pst.executeQuery();

if(rs.next())
{
loginUser=user;
loginRole=rs.getString("role");
loginId=rs.getInt("id");

JOptionPane.showMessageDialog(this,
"Login Success : "+loginRole);

return true;
}
else
{
JOptionPane.showMessageDialog(this,
"Invalid Login");
return false;
}
}
catch(Exception ex)
{
System.out.println(ex);
return false;
}
}

public void hideFieldsForAdmin()
{
l1.setVisible(false);
l2.setVisible(false);
l3.setVisible(false);
l4.setVisible(false);
l5.setVisible(false);
l6.setVisible(false);
l7.setVisible(false);
l8.setVisible(false);

t1.setVisible(false);
t2.setVisible(false);
t3.setVisible(false);
t4.setVisible(false);
t5.setVisible(false);
t6.setVisible(false);
t7.setVisible(false);
t8.setVisible(false);
}

public void showStudentData()
{
try
{
pst=con.prepareStatement(
"select * from fee where id=?");

pst.setInt(1,loginId);

ResultSet rs=pst.executeQuery();

if(rs.next())
{
t1.setText(rs.getInt("id")+"");
t2.setText(rs.getString("name"));
t3.setText(rs.getString("class"));
t4.setText(rs.getDouble("total")+"");
t6.setText(rs.getString("due_date"));
t7.setText(rs.getDouble("scholarship")+"");
t8.setText(rs.getInt("installment")+"");
}
}
catch(Exception ex)
{
System.out.println(ex);
}
}

public void actionPerformed(ActionEvent e)
{
if(e.getSource()==updateBtn)
updateOwnRecord();

if(e.getSource()==showBtn)
{
if(loginRole.equalsIgnoreCase("Admin"))
loadAll();
else
loadOwn();
}

if(e.getSource()==receiptBtn)
generateReceipt();
}

public void updateOwnRecord()
{
try
{
if(loginRole.equalsIgnoreCase("Admin"))
{
JOptionPane.showMessageDialog(this,
"Admin Cannot Update");
return;
}

int enteredId=
Integer.parseInt(t1.getText());

if(enteredId!=loginId)
{
JOptionPane.showMessageDialog(this,
"You can update only your own account");
return;
}

double newPay=
Double.parseDouble(t5.getText());

pst=con.prepareStatement(
"select * from fee where id=?");

pst.setInt(1,loginId);

ResultSet rs=pst.executeQuery();

if(rs.next())
{
double oldPaid=rs.getDouble("paid");
double total=rs.getDouble("total");
double balance=rs.getDouble("balance");
int oldInstall=rs.getInt("installment");

if(balance==0)
{
JOptionPane.showMessageDialog(this,
"Fee Already Fully Paid");
t5.setText("");
return;
}

double updatedPaid=
oldPaid + newPay;

double newBalance=
total - updatedPaid;

if(newBalance<0)
newBalance=0;

String status;

if(newBalance==0)
status="Paid";
else
status="Partial";

pst=con.prepareStatement(
"update fee set paid=?, balance=?, installment=?, status=? where id=?");

pst.setDouble(1,updatedPaid);
pst.setDouble(2,newBalance);
pst.setInt(3,oldInstall+1);
pst.setString(4,status);
pst.setInt(5,loginId);

pst.executeUpdate();

JOptionPane.showMessageDialog(this,
"Your Account Updated Successfully");

showStudentData();
loadOwn();
t5.setText("");
}
}
catch(Exception ex)
{
System.out.println(ex);
}
}

public void loadAll()
{
try
{
model.setRowCount(0);

Statement st=con.createStatement();

ResultSet rs=st.executeQuery(
"select id,name,class,total,paid,balance,status,due_date,fine,scholarship,installment from fee");

while(rs.next())
{
model.addRow(new Object[]{
rs.getInt(1),
rs.getString(2),
rs.getString(3),
rs.getDouble(4),
rs.getDouble(5),
rs.getDouble(6),
rs.getString(7),
rs.getString(8),
rs.getDouble(9),
rs.getDouble(10),
rs.getInt(11)
});
}
}
catch(Exception ex)
{
System.out.println(ex);
}
}

public void loadOwn()
{
try
{
model.setRowCount(0);

pst=con.prepareStatement(
"select id,name,class,total,paid,balance,status,due_date,fine,scholarship,installment from fee where id=?");

pst.setInt(1,loginId);

ResultSet rs=pst.executeQuery();

while(rs.next())
{
model.addRow(new Object[]{
rs.getInt(1),
rs.getString(2),
rs.getString(3),
rs.getDouble(4),
rs.getDouble(5),
rs.getDouble(6),
rs.getString(7),
rs.getString(8),
rs.getDouble(9),
rs.getDouble(10),
rs.getInt(11)
});
}
}
catch(Exception ex)
{
System.out.println(ex);
}
}

public void generateReceipt()
{
try
{
pst=con.prepareStatement(
"select id,name,class,total,paid,balance,status,fine,installment from fee where id=?");

pst.setInt(1,loginId);

ResultSet rs=pst.executeQuery();

if(rs.next())
{
JTextArea area=new JTextArea(18,30);

area.setText(
"******** SCHOOL RECEIPT ********\n\n"+
"ID          : "+rs.getInt(1)+"\n"+
"Name        : "+rs.getString(2)+"\n"+
"Class       : "+rs.getString(3)+"\n"+
"Total Fee   : "+rs.getDouble(4)+"\n"+
"Paid        : "+rs.getDouble(5)+"\n"+
"Balance     : "+rs.getDouble(6)+"\n"+
"Status      : "+rs.getString(7)+"\n"+
"Fine        : "+rs.getDouble(8)+"\n"+
"Installment : "+rs.getInt(9)+"\n\n"+
"Thank You");

JOptionPane.showMessageDialog(
this,
new JScrollPane(area),
"Receipt",
JOptionPane.INFORMATION_MESSAGE);
}
}
catch(Exception ex)
{
System.out.println(ex);
}
}

public static void main(String args[])
{
new FeeManagement();
}
}
