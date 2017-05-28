package resources;

import java.io.IOException;
import java.util.Scanner;

public class TestDLList {
    public static void main(String[] args) throws IOException
    {
        DLinkedList<Integer> dlist = new DLinkedList<Integer>();
        Scanner s = new Scanner(System.in);
        int resp= 0;
        int n2 = 0;
        DLNode<Integer> node;

        while(true){
            System.out.println("(1): Add\n(2): Remove");
            resp = s.nextInt();

            if(resp == 1){
                System.out.println("(1):First\n(2):Last\n(3):Before\n(4):After");
                resp = s.nextInt();
                System.out.println("Number: ");
                int n = s.nextInt();
                switch (resp){
                    case 1:
                        dlist.addFirst(n);
                        break;
                    case 2:
                        dlist.addLast(n);
                        break;
                    case 3:
                        n2 = s.nextInt();
                        node = dlist.getNode(n2);
                        if(node != null)
                            dlist.addBefore(node,n);
                        break;
                    case 4:
                        n2 = s.nextInt();
                        node = dlist.getNode(n2);
                        if(node != null)
                            dlist.addAfter(node,n);
                        break;
                }
            }
            else{
                System.out.println("(1):First\n(2):Last\n(3):Node");
                resp = s.nextInt();
                switch (resp){
                    case 1:
                        dlist.removeFirst();
                        break;
                    case 2:
                        dlist.removeLast();
                        break;
                    case 3:
                        System.out.println("Number: ");
                        int n = s.nextInt();
                        node = dlist.getNode(n);
                        if(node != null)
                            dlist.removeNode(node);
                        break;
                }
            }

            System.out.println(dlist.toString());
        }
    }
}
