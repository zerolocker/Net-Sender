/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javalearn;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * ��GUI��ϰ�ṩ���غͱʼǡ�
 * @author Zero
 */
public class MyFrame extends JFrame{
    public static void main(String [] args){        
        
        //��δ����Ѿ��Ǵ��������������̴��롣Ҫ��ס��
        JFrame frame=new MyFrame();
        frame.setBounds(200, 200, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //�����ڹرմ��ں���Ȼ����
        frame.setVisible(true);
        frame.validate();
        
        System.out.println(frame.getLayout() instanceof BorderLayout); //true.ûָ��ʱ��һ������Ĭ�ϵĲ��ֹ�������BorderLayout
        frame.setLayout(new FlowLayout(10,20,10));//�������ֹ�����
         
    }
    public MyFrame()
    {
        add(jbtOK);
        add(new JButton("OK2"),BorderLayout.WEST);
        
        //��Ӽ������ķ���ڱ�ĵط�д��jbtOKactionPerformed��������������á������ɱ�ü�ࡣ
        jbtOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jbtOKactionPerformed(e);
            }
        });
        
        //��Щ��������һ�緽����ʹ��ListenerҪʵ������ȫ������ʱ������Adapter���棬����ʵ���Ҳ���Ҫ�ķ���������ࡣ
        addWindowListener(new WindowAdapter() {});
    }
    
    private void jbtOKactionPerformed(ActionEvent e)
    {System.out.println("jbtOK clicked.");}
    
    //��񣺰ѿؼ�������������ô��ǣ��Ժ����ͨ��this.jbtOK��������
    private JButton jbtOK=new JButton("OK"); 
}
