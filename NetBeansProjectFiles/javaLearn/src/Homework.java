
import java.util.*;
import javax.swing.*;

public class Homework {

    int[][] a;
    int i, j;

    public static void main(String args[]) {
        Homework w = new Homework();
        w.work();
    }

    void yh(int n) {
        a = new int[n][n];
        for (i = 0; i < n; i++) {
            a[i][0] = a[i][i] = 1;
        }
        for (i = 2; i < n; i++) {
            for (j = 1; j < i; j++) {
                a[i][j] = a[i - 1][j - 1] + a[i - 1][j];
            }
        }
    }

    void work() {
        int n, i, j;
        Scanner s = new Scanner(System.in);
        System.out.println("Input n:");
        n = s.nextInt();
        yh(n);

        for (i = 0; i < n; i++) {
            for (j = 0; j <= i; j++) {
                System.out.printf("%d ", a[i][j]);
            }
            System.out.println("");
        }
        n = Integer.parseInt(JOptionPane.showInputDialog(null, "Input n:", "Pascal Triangle", JOptionPane.INFORMATION_MESSAGE));
        String str = new String();
        for (i = 0; i < n; i++) {
            for (j = 0; j <= i; j++) {
                str += a[i][j] + " ";
            }
            str += "\n";
        }
        JOptionPane.showMessageDialog(null, str, "Pascal Triangle", JOptionPane.INFORMATION_MESSAGE);
    }
}