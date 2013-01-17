/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javalearn;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * 给GUI练习提供场地和笔记。
 * @author Zero
 */
public class MyFrame extends JFrame{
    public static void main(String [] args){        
        
        //这段代码已经是创建窗口所需的最短代码。要记住。
        JFrame frame=new MyFrame();
        frame.setBounds(200, 200, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //否则在关闭窗口后仍然运行
        frame.setVisible(true);
        frame.validate();
        
        System.out.println(frame.getLayout() instanceof BorderLayout); //true.没指定时，一个窗口默认的布局管理器是BorderLayout
        frame.setLayout(new FlowLayout(10,20,10));//换个布局管理器
         
    }
    public MyFrame()
    {
        add(jbtOK);
        add(new JButton("OK2"),BorderLayout.WEST);
        
        //添加监听器的风格：在别的地方写个jbtOKactionPerformed方法，在这里调用。这样可变得简洁。
        jbtOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jbtOKactionPerformed(e);
            }
        });
        
        //有些监听器有一坨方法，使用Listener要实现它们全部。这时可以用Adapter代替，避免实现我不需要的方法，更简洁。
        addWindowListener(new WindowAdapter() {});
    }
    
    private void jbtOKactionPerformed(ActionEvent e)
    {System.out.println("jbtOK clicked.");}
    
    //风格：把控件声明放在这里。好处是，以后可以通过this.jbtOK访问它。
    private JButton jbtOK=new JButton("OK"); 
}
