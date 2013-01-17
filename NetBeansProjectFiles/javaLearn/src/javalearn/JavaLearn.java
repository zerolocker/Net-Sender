/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javalearn;

import java.awt.Graphics;
import java.util.InputMismatchException;
import java.util.Scanner;


/**
 *
 * @author Zero
 */
public class JavaLearn {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner s=new Scanner(System.in);
        boolean continueInput=true;
        do{
            try{
                System.out.print("Enter an Integer:");
                int n=s.nextInt();
                System.out.println("The number entered is :"+n);
                continueInput=false;
            }catch(InputMismatchException e){
                System.out.println("Try again.");
                s.nextLine();
            }
        }while(continueInput);
        JavaLearn j=new JavaLearn();
        J1 [] jj={j.new J1(),j.new J2()};
        System.out.println(jj[1] instanceof J2);
        J2 c=(J2)jj[0];
    }
    class J1{
        String x="J1";
        void p(){System.out.println(x);}
        
    }
    class J2 extends J1{
        String x="J2";
        void p(){System.out.println(x);}
        J2(){ //super�����÷������캯����һ�䡢�������صķ������������صı���,����ָ���Ǳ���д(ע�����������)
            super();
            super.p();
            p();
            System.out.println("super.x in J2:"+super.x);
        }
    }
}
//ʵ��1������֪ʶ������ʵ��
//˵��
//��λͬѧ��ע�⣺ֻ���ύһ�Σ������ύǰ�ɴ�Ϊ�ݸ屣����blackboard�ϡ�
//��������
//
//
//ʵ�����ݣ�
//1�����տα���1-1��дһ��javaӦ�ó��򣬳����������Hello,my name is ****,my student No is *********��.
//2���ο�����ѧʵ�ý̳�ѧϰָ����page 4 1.2.2�����벢��������ĳ���
//FirstApplet.java
//import java.applet.*;
//import java.awt.*;
//public class FirstApplet extends Applet{
//public void paint(Graphics g){
//g.setColor(Color.blue);
//g.drawString("����һ��java����",12,30);
//g.setColor(Color.red);
//g.setFont(new Font("����",Font.BOLD,36));
//g.drawString("�Ҹı�������",22,56); 
//}
//}