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
        J2(){ //super三种用法：构造函数第一句、操作隐藏的方法、操作隐藏的变量,隐藏指的是被重写(注意和重载区分)
            super();
            super.p();
            p();
            System.out.println("super.x in J2:"+super.x);
        }
    }
}
//实验1：基本知识与概念的实验
//说明
//各位同学请注意：只能提交一次，但在提交前可存为草稿保存在blackboard上。
//请点击进入
//
//
//实验内容：
//1、参照课本例1-1编写一个java应用程序，程序能输出“Hello,my name is ****,my student No is *********”.
//2、参看《大学实用教程学习指导》page 4 1.2.2，输入并运行下面的程序。
//FirstApplet.java
//import java.applet.*;
//import java.awt.*;
//public class FirstApplet extends Applet{
//public void paint(Graphics g){
//g.setColor(Color.blue);
//g.drawString("这是一个java程序",12,30);
//g.setColor(Color.red);
//g.setFont(new Font("宋体",Font.BOLD,36));
//g.drawString("我改变了字体",22,56); 
//}
//}