package client;

import message.MessageRMI;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main
{
    private static MessageRMI stub = null;

    public static void main(String[] args) throws IOException
    {
        System.out.println("CLIENT");

        //args verification
        if(args.length < 2)
        {
            System.out.println("Usage: java Main <peer_ap> <sub_protocol> <opnd_1> <opnd_2>*");
            return;
        }

        //start rmi for server communication
        startRMI(args[0]);

        if(!protocolVerAndSend(args))
            System.out.println("Error transmiting protocol");
    }

    private static void startRMI(String remoteObjName) {
        try
        {
            Registry registry = LocateRegistry.getRegistry(null);
            stub = (MessageRMI) registry.lookup(remoteObjName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static boolean protocolVerAndSend(String args[])
    {
        try {
            switch (args[1]) {
                case "BACKUP":
                    if(args.length != 4)
                        return false;
                    stub.backup(args[2],Integer.parseInt(args[3]));
                    break;
                case "RESTORE":
                    if(args.length != 3)
                        return false;
                    stub.restore(args[2]);
                    break;
                case "DELETE":
                    if(args.length != 3)
                        return false;
                    stub.delete(args[2]);
                    break;
                case "RECLAIM":
                    if(args.length != 3)
                        return false;
                    stub.reclaim(Integer.parseInt(args[2]));
                    break;
                case "STATE":
                    if(args.length != 2)
                        return false;
                    stub.state();
                    break;
                default:
                    System.out.println("Error sending message");
                    return false;
            }
        }
        catch (NumberFormatException | RemoteException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
