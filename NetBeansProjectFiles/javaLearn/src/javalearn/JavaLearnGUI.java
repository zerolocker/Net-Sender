/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javalearn;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.*;

/**
 *
 * @author Zero
 */
public class JavaLearnGUI extends JFrame{
    JavaLearnGUI(){
        add(new MMP("welcome"));
    }
    
    public static void main(String[] args){
        JavaLearnGUI frame=new JavaLearnGUI();
        frame.setSize(400,300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
    static class MMP extends JPanel{
        private String message="None";
        private int x=20;
        private int y=20;
        public MMP(){}
        public MMP(String s){
            message=s;
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    x=e.getX();
                    y=e.getY();
                    paintComponent(e.getComponent().getGraphics());
                }
            });
        }
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawString(message,x,y);
        }
    }
 }